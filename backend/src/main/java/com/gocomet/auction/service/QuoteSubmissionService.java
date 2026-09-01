package com.gocomet.auction.service;

import com.gocomet.auction.dto.request.SubmitQuoteRequestDto;
import com.gocomet.auction.dto.response.QuoteResponseDto;

import java.util.List;

public interface QuoteSubmissionService {

    QuoteResponseDto submitQuote(Long rfqId, SubmitQuoteRequestDto request);

    List<QuoteResponseDto> getQuotesForRfq(Long rfqId);
}
