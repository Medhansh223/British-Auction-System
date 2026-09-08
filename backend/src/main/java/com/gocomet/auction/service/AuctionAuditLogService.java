package com.gocomet.auction.service;

import com.gocomet.auction.dto.response.AuctionActivityLogResponseDto;
import com.gocomet.auction.entity.AuctionActivityLogEntity;
import com.gocomet.auction.entity.QuoteEntity;
import com.gocomet.auction.entity.RfqEntity;
import com.gocomet.auction.enums.ActivityEventType;
import com.gocomet.auction.event.AuctionExtendedEvent;
import com.gocomet.auction.event.BidSubmittedEvent;
import com.gocomet.auction.repository.AuctionActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionAuditLogService {

    private final AuctionActivityLogRepository activityLogRepository;
    @Transactional
    public AuctionActivityLogEntity logEvent(
    RfqEntity rfq,
    QuoteEntity quote,
    ActivityEventType eventType,
    String reason,
    Instant previousCloseTime,
    Instant newCloseTime,
    String carrierName,
    BigDecimal amount) {

        AuctionActivityLogEntity logEntity = AuctionActivityLogEntity.builder()
        .rfq(rfq)
        .quoteId(quote != null ? quote.getId() : null)
        .eventType(eventType)
        .reason(reason)
        .previousCloseTime(previousCloseTime)
        .newCloseTime(newCloseTime)
        .carrierName(carrierName)
        .bidAmount(amount)
        .createdAt(Instant.now())
        .build();

        AuctionActivityLogEntity saved = activityLogRepository.save(logEntity);
        log.debug("Recorded audit event [{}] for RFQ ID {}: {}", eventType, rfq.getId(), reason);
        return saved;
    }
    @Transactional(readOnly = true)
    public List<AuctionActivityLogResponseDto> getLogsForRfq(Long rfqId) {
        return activityLogRepository.findByRfqIdOrderByCreatedAtDesc(rfqId)
        .stream()
        .map(this::mapToResponseDto)
        .collect(Collectors.toList());
    }

    @EventListener
    public void onBidSubmitted(BidSubmittedEvent event) {
        String description = String.format("Quote submitted by '%s' with total amount $%s (Rank: L%d).",
        event.getQuote().getCarrierName(),
        event.getQuote().getTotalAmount().toPlainString(),
        event.getQuote().getSupplierRank());

        logEvent(
        event.getRfq(),
        event.getQuote(),
        ActivityEventType.BID_SUBMITTED,
        description,
        event.getRfq().getBidCloseTime(),
        event.getRfq().getBidCloseTime(),
        event.getQuote().getCarrierName(),
        event.getQuote().getTotalAmount()
    );
    }

    @EventListener
    public void onAuctionExtended(AuctionExtendedEvent event) {
        logEvent(
            event.getRfq(),
            null,
            ActivityEventType.AUCTION_EXTENDED,
            event.getReason(),
            event.getPreviousCloseTime(),
            event.getNewCloseTime(),
            null,
            null
        );
    }

    private AuctionActivityLogResponseDto mapToResponseDto(AuctionActivityLogEntity entity) {
        return AuctionActivityLogResponseDto.builder()
        .id(entity.getId())
        .rfqId(entity.getRfq().getId())
        .quoteId(entity.getQuoteId())
        .eventType(entity.getEventType())
        .reason(entity.getReason())
        .previousCloseTime(entity.getPreviousCloseTime())
        .newCloseTime(entity.getNewCloseTime())
        .carrierName(entity.getCarrierName())
        .bidAmount(entity.getBidAmount())
        .createdAt(entity.getCreatedAt())
        .build();
    }
}
