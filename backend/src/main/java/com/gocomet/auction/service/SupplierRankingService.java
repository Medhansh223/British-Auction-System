package com.gocomet.auction.service;

import com.gocomet.auction.constant.LogMessageConstants;
import com.gocomet.auction.constant.RfqConstants;
import com.gocomet.auction.dto.event.BidSubmissionContext;
import com.gocomet.auction.entity.QuoteEntity;
import com.gocomet.auction.entity.RfqEntity;
import com.gocomet.auction.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierRankingService {

    private final QuoteRepository quoteRepository;
    public BidSubmissionContext calculateAndAssignRankings(
    RfqEntity rfq, QuoteEntity newQuote, List<QuoteEntity> existingQuotes) {

        Map<String, QuoteEntity> previousBestPerCarrier = getBestQuotePerCarrier(existingQuotes);
        QuoteEntity previousL1 = existingQuotes.isEmpty() ? null : existingQuotes.get(0);

        List<QuoteEntity> allQuotes = new ArrayList<>(existingQuotes);
        allQuotes.add(newQuote);
        allQuotes.sort(Comparator.comparing(QuoteEntity::getTotalAmount)
        .thenComparing(QuoteEntity::getSubmittedAt));

        int newQuoteRank = 1;
        for (int i = 0; i < allQuotes.size(); i++) {
            QuoteEntity q = allQuotes.get(i);
            int rank = i + 1;
            q.setSupplierRank(rank);
            if (q == newQuote) {
                newQuoteRank = rank;
            }
        }
        newQuote.setSupplierRank(newQuoteRank);

        boolean lowestBidderChanged = false;
        if (newQuoteRank == RfqConstants.TOP_RANK_L1) {
            if (previousL1 == null || newQuote.getTotalAmount().compareTo(previousL1.getTotalAmount()) < 0) {
                lowestBidderChanged = true;
            }
        }

        boolean anyRankChanged = false;
        if (existingQuotes.isEmpty()) {
            anyRankChanged = true;
        } else if (newQuoteRank < allQuotes.size()) {

            anyRankChanged = true;
        }

        log.debug("Rank evaluation for carrier '{}' in RFQ {}: assigned L{}, isL1Changed={}, isAnyRankChanged={}",
        newQuote.getCarrierName(), rfq.getId(), newQuoteRank, lowestBidderChanged, anyRankChanged);

        return BidSubmissionContext.builder()
        .rfq(rfq)
        .newQuote(newQuote)
        .previousQuotes(existingQuotes)
        .newRank(newQuoteRank)
        .isLowestBidderChanged(lowestBidderChanged)
        .isAnyRankChanged(anyRankChanged)
        .bidTimestamp(newQuote.getSubmittedAt() != null ? newQuote.getSubmittedAt() : Instant.now())
        .build();
    }
    @Transactional
    public void reindexAllQuotesForRfq(Long rfqId) {
        List<QuoteEntity> quotes = quoteRepository.findByRfqIdOrderByTotalAmountAscSubmittedAtAsc(rfqId);
        for (int i = 0; i < quotes.size(); i++) {
            quotes.get(i).setSupplierRank(i + 1);
        }
        quoteRepository.saveAll(quotes);
        log.debug(LogMessageConstants.LOG_RANKINGS_RECALCULATED, rfqId, quotes.size());
    }

    private Map<String, QuoteEntity> getBestQuotePerCarrier(List<QuoteEntity> quotes) {
        Map<String, QuoteEntity> bestMap = new HashMap<>();
        for (QuoteEntity q : quotes) {
            String carrier = q.getCarrierName().trim().toLowerCase();
            if (!bestMap.containsKey(carrier) || q.getTotalAmount().compareTo(bestMap.get(carrier).getTotalAmount()) < 0) {
                bestMap.put(carrier, q);
            }
        }
        return bestMap;
    }
}
