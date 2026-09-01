package com.gocomet.auction.service;

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
import com.gocomet.auction.service.impl.QuoteSubmissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuoteSubmissionServiceTest {

    @Mock
    private RfqRepository rfqRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private SupplierRankingService rankingService;

    @Mock
    private AuctionExtensionService extensionService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private QuoteSubmissionServiceImpl quoteSubmissionService;

    private RfqEntity validRfq;
    private SubmitQuoteRequestDto validRequest;

    @BeforeEach
    void setUp() {
        validRfq = RfqEntity.builder()
                .id(1L)
                .referenceId("RFQ-1001")
                .bidStartTime(Instant.now().minusSeconds(60))
                .bidCloseTime(Instant.now().plusSeconds(600))
                .forcedBidCloseTime(Instant.now().plusSeconds(1200))
                .status(AuctionStatus.ACTIVE)
                .build();

        validRequest = new SubmitQuoteRequestDto();
        validRequest.setCarrierName("Fast Freight Inc");
        validRequest.setFreightCharges(new BigDecimal("1000.00"));
        validRequest.setOriginCharges(new BigDecimal("100.00"));
        validRequest.setDestinationCharges(new BigDecimal("200.00"));
        validRequest.setTransitTimeDays(14);
        validRequest.setQuoteValidity(LocalDate.now().plusDays(30));
    }

    @Test
    void submitQuote_ValidQuote_SavesQuoteAndPublishesEvent() {

        when(rfqRepository.findById(1L)).thenReturn(Optional.of(validRfq));
        when(quoteRepository.findByRfqIdOrderByTotalAmountAscSubmittedAtAsc(1L)).thenReturn(Collections.emptyList());

        BidSubmissionContext context = BidSubmissionContext.builder().build();
        when(rankingService.calculateAndAssignRankings(eq(validRfq), any(QuoteEntity.class), anyList())).thenReturn(context);

        QuoteEntity savedQuote = QuoteEntity.builder()
                .id(100L)
                .rfq(validRfq)
                .carrierName(validRequest.getCarrierName())
                .freightCharges(validRequest.getFreightCharges())
                .originCharges(validRequest.getOriginCharges())
                .destinationCharges(validRequest.getDestinationCharges())
                .totalAmount(new BigDecimal("1300.00"))
                .supplierRank(1)
                .submittedAt(Instant.now())
                .build();
        when(quoteRepository.save(any(QuoteEntity.class))).thenReturn(savedQuote);

        ExtensionEvaluationResult extensionResult = ExtensionEvaluationResult.builder().extensionTriggered(false).build();
        when(extensionService.evaluateAndApplyExtension(any())).thenReturn(extensionResult);

    
        QuoteResponseDto response = quoteSubmissionService.submitQuote(1L, validRequest);


        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(new BigDecimal("1300.00"), response.getTotalAmount());
        assertEquals(1, response.getSupplierRank());
        assertEquals("L1", response.getRankLabel());

        verify(quoteRepository, times(1)).save(any(QuoteEntity.class));
        verify(rankingService, times(1)).reindexAllQuotesForRfq(1L);
        verify(eventPublisher, times(1)).publishEvent(any(BidSubmittedEvent.class));
    }

    @Test
    void submitQuote_RfqNotFound_ThrowsResourceNotFoundException() {
        
        when(rfqRepository.findById(999L)).thenReturn(Optional.empty());


        assertThrows(ResourceNotFoundException.class, () -> {
            quoteSubmissionService.submitQuote(999L, validRequest);
        });
    }

    @Test
    void submitQuote_AuctionNotStarted_ThrowsAuctionNotStartedException() {
        
        validRfq.setBidStartTime(Instant.now().plusSeconds(3600)); // Starts in 1 hour
        when(rfqRepository.findById(1L)).thenReturn(Optional.of(validRfq));

        
        assertThrows(AuctionNotStartedException.class, () -> {
            quoteSubmissionService.submitQuote(1L, validRequest);
        });
    }

    @Test
    void submitQuote_AuctionClosed_ThrowsAuctionClosedException() {

        validRfq.setBidCloseTime(Instant.now().minusSeconds(60)); // Closed 1 min ago
        when(rfqRepository.findById(1L)).thenReturn(Optional.of(validRfq));

        
        assertThrows(AuctionClosedException.class, () -> {
            quoteSubmissionService.submitQuote(1L, validRequest);
        });
        assertEquals(AuctionStatus.CLOSED, validRfq.getStatus());
        verify(rfqRepository, times(1)).save(validRfq);
    }

    @Test
    void submitQuote_AuctionForceClosed_ThrowsAuctionClosedException() {

        validRfq.setForcedBidCloseTime(Instant.now().minusSeconds(60)); // Force closed 1 min ago

        validRfq.setBidCloseTime(Instant.now().plusSeconds(600));
        
        when(rfqRepository.findById(1L)).thenReturn(Optional.of(validRfq));

        assertThrows(AuctionClosedException.class, () -> {
            quoteSubmissionService.submitQuote(1L, validRequest);
        });
        assertEquals(AuctionStatus.FORCE_CLOSED, validRfq.getStatus());
        verify(rfqRepository, times(1)).save(validRfq);
    }

    @Test
    void submitQuote_NegativeFreight_ThrowsQuoteValidationException() {

        when(rfqRepository.findById(1L)).thenReturn(Optional.of(validRfq));
        validRequest.setFreightCharges(new BigDecimal("-100.00"));

        assertThrows(QuoteValidationException.class, () -> {
            quoteSubmissionService.submitQuote(1L, validRequest);
        });
    }
    
    @Test
    void submitQuote_MissingCarrierName_ThrowsQuoteValidationException() {

        when(rfqRepository.findById(1L)).thenReturn(Optional.of(validRfq));
        validRequest.setCarrierName("");

        assertThrows(QuoteValidationException.class, () -> {
            quoteSubmissionService.submitQuote(1L, validRequest);
        });
    }
}
