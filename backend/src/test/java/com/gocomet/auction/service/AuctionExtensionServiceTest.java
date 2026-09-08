package com.gocomet.auction.service;

import com.gocomet.auction.dto.event.BidSubmissionContext;
import com.gocomet.auction.dto.event.ExtensionEvaluationResult;
import com.gocomet.auction.entity.QuoteEntity;
import com.gocomet.auction.entity.RfqEntity;
import com.gocomet.auction.enums.AuctionStatus;
import com.gocomet.auction.enums.ExtensionTriggerType;
import com.gocomet.auction.repository.RfqRepository;
import com.gocomet.auction.factory.ExtensionStrategyFactory;
import com.gocomet.auction.strategy.AnyBidExtensionStrategy;
import com.gocomet.auction.strategy.AnyRankChangeExtensionStrategy;
import com.gocomet.auction.strategy.LowestBidderChangeExtensionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuctionExtensionServiceTest {

    @Mock
    private RfqRepository rfqRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AuctionExtensionService extensionService;

    @BeforeEach
    void setUp() {
        ExtensionStrategyFactory strategyFactory = new ExtensionStrategyFactory(List.of(
        new AnyBidExtensionStrategy(),
        new AnyRankChangeExtensionStrategy(),
        new LowestBidderChangeExtensionStrategy()
));
        extensionService = new AuctionExtensionService(strategyFactory, rfqRepository, eventPublisher);
    }

    @Test
    @DisplayName("Mode A (Any Bid): Should extend close time when bid arrives within trigger window")
    void testModeA_AnyBidInsideTriggerWindow_ShouldExtend() {
        Instant baseTime = Instant.parse("2026-09-01T18:00:00Z");
        Instant closeTime = baseTime;
        Instant forcedCloseTime = baseTime.plus(Duration.ofMinutes(30));
        Instant bidTime = baseTime.minus(Duration.ofMinutes(5));

        RfqEntity rfq = RfqEntity.builder()
        .id(1L)
        .referenceId("RFQ-TEST-001")
        .bidCloseTime(closeTime)
        .forcedBidCloseTime(forcedCloseTime)
        .triggerWindowMinutes(10)
        .extensionDurationMinutes(5)
        .extensionTriggerType(ExtensionTriggerType.ANY_BID)
        .status(AuctionStatus.ACTIVE)
        .totalExtensionsCount(0)
        .build();

        QuoteEntity quote = QuoteEntity.builder()
        .carrierName("DHL")
        .totalAmount(new BigDecimal("1000.00"))
        .supplierRank(1)
        .submittedAt(bidTime)
        .build();

        BidSubmissionContext context = BidSubmissionContext.builder()
        .rfq(rfq)
        .newQuote(quote)
        .bidTimestamp(bidTime)
        .isAnyRankChanged(true)
        .isLowestBidderChanged(true)
        .build();

        ExtensionEvaluationResult result = extensionService.evaluateAndApplyExtension(context);

        assertTrue(result.isExtensionTriggered(), "Extension should be triggered");
        assertEquals(closeTime.plus(Duration.ofMinutes(5)), result.getNewCloseTime(), "New close time should be +5 mins");
        assertFalse(result.isCappedByForcedClose(), "Should not be capped");
        assertEquals(1, rfq.getTotalExtensionsCount());
        verify(rfqRepository).save(any(RfqEntity.class));
    }

    @Test
    @DisplayName("Mode A: Should NOT extend when bid arrives outside trigger window")
    void testModeA_BidOutsideTriggerWindow_ShouldNotExtend() {
        Instant baseTime = Instant.parse("2026-09-01T18:00:00Z");
        Instant closeTime = baseTime;
        Instant forcedCloseTime = baseTime.plus(Duration.ofMinutes(30));
        Instant bidTime = baseTime.minus(Duration.ofMinutes(25));

        RfqEntity rfq = RfqEntity.builder()
        .id(1L)
        .referenceId("RFQ-TEST-001")
        .bidCloseTime(closeTime)
        .forcedBidCloseTime(forcedCloseTime)
        .triggerWindowMinutes(10)
        .extensionDurationMinutes(5)
        .extensionTriggerType(ExtensionTriggerType.ANY_BID)
        .status(AuctionStatus.ACTIVE)
        .totalExtensionsCount(0)
        .build();

        QuoteEntity quote = QuoteEntity.builder()
        .carrierName("DHL")
        .totalAmount(new BigDecimal("1000.00"))
        .supplierRank(1)
        .submittedAt(bidTime)
        .build();

        BidSubmissionContext context = BidSubmissionContext.builder()
        .rfq(rfq)
        .newQuote(quote)
        .bidTimestamp(bidTime)
        .isAnyRankChanged(true)
        .isLowestBidderChanged(true)
        .build();

        ExtensionEvaluationResult result = extensionService.evaluateAndApplyExtension(context);

        assertFalse(result.isExtensionTriggered(), "Extension should NOT be triggered outside window");
        assertEquals(closeTime, result.getNewCloseTime());
    }

    @Test
    @DisplayName("Mode C (L1 Change): Should extend ONLY when lowest bidder (L1) changes")
    void testModeC_L1Change_ShouldExtendOnlyOnL1() {
        Instant baseTime = Instant.parse("2026-09-01T18:00:00Z");
        Instant closeTime = baseTime;
        Instant forcedCloseTime = baseTime.plus(Duration.ofMinutes(30));
        Instant bidTime = baseTime.minus(Duration.ofMinutes(2));

        RfqEntity rfq = RfqEntity.builder()
        .id(1L)
        .referenceId("RFQ-TEST-002")
        .bidCloseTime(closeTime)
        .forcedBidCloseTime(forcedCloseTime)
        .triggerWindowMinutes(10)
        .extensionDurationMinutes(5)
        .extensionTriggerType(ExtensionTriggerType.L1_CHANGE)
        .status(AuctionStatus.ACTIVE)
        .totalExtensionsCount(0)
        .build();

        QuoteEntity quoteL2 = QuoteEntity.builder()
        .carrierName("FedEx")
        .totalAmount(new BigDecimal("1200.00"))
        .supplierRank(2)
        .submittedAt(bidTime)
        .build();

        BidSubmissionContext contextL2 = BidSubmissionContext.builder()
        .rfq(rfq)
        .newQuote(quoteL2)
        .bidTimestamp(bidTime)
        .isLowestBidderChanged(false)
        .build();

        ExtensionEvaluationResult resultL2 = extensionService.evaluateAndApplyExtension(contextL2);
        assertFalse(resultL2.isExtensionTriggered(), "Rank 2 bid should NOT trigger Mode C extension");

        QuoteEntity quoteL1 = QuoteEntity.builder()
        .carrierName("Maersk")
        .totalAmount(new BigDecimal("900.00"))
        .supplierRank(1)
        .submittedAt(bidTime)
        .build();

        BidSubmissionContext contextL1 = BidSubmissionContext.builder()
        .rfq(rfq)
        .newQuote(quoteL1)
        .bidTimestamp(bidTime)
        .isLowestBidderChanged(true)
        .build();

        ExtensionEvaluationResult resultL1 = extensionService.evaluateAndApplyExtension(contextL1);
        assertTrue(resultL1.isExtensionTriggered(), "New L1 bid SHOULD trigger Mode C extension");
        assertEquals(closeTime.plus(Duration.ofMinutes(5)), resultL1.getNewCloseTime());
    }

    @Test
    @DisplayName("Forced Bid Close Time Ceiling: Extension must be capped strictly at Forced Close Time")
    void testForcedCloseCeiling_ShouldCapExtensionAtForcedClose() {
        Instant baseTime = Instant.parse("2026-09-01T18:00:00Z");
        Instant closeTime = baseTime.plus(Duration.ofMinutes(27));
        Instant forcedCloseTime = baseTime.plus(Duration.ofMinutes(30));
        Instant bidTime = closeTime.minus(Duration.ofMinutes(2));

        RfqEntity rfq = RfqEntity.builder()
        .id(1L)
        .referenceId("RFQ-TEST-003")
        .bidCloseTime(closeTime)
        .forcedBidCloseTime(forcedCloseTime)
        .triggerWindowMinutes(10)
        .extensionDurationMinutes(5)
        .extensionTriggerType(ExtensionTriggerType.ANY_BID)
        .status(AuctionStatus.ACTIVE)
        .totalExtensionsCount(0)
        .build();

        QuoteEntity quote = QuoteEntity.builder()
        .carrierName("MSC")
        .totalAmount(new BigDecimal("950.00"))
        .supplierRank(1)
        .submittedAt(bidTime)
        .build();

        BidSubmissionContext context = BidSubmissionContext.builder()
        .rfq(rfq)
        .newQuote(quote)
        .bidTimestamp(bidTime)
        .isAnyRankChanged(true)
        .isLowestBidderChanged(true)
        .build();

        ExtensionEvaluationResult result = extensionService.evaluateAndApplyExtension(context);

        assertTrue(result.isExtensionTriggered(), "Extension should trigger");
        assertTrue(result.isCappedByForcedClose(), "Must be capped by forced close limit");
        assertEquals(forcedCloseTime, result.getNewCloseTime(), "New close time must cap exactly at forced close time");
    }
}
