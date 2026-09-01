package com.gocomet.auction.service;

import com.gocomet.auction.dto.request.CreateRfqRequestDto;
import com.gocomet.auction.dto.response.RfqResponseDto;
import com.gocomet.auction.entity.RfqEntity;
import com.gocomet.auction.enums.AuctionStatus;
import com.gocomet.auction.enums.ExtensionTriggerType;
import com.gocomet.auction.exception.InvalidRfqTimelineException;
import com.gocomet.auction.repository.QuoteRepository;
import com.gocomet.auction.repository.RfqRepository;
import com.gocomet.auction.service.impl.RfqManagementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RfqManagementServiceTest {

    @Mock
    private RfqRepository rfqRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private AuctionAuditLogService auditLogService;

    @Mock
    private AuctionExtensionService extensionService;

    private RfqManagementServiceImpl rfqManagementService;

    @BeforeEach
    void setUp() {
        rfqManagementService = new RfqManagementServiceImpl(
        rfqRepository, quoteRepository, auditLogService, extensionService);
    }

    @Test
    @DisplayName("Should throw InvalidRfqTimelineException when Forced Close is before or equal to Bid Close")
    void testCreateRfq_ForcedCloseBeforeBidClose_ShouldThrowException() {
        Instant startTime = Instant.parse("2026-09-01T10:00:00Z");
        Instant closeTime = Instant.parse("2026-09-01T18:00:00Z");
        Instant invalidForcedCloseTime = Instant.parse("2026-09-01T17:00:00Z");

        CreateRfqRequestDto request = CreateRfqRequestDto.builder()
        .name("Test RFQ")
        .bidStartTime(startTime)
        .bidCloseTime(closeTime)
        .forcedBidCloseTime(invalidForcedCloseTime)
        .pickupServiceDate(LocalDate.now().plusDays(10))
        .triggerWindowMinutes(10)
        .extensionDurationMinutes(5)
        .extensionTriggerType(ExtensionTriggerType.ANY_BID)
        .build();

        assertThrows(InvalidRfqTimelineException.class, () -> rfqManagementService.createRfq(request));
    }

    @Test
    @DisplayName("Should throw InvalidRfqTimelineException when Bid Close is before Bid Start")
    void testCreateRfq_BidCloseBeforeBidStart_ShouldThrowException() {
        Instant startTime = Instant.parse("2026-09-01T18:00:00Z");
        Instant invalidCloseTime = Instant.parse("2026-09-01T10:00:00Z");
        Instant forcedCloseTime = Instant.parse("2026-09-01T19:00:00Z");

        CreateRfqRequestDto request = CreateRfqRequestDto.builder()
        .name("Test RFQ")
        .bidStartTime(startTime)
        .bidCloseTime(invalidCloseTime)
        .forcedBidCloseTime(forcedCloseTime)
        .pickupServiceDate(LocalDate.now().plusDays(10))
        .build();

        assertThrows(InvalidRfqTimelineException.class, () -> rfqManagementService.createRfq(request));
    }

    @Test
    @DisplayName("Should successfully create RFQ with valid timeline")
    void testCreateRfq_ValidTimeline_Success() {
        Instant startTime = Instant.parse("2026-09-01T10:00:00Z");
        Instant closeTime = Instant.parse("2026-09-01T18:00:00Z");
        Instant forcedCloseTime = Instant.parse("2026-09-01T19:00:00Z");

        CreateRfqRequestDto request = CreateRfqRequestDto.builder()
        .name("Mumbai to NY Container Freight")
        .bidStartTime(startTime)
        .bidCloseTime(closeTime)
        .forcedBidCloseTime(forcedCloseTime)
        .pickupServiceDate(LocalDate.now().plusDays(14))
        .triggerWindowMinutes(10)
        .extensionDurationMinutes(5)
        .extensionTriggerType(ExtensionTriggerType.ANY_BID)
        .build();

        RfqEntity savedEntity = RfqEntity.builder()
        .id(100L)
        .referenceId("RFQ-2026-10001")
        .name(request.getName())
        .bidStartTime(startTime)
        .bidCloseTime(closeTime)
        .originalBidCloseTime(closeTime)
        .forcedBidCloseTime(forcedCloseTime)
        .pickupServiceDate(request.getPickupServiceDate())
        .status(AuctionStatus.ACTIVE)
        .triggerWindowMinutes(10)
        .extensionDurationMinutes(5)
        .extensionTriggerType(ExtensionTriggerType.ANY_BID)
        .totalExtensionsCount(0)
        .createdAt(Instant.now())
        .build();

        when(rfqRepository.save(any(RfqEntity.class))).thenReturn(savedEntity);

        RfqResponseDto result = rfqManagementService.createRfq(request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("RFQ-2026-10001", result.getReferenceId());
    }
}
