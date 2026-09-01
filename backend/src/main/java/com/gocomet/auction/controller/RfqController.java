package com.gocomet.auction.controller;

import com.gocomet.auction.constant.RfqConstants;
import com.gocomet.auction.dto.request.CreateRfqRequestDto;
import com.gocomet.auction.dto.response.RfqResponseDto;
import com.gocomet.auction.enums.AuctionStatus;
import com.gocomet.auction.service.RfqManagementService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(RfqConstants.RFQS_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "RFQ Management", description = "Endpoints for managing British Auction RFQs")
public class RfqController {

    private final RfqManagementService rfqManagementService;

    @PostMapping
    @Operation(
            summary = "Create an RFQ with British Auction configuration",
            description = "Validates timeline constraints (Forced Close > Close > Start) and provisions a new British Auction RFQ."
    )
    @ApiResponse(responseCode = "201", description = "RFQ created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid timeline or configuration payload")
    public ResponseEntity<RfqResponseDto> createRfq(@Valid @RequestBody CreateRfqRequestDto request) {
        log.info("REST API Request: POST {} for name '{}'", RfqConstants.RFQS_ENDPOINT, request.getName());
        RfqResponseDto created = rfqManagementService.createRfq(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "List all RFQ Auctions",
            description = "Retrieves all RFQs with live countdowns, lowest bids, and status summaries."
    )
    public ResponseEntity<List<RfqResponseDto>> getAllRfqs(
            @Parameter(description = "Optional filter by auction status: ACTIVE, EXTENDED, CLOSED, FORCE_CLOSED, DRAFT")
            @RequestParam(required = false) AuctionStatus status) {

        log.debug("REST API Request: GET {} statusFilter={}", RfqConstants.RFQS_ENDPOINT, status);
        List<RfqResponseDto> rfqs = rfqManagementService.getAllRfqs(status);
        return ResponseEntity.ok(rfqs);
    }

    @GetMapping("/{rfqId}")
    @Operation(
            summary = "Get RFQ Details by ID",
            description = "Returns complete RFQ details, quotes leaderboard, and chronological activity logs."
    )
    @ApiResponse(responseCode = "200", description = "RFQ details retrieved")
    @ApiResponse(responseCode = "404", description = "RFQ ID not found")
    public ResponseEntity<RfqResponseDto> getRfqById(
            @Parameter(description = "Numeric ID of the RFQ", required = true)
            @PathVariable Long rfqId) {

        log.debug("REST API Request: GET {}/{}", RfqConstants.RFQS_ENDPOINT, rfqId);
        RfqResponseDto rfq = rfqManagementService.getRfqById(rfqId);
        return ResponseEntity.ok(rfq);
    }

    @GetMapping("/reference/{referenceId}")
    @Operation(
            summary = "Get RFQ Details by Business Reference ID",
            description = "Returns RFQ details using its business reference (e.g. RFQ-2026-001)"
    )
    public ResponseEntity<RfqResponseDto> getRfqByReferenceId(
            @Parameter(description = "Reference ID string", required = true)
            @PathVariable String referenceId) {

        log.debug("REST API Request: GET {}/reference/{}", RfqConstants.RFQS_ENDPOINT, referenceId);
        RfqResponseDto rfq = rfqManagementService.getRfqByReferenceId(referenceId);
        return ResponseEntity.ok(rfq);
    }
}
