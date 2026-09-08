package com.gocomet.auction.service;

import com.gocomet.auction.constant.ExceptionMessageConstants;
import com.gocomet.auction.constant.LogMessageConstants;
import com.gocomet.auction.constant.RfqConstants;
import com.gocomet.auction.dto.request.CreateRfqRequestDto;
import com.gocomet.auction.dto.response.AuctionActivityLogResponseDto;
import com.gocomet.auction.dto.response.QuoteResponseDto;
import com.gocomet.auction.dto.response.RfqResponseDto;
import com.gocomet.auction.entity.QuoteEntity;
import com.gocomet.auction.entity.RfqEntity;
import com.gocomet.auction.enums.ActivityEventType;
import com.gocomet.auction.enums.AuctionStatus;
import com.gocomet.auction.exception.InvalidRfqTimelineException;
import com.gocomet.auction.exception.ResourceNotFoundException;
import com.gocomet.auction.repository.QuoteRepository;
import com.gocomet.auction.repository.RfqRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RfqManagementService {

    private final RfqRepository rfqRepository;
    private final QuoteRepository quoteRepository;
    private final AuctionAuditLogService auditLogService;
    private final AuctionExtensionService extensionService;
    private final Random random = new Random();
    @Transactional
    public RfqResponseDto createRfq(CreateRfqRequestDto request) {
        log.info("Initiating RFQ creation: '{}' with Start: {}, Close: {}, ForcedClose: {}",
        request.getName(), request.getBidStartTime(), request.getBidCloseTime(), request.getForcedBidCloseTime());

        validateTimelineConstraints(request.getBidStartTime(), request.getBidCloseTime(), request.getForcedBidCloseTime());

        String referenceId = resolveReferenceId(request.getReferenceId());

        Instant now = Instant.now();
        AuctionStatus initialStatus = now.isBefore(request.getBidStartTime())
        ? AuctionStatus.DRAFT
        : AuctionStatus.ACTIVE;

        RfqEntity rfq = RfqEntity.builder()
        .referenceId(referenceId)
        .name(request.getName().trim())
        .bidStartTime(request.getBidStartTime())
        .bidCloseTime(request.getBidCloseTime())
        .originalBidCloseTime(request.getBidCloseTime())
        .forcedBidCloseTime(request.getForcedBidCloseTime())
        .pickupServiceDate(request.getPickupServiceDate())
        .status(initialStatus)
        .triggerWindowMinutes(request.getTriggerWindowMinutes() != null
        ? request.getTriggerWindowMinutes() : RfqConstants.DEFAULT_TRIGGER_WINDOW_MINUTES)
        .extensionDurationMinutes(request.getExtensionDurationMinutes() != null
        ? request.getExtensionDurationMinutes() : RfqConstants.DEFAULT_EXTENSION_DURATION_MINUTES)
        .extensionTriggerType(request.getExtensionTriggerType())
        .totalExtensionsCount(0)
        .build();

        RfqEntity saved = rfqRepository.save(rfq);

        auditLogService.logEvent(
        saved,
        null,
        ActivityEventType.RFQ_CREATED,
        String.format("RFQ '%s' created with Trigger Window X=%dm, Extension Duration Y=%dm, Rule=%s.",
        saved.getReferenceId(), saved.getTriggerWindowMinutes(),
        saved.getExtensionDurationMinutes(), saved.getExtensionTriggerType()),
        saved.getBidCloseTime(),
        saved.getBidCloseTime(),
        null,
        null
    );

        log.info(LogMessageConstants.LOG_RFQ_CREATED,
        saved.getId(), saved.getReferenceId(), saved.getBidStartTime(),
        saved.getBidCloseTime(), saved.getForcedBidCloseTime(), saved.getExtensionTriggerType());

        return mapToResponseDto(saved, true);
    }
    @Transactional
    public RfqResponseDto getRfqById(Long rfqId) {
        RfqEntity rfq = rfqRepository.findById(rfqId)
        .orElseThrow(() -> new ResourceNotFoundException(
        String.format(ExceptionMessageConstants.MSG_RFQ_NOT_FOUND, rfqId)));

        syncRfqStatusWithClock(rfq);
        return mapToResponseDto(rfq, true);
    }
    @Transactional
    public RfqResponseDto getRfqByReferenceId(String referenceId) {
        RfqEntity rfq = rfqRepository.findByReferenceId(referenceId)
        .orElseThrow(() -> new ResourceNotFoundException(
        String.format(ExceptionMessageConstants.MSG_RFQ_REF_NOT_FOUND, referenceId)));

        syncRfqStatusWithClock(rfq);
        return mapToResponseDto(rfq, true);
    }
    @Transactional
    public List<RfqResponseDto> getAllRfqs(AuctionStatus statusFilter) {
        refreshAuctionStatuses();

        List<RfqEntity> rfqs = (statusFilter != null)
        ? rfqRepository.findByStatusOrderByCreatedAtDesc(statusFilter)
        : rfqRepository.findAllByOrderByCreatedAtDesc();

        return rfqs.stream()
        .map(rfq -> mapToResponseDto(rfq, false))
        .collect(Collectors.toList());
    }
    @Transactional
    public void refreshAuctionStatuses() {
        Instant now = Instant.now();

        List<RfqEntity> forcedExpired = rfqRepository.findForceCloseExpiredRfqs(now);
        for (RfqEntity rfq : forcedExpired) {
            rfq.setStatus(AuctionStatus.FORCE_CLOSED);
            rfqRepository.save(rfq);
            auditLogService.logEvent(
            rfq, null, ActivityEventType.AUCTION_FORCE_CLOSED,
            "Auction hit Forced Bid Close Time ceiling and is FORCE_CLOSED.",
            rfq.getBidCloseTime(), rfq.getBidCloseTime(), null, null
        );
        }

        List<RfqEntity> normalExpired = rfqRepository.findExpiredActiveRfqs(now);
        for (RfqEntity rfq : normalExpired) {
            rfq.setStatus(AuctionStatus.CLOSED);
            rfqRepository.save(rfq);
            auditLogService.logEvent(
            rfq, null, ActivityEventType.AUCTION_CLOSED,
            "Auction reached Bid Close Time and is officially CLOSED.",
            rfq.getBidCloseTime(), rfq.getBidCloseTime(), null, null
        );
        }
    }

    private void syncRfqStatusWithClock(RfqEntity rfq) {
        Instant now = Instant.now();
        if (rfq.getStatus() == AuctionStatus.ACTIVE || rfq.getStatus() == AuctionStatus.EXTENDED) {
            if (!now.isBefore(rfq.getForcedBidCloseTime())) {
                rfq.setStatus(AuctionStatus.FORCE_CLOSED);
                rfqRepository.save(rfq);
            } else if (!now.isBefore(rfq.getBidCloseTime())) {
                rfq.setStatus(AuctionStatus.CLOSED);
                rfqRepository.save(rfq);
            }
        } else if (rfq.getStatus() == AuctionStatus.DRAFT && !now.isBefore(rfq.getBidStartTime())) {
            rfq.setStatus(AuctionStatus.ACTIVE);
            rfqRepository.save(rfq);
        }
    }

    private void validateTimelineConstraints(Instant startTime, Instant closeTime, Instant forcedCloseTime) {
        if (closeTime.isBefore(startTime) || closeTime.equals(startTime)) {
            String msg = ExceptionMessageConstants.MSG_CLOSE_BEFORE_START;
            log.warn(LogMessageConstants.LOG_TIMELINE_VALIDATION_FAILED, msg);
            throw new InvalidRfqTimelineException(msg);
        }

        if (!forcedCloseTime.isAfter(closeTime)) {
            String msg = ExceptionMessageConstants.MSG_FORCED_CLOSE_BEFORE_CLOSE;
            log.warn(LogMessageConstants.LOG_TIMELINE_VALIDATION_FAILED, msg);
            throw new InvalidRfqTimelineException(msg);
        }
    }

    private String resolveReferenceId(String providedRef) {
        if (providedRef != null && !providedRef.trim().isEmpty()) {
            return providedRef.trim();
        }
        int randomSuffix = random.nextInt(RfqConstants.REFERENCE_RANDOM_SUFFIX_BOUND)
        + RfqConstants.REFERENCE_RANDOM_SUFFIX_OFFSET;
        return String.format("%s%d-%d", RfqConstants.RFQ_REFERENCE_PREFIX, Year.now().getValue(), randomSuffix);
    }

    private RfqResponseDto mapToResponseDto(RfqEntity rfq, boolean includeDetails) {
        Instant now = Instant.now();

        Optional<QuoteEntity> lowestQuoteOpt = quoteRepository.findFirstByRfqIdOrderByTotalAmountAscSubmittedAtAsc(rfq.getId());
        BigDecimal lowestBid = lowestQuoteOpt.map(QuoteEntity::getTotalAmount).orElse(null);
        String lowestCarrier = lowestQuoteOpt.map(QuoteEntity::getCarrierName).orElse(null);
        long totalQuotesCount = quoteRepository.countByRfqId(rfq.getId());

        long remainingToClose = Math.max(0, Duration.between(now, rfq.getBidCloseTime()).getSeconds());
        long remainingToForcedClose = Math.max(0, Duration.between(now, rfq.getForcedBidCloseTime()).getSeconds());

        boolean isInsideTriggerWindow = extensionService.isInsideTriggerWindow(rfq, now);
        boolean isForceCloseImminent = remainingToForcedClose <= (rfq.getExtensionDurationMinutes() * 60L);

        List<QuoteResponseDto> quotes = null;
        List<AuctionActivityLogResponseDto> logs = null;

        if (includeDetails) {
            quotes = quoteRepository.findByRfqIdOrderByTotalAmountAscSubmittedAtAsc(rfq.getId())
            .stream()
            .map(q -> QuoteResponseDto.builder()
            .id(q.getId())
            .rfqId(rfq.getId())
            .carrierName(q.getCarrierName())
            .freightCharges(q.getFreightCharges())
            .originCharges(q.getOriginCharges())
            .destinationCharges(q.getDestinationCharges())
            .totalAmount(q.getTotalAmount())
            .transitTimeDays(q.getTransitTimeDays())
            .quoteValidity(q.getQuoteValidity())
            .supplierRank(q.getSupplierRank())
            .rankLabel(RfqConstants.RANK_PREFIX + q.getSupplierRank())
            .submittedAt(q.getSubmittedAt())
            .build())
            .collect(Collectors.toList());

            logs = auditLogService.getLogsForRfq(rfq.getId());
        }

        return RfqResponseDto.builder()
        .id(rfq.getId())
        .referenceId(rfq.getReferenceId())
        .name(rfq.getName())
        .bidStartTime(rfq.getBidStartTime())
        .bidCloseTime(rfq.getBidCloseTime())
        .originalBidCloseTime(rfq.getOriginalBidCloseTime())
        .forcedBidCloseTime(rfq.getForcedBidCloseTime())
        .pickupServiceDate(rfq.getPickupServiceDate())
        .status(rfq.getStatus())
        .triggerWindowMinutes(rfq.getTriggerWindowMinutes())
        .extensionDurationMinutes(rfq.getExtensionDurationMinutes())
        .extensionTriggerType(rfq.getExtensionTriggerType())
        .totalExtensionsCount(rfq.getTotalExtensionsCount())
        .createdAt(rfq.getCreatedAt())
        .updatedAt(rfq.getUpdatedAt())
        .currentLowestBid(lowestBid)
        .currentLowestCarrier(lowestCarrier)
        .totalQuotesCount((int) totalQuotesCount)
        .remainingSecondsToClose(remainingToClose)
        .remainingSecondsToForcedClose(remainingToForcedClose)
        .isInsideTriggerWindow(isInsideTriggerWindow)
        .isForceCloseImminent(isForceCloseImminent)
        .quotes(quotes)
        .activityLogs(logs)
        .build();
    }
}
