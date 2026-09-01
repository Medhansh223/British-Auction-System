package com.gocomet.auction.dto.event;

import com.gocomet.auction.entity.QuoteEntity;
import com.gocomet.auction.entity.RfqEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidSubmissionContext {

    private RfqEntity rfq;
    private QuoteEntity newQuote;
    private List<QuoteEntity> previousQuotes;
    private Integer previousRank;
    private Integer newRank;
    private boolean isLowestBidderChanged;
    private boolean isAnyRankChanged;
    private Instant bidTimestamp;
}
