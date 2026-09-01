package com.gocomet.auction.service.impl;

import com.gocomet.auction.constant.ExceptionMessageConstants;
import com.gocomet.auction.constant.LogMessageConstants;
import com.gocomet.auction.constant.RfqConstants;
import com.gocomet.auction.dto.event.BidSubmissionContext;
import com.gocomet.auction.dto.event.ExtensionEvaluationResult;
import com.gocomet.auction.dto.request.SubmitQuoteRequestDto;
import com.gocomet.auction.dto.response.QuoteResponseDto;
import com.gocomet.auction.entity.QuoteEntity;
import com.gocomet.auction.entity.RfqEntity;
import com.gocomet.auction.enums.AuctionStatus;
import com.gocomet.auction.event.BidSubmittedEvent;
import com.gocomet.auction.exception.AuctionClosedException;
import com.gocomet.auction.exception.AuctionNotStartedException;
import com.gocomet.auction.exception.QuoteValidationException;
import com.gocomet.auction.exception.ResourceNotFoundException;
import com.gocomet.auction.repository.QuoteRepository;
import com.gocomet.auction.repository.RfqRepository;
import com.gocomet.auction.service.AuctionExtensionService;
import com.gocomet.auction.service.QuoteSubmissionService;
import com.gocomet.auction.service.SupplierRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteSubmissionServiceImpl implements QuoteSubmissionService {

    private final RfqRepository rfqRepository;
    private final QuoteRepository quoteRepository;
    private final SupplierRankingService rankingService;
    private final AuctionExtensionService extensionService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public QuoteResponseDto submitQuote(Long rfqId, SubmitQuoteRequestDto request) {
        log.info(LogMessageConstants.LOG_QUOTE_SUBMITTING, rfqId, request.getCarrierName(),
        QuoteEntity.calculateTotal(request.getFreightCharges(), request.getOriginCharges(), request.getDestinationCharges()));

        RfqEntity rfq = rfqRepository.findById(rfqId)
        .orElseThrow(() -> {
            String msg = String.format(ExceptionMessageConstants.MSG_RFQ_NOT_FOUND, rfqId);
            log.error(LogMessageConstants.LOG_RESOURCE_NOT_FOUND, msg);
            return new ResourceNotFoundException(msg);
    });

        Instant now = Instant.now();
        validateAuctionStateForQuote(rfq, now);

        validateQuoteRequest(request);

        BigDecimal totalAmount = QuoteEntity.calculateTotal(
        request.getFreightCharges(), request.getOriginCharges(), request.getDestinationCharges());

        QuoteEntity newQuote = QuoteEntity.builder()
        .rfq(rfq)
        .carrierName(request.getCarrierName().trim())
        .freightCharges(request.getFreightCharges())
        .originCharges(request.getOriginCharges())
        .destinationCharges(request.getDestinationCharges())
        .totalAmount(totalAmount)
        .transitTimeDays(request.getTransitTimeDays())
        .quoteValidity(request.getQuoteValidity())
        .supplierRank(1)
        .submittedAt(now)
        .build();

        List<QuoteEntity> existingQuotes = quoteRepository.findByRfqIdOrderByTotalAmountAscSubmittedAtAsc(rfqId);
        BidSubmissionContext context = rankingService.calculateAndAssignRankings(rfq, newQuote, existingQuotes);

        QuoteEntity savedQuote = quoteRepository.save(newQuote);

        rankingService.reindexAllQuotesForRfq(rfqId);

        ExtensionEvaluationResult extensionResult = extensionService.evaluateAndApplyExtension(context);

        eventPublisher.publishEvent(new BidSubmittedEvent(
        this,
        rfq,
        savedQuote,
        extensionResult.isExtensionTriggered(),
        extensionResult.getReason()
));

        log.info(LogMessageConstants.LOG_QUOTE_SUBMITTED,
        savedQuote.getId(), rfqId, savedQuote.getSupplierRank(), savedQuote.getTotalAmount());

        return mapToResponseDto(savedQuote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuoteResponseDto> getQuotesForRfq(Long rfqId) {
        if (!rfqRepository.existsById(rfqId)) {
            throw new ResourceNotFoundException(String.format(ExceptionMessageConstants.MSG_RFQ_NOT_FOUND, rfqId));
        }
        return quoteRepository.findByRfqIdOrderByTotalAmountAscSubmittedAtAsc(rfqId)
        .stream()
        .map(this::mapToResponseDto)
        .collect(Collectors.toList());
    }

    private void validateAuctionStateForQuote(RfqEntity rfq, Instant now) {

        if (now.isBefore(rfq.getBidStartTime())) {
            String msg = String.format(ExceptionMessageConstants.MSG_AUCTION_NOT_STARTED,
            rfq.getReferenceId(), rfq.getBidStartTime());
            log.warn(msg);
            throw new AuctionNotStartedException(msg);
        }

        if (!now.isBefore(rfq.getForcedBidCloseTime())) {
            rfq.setStatus(AuctionStatus.FORCE_CLOSED);
            rfqRepository.save(rfq);
            String msg = String.format(ExceptionMessageConstants.MSG_AUCTION_FORCE_CLOSED, rfq.getReferenceId());
            log.warn(LogMessageConstants.LOG_QUOTE_ON_CLOSED_AUCTION, rfq.getReferenceId(), rfq.getId(), AuctionStatus.FORCE_CLOSED);
            throw new AuctionClosedException(msg);
        }

        if (!now.isBefore(rfq.getBidCloseTime())) {
            rfq.setStatus(AuctionStatus.CLOSED);
            rfqRepository.save(rfq);
            String msg = String.format(ExceptionMessageConstants.MSG_AUCTION_CLOSED, rfq.getReferenceId());
            log.warn(LogMessageConstants.LOG_QUOTE_ON_CLOSED_AUCTION, rfq.getReferenceId(), rfq.getId(), AuctionStatus.CLOSED);
            throw new AuctionClosedException(msg);
        }

        if (rfq.getStatus() == AuctionStatus.CLOSED || rfq.getStatus() == AuctionStatus.FORCE_CLOSED) {
            String msg = String.format(ExceptionMessageConstants.MSG_AUCTION_CLOSED, rfq.getReferenceId());
            log.warn(LogMessageConstants.LOG_QUOTE_ON_CLOSED_AUCTION, rfq.getReferenceId(), rfq.getId(), rfq.getStatus());
            throw new AuctionClosedException(msg);
        }
    }

    private void validateQuoteRequest(SubmitQuoteRequestDto request) {
        if (request.getCarrierName() == null || request.getCarrierName().trim().isEmpty()) {
            throw new QuoteValidationException(ExceptionMessageConstants.MSG_CARRIER_REQUIRED);
        }
        if (request.getFreightCharges() == null || request.getFreightCharges().compareTo(BigDecimal.ZERO) < 0) {
            throw new QuoteValidationException(ExceptionMessageConstants.MSG_FREIGHT_POSITIVE);
        }
        if (request.getOriginCharges() == null || request.getOriginCharges().compareTo(BigDecimal.ZERO) < 0) {
            throw new QuoteValidationException(ExceptionMessageConstants.MSG_ORIGIN_POSITIVE);
        }
        if (request.getDestinationCharges() == null || request.getDestinationCharges().compareTo(BigDecimal.ZERO) < 0) {
            throw new QuoteValidationException(ExceptionMessageConstants.MSG_DESTINATION_POSITIVE);
        }
        if (request.getTransitTimeDays() == null || request.getTransitTimeDays() < 1) {
            throw new QuoteValidationException(ExceptionMessageConstants.MSG_TRANSIT_POSITIVE);
        }
    }

    private QuoteResponseDto mapToResponseDto(QuoteEntity quote) {
        return QuoteResponseDto.builder()
        .id(quote.getId())
        .rfqId(quote.getRfq().getId())
        .carrierName(quote.getCarrierName())
        .freightCharges(quote.getFreightCharges())
        .originCharges(quote.getOriginCharges())
        .destinationCharges(quote.getDestinationCharges())
        .totalAmount(quote.getTotalAmount())
        .transitTimeDays(quote.getTransitTimeDays())
        .quoteValidity(quote.getQuoteValidity())
        .supplierRank(quote.getSupplierRank())
        .rankLabel(RfqConstants.RANK_PREFIX + quote.getSupplierRank())
        .submittedAt(quote.getSubmittedAt())
        .build();
    }
}
