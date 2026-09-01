package com.gocomet.auction.service;

import com.gocomet.auction.dto.event.BidSubmissionContext;
import com.gocomet.auction.entity.QuoteEntity;
import com.gocomet.auction.entity.RfqEntity;
import com.gocomet.auction.repository.QuoteRepository;
import com.gocomet.auction.service.impl.SupplierRankingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SupplierRankingServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    private SupplierRankingServiceImpl rankingService;

    @BeforeEach
    void setUp() {
        rankingService = new SupplierRankingServiceImpl(quoteRepository);
    }

    @Test
    @DisplayName("Should rank quotes by lowest price first (Reverse Auction L1, L2, L3)")
    void testRankCalculation_ReverseAuctionOrdering() {
        RfqEntity rfq = RfqEntity.builder().id(1L).build();

        QuoteEntity existingL1 = QuoteEntity.builder()
        .id(1L)
        .carrierName("Maersk")
        .totalAmount(new BigDecimal("1200.00"))
        .supplierRank(1)
        .submittedAt(Instant.parse("2026-09-01T10:00:00Z"))
        .build();

        QuoteEntity existingL2 = QuoteEntity.builder()
        .id(2L)
        .carrierName("CMA CGM")
        .totalAmount(new BigDecimal("1400.00"))
        .supplierRank(2)
        .submittedAt(Instant.parse("2026-09-01T10:05:00Z"))
        .build();

        List<QuoteEntity> existing = List.of(existingL1, existingL2);

        QuoteEntity newQuote = QuoteEntity.builder()
        .carrierName("DHL")
        .totalAmount(new BigDecimal("1000.00"))
        .submittedAt(Instant.parse("2026-09-01T10:10:00Z"))
        .build();

        BidSubmissionContext context = rankingService.calculateAndAssignRankings(rfq, newQuote, existing);

        assertEquals(1, newQuote.getSupplierRank(), "New quote should be rank 1 (L1)");
        assertTrue(context.isLowestBidderChanged(), "Lowest bidder should be marked as changed");
        assertTrue(context.isAnyRankChanged(), "Ranking should be marked as changed");
    }

    @Test
    @DisplayName("First quote submitted should automatically become L1")
    void testFirstQuote_BecomesL1() {
        RfqEntity rfq = RfqEntity.builder().id(1L).build();

        QuoteEntity firstQuote = QuoteEntity.builder()
        .carrierName("Hapag-Lloyd")
        .totalAmount(new BigDecimal("1500.00"))
        .submittedAt(Instant.now())
        .build();

        BidSubmissionContext context = rankingService.calculateAndAssignRankings(rfq, firstQuote, new ArrayList<>());

        assertEquals(1, firstQuote.getSupplierRank());
        assertTrue(context.isLowestBidderChanged());
        assertTrue(context.isAnyRankChanged());
    }
}
