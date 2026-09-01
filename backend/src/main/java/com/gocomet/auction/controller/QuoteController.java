package com.gocomet.auction.controller;

import com.gocomet.auction.constant.RfqConstants;
import com.gocomet.auction.dto.request.SubmitQuoteRequestDto;
import com.gocomet.auction.dto.response.QuoteResponseDto;
import com.gocomet.auction.service.QuoteSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(RfqConstants.QUOTES_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "Quote / Bidding Management", description = "Endpoints for submitting supplier bids and querying quotes")
public class QuoteController {

    private final QuoteSubmissionService quoteSubmissionService;

    @PostMapping
    @Operation(
            summary = "Submit a Quote / Bid on an RFQ",
            description = "Accepts freight, origin, and destination charges, calculates total, re-indexes rankings (L1, L2...), and triggers British Auction extensions if inside trigger window."
    )
    @ApiResponse(responseCode = "201", description = "Quote placed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid quote data or auction not started")
    @ApiResponse(responseCode = "409", description = "Auction is closed or force-closed")
    @ApiResponse(responseCode = "404", description = "RFQ ID not found")
    public ResponseEntity<QuoteResponseDto> submitQuote(
            @Parameter(description = "ID of the target RFQ", required = true)
            @PathVariable Long rfqId,
            @Valid @RequestBody SubmitQuoteRequestDto request) {

        log.info("REST API Request: POST {} for RFQ ID {} from carrier '{}'",
                RfqConstants.QUOTES_ENDPOINT, rfqId, request.getCarrierName());

        QuoteResponseDto response = quoteSubmissionService.submitQuote(rfqId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Get All Quotes for an RFQ",
            description = "Returns all submitted quotes sorted by price (Lowest/L1 first)."
    )
    public ResponseEntity<List<QuoteResponseDto>> getQuotesForRfq(
            @Parameter(description = "ID of the target RFQ", required = true)
            @PathVariable Long rfqId) {

        log.debug("REST API Request: GET {} for RFQ ID {}", RfqConstants.QUOTES_ENDPOINT, rfqId);
        List<QuoteResponseDto> quotes = quoteSubmissionService.getQuotesForRfq(rfqId);
        return ResponseEntity.ok(quotes);
    }
}
