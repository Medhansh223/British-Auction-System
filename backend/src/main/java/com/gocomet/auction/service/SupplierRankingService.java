package com.gocomet.auction.service;

import com.gocomet.auction.dto.event.BidSubmissionContext;
import com.gocomet.auction.entity.QuoteEntity;
import com.gocomet.auction.entity.RfqEntity;

import java.util.List;

public interface SupplierRankingService {

    BidSubmissionContext calculateAndAssignRankings(
    RfqEntity rfq, QuoteEntity newQuote, List<QuoteEntity> existingQuotes);

    void reindexAllQuotesForRfq(Long rfqId);
}
