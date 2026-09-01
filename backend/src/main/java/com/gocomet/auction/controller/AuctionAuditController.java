package com.gocomet.auction.controller;

import com.gocomet.auction.constant.RfqConstants;
import com.gocomet.auction.dto.response.AuctionActivityLogResponseDto;
import com.gocomet.auction.service.AuctionAuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(RfqConstants.AUDIT_LOGS_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "Auction Audit Logs", description = "Endpoints for retrieving auction event timelines and extension reasons")
public class AuctionAuditController {

    private final AuctionAuditLogService auditLogService;

    @GetMapping
    @Operation(
            summary = "Get Chronological Activity & Extension Logs",
            description = "Returns audit logs with detailed reasons for time extensions, bids placed, and status changes."
    )
    public ResponseEntity<List<AuctionActivityLogResponseDto>> getLogsForRfq(
            @Parameter(description = "ID of the target RFQ", required = true)
            @PathVariable Long rfqId) {

        log.debug("REST API Request: GET {} for RFQ ID {}", RfqConstants.AUDIT_LOGS_ENDPOINT, rfqId);
        List<AuctionActivityLogResponseDto> logs = auditLogService.getLogsForRfq(rfqId);
        return ResponseEntity.ok(logs);
    }
}
