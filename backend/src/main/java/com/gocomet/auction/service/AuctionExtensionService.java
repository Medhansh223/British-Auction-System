package com.gocomet.auction.service;

import com.gocomet.auction.constant.LogMessageConstants;
import com.gocomet.auction.dto.event.BidSubmissionContext;
import com.gocomet.auction.dto.event.ExtensionEvaluationResult;
import com.gocomet.auction.entity.RfqEntity;
import com.gocomet.auction.enums.AuctionStatus;
import com.gocomet.auction.event.AuctionExtendedEvent;
import com.gocomet.auction.repository.RfqRepository;
import com.gocomet.auction.factory.ExtensionStrategyFactory;
import com.gocomet.auction.strategy.ExtensionTriggerStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionExtensionService {

    private final ExtensionStrategyFactory strategyFactory;
    private final RfqRepository rfqRepository;
    private final ApplicationEventPublisher eventPublisher;
    @Transactional
    public ExtensionEvaluationResult evaluateAndApplyExtension(BidSubmissionContext context) {
        RfqEntity rfq = context.getRfq();
        Instant bidTime = context.getBidTimestamp();
        Instant currentCloseTime = rfq.getBidCloseTime();
        Instant forcedCloseTime = rfq.getForcedBidCloseTime();

        log.debug(LogMessageConstants.LOG_EVALUATING_EXTENSION, rfq.getId(), rfq.getExtensionTriggerType());

        if (!isInsideTriggerWindow(rfq, bidTime)) {
            log.debug(LogMessageConstants.LOG_TRIGGER_WINDOW_INACTIVE, bidTime, rfq.getId());
            return ExtensionEvaluationResult.builder()
            .extensionTriggered(false)
            .previousCloseTime(currentCloseTime)
            .newCloseTime(currentCloseTime)
            .cappedByForcedClose(false)
            .build();
        }

        log.info(LogMessageConstants.LOG_TRIGGER_WINDOW_ACTIVE,
        bidTime,
        currentCloseTime.minus(Duration.ofMinutes(rfq.getTriggerWindowMinutes())),
        currentCloseTime,
        rfq.getId());

        ExtensionTriggerStrategy strategy = strategyFactory.getStrategy(rfq.getExtensionTriggerType());
        boolean shouldExtend = strategy.shouldExtend(context);

        if (!shouldExtend) {
            log.info("Extension condition not satisfied for RFQ ID: {} under Strategy: {}",
            rfq.getId(), rfq.getExtensionTriggerType());
            return ExtensionEvaluationResult.builder()
            .extensionTriggered(false)
            .previousCloseTime(currentCloseTime)
            .newCloseTime(currentCloseTime)
            .cappedByForcedClose(false)
            .build();
        }

        long extensionMinutes = rfq.getExtensionDurationMinutes();
        Instant proposedNewCloseTime = currentCloseTime.plus(Duration.ofMinutes(extensionMinutes));

        boolean cappedByForcedClose = false;
        Instant actualNewCloseTime = proposedNewCloseTime;

        if (proposedNewCloseTime.isAfter(forcedCloseTime) || proposedNewCloseTime.equals(forcedCloseTime)) {
            actualNewCloseTime = forcedCloseTime;
            cappedByForcedClose = true;
            log.warn(LogMessageConstants.LOG_AUCTION_EXTENSION_CAPPED, rfq.getId(), forcedCloseTime);
        }

        if (actualNewCloseTime.isAfter(currentCloseTime)) {
            String baseReason = strategy.buildExtensionReason(context);
            String fullReason = cappedByForcedClose
            ? baseReason + " (Note: Extension reached the Forced Close Time ceiling)."
            : baseReason;

            rfq.setBidCloseTime(actualNewCloseTime);
            rfq.setTotalExtensionsCount(rfq.getTotalExtensionsCount() + 1);
            if (rfq.getStatus() == AuctionStatus.ACTIVE) {
                rfq.setStatus(AuctionStatus.EXTENDED);
            }
            rfqRepository.save(rfq);

            log.info(LogMessageConstants.LOG_AUCTION_EXTENDED,
            rfq.getId(), currentCloseTime, actualNewCloseTime, rfq.getTotalExtensionsCount(), fullReason);

            eventPublisher.publishEvent(new AuctionExtendedEvent(
            this, rfq, currentCloseTime, actualNewCloseTime, fullReason, cappedByForcedClose));

            return ExtensionEvaluationResult.builder()
            .extensionTriggered(true)
            .previousCloseTime(currentCloseTime)
            .newCloseTime(actualNewCloseTime)
            .cappedByForcedClose(cappedByForcedClose)
            .reason(fullReason)
            .build();
        } else {
            log.info("Auction close time is already at or beyond the forced close limit for RFQ ID: {}", rfq.getId());
            return ExtensionEvaluationResult.builder()
            .extensionTriggered(false)
            .previousCloseTime(currentCloseTime)
            .newCloseTime(currentCloseTime)
            .cappedByForcedClose(true)
            .reason("Extension limit reached: Auction is at maximum Forced Close Time.")
            .build();
        }
    }
    public boolean isInsideTriggerWindow(RfqEntity rfq, Instant timestamp) {
        if (rfq == null || timestamp == null) {
            return false;
        }
        Instant closeTime = rfq.getBidCloseTime();
        Instant triggerWindowStart = closeTime.minus(Duration.ofMinutes(rfq.getTriggerWindowMinutes()));

        return timestamp.isAfter(triggerWindowStart) && !timestamp.isAfter(closeTime);
    }
}
