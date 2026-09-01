package com.gocomet.auction.service;

import com.gocomet.auction.dto.response.AuctionActivityLogResponseDto;
import com.gocomet.auction.entity.AuctionActivityLogEntity;
import com.gocomet.auction.entity.QuoteEntity;
import com.gocomet.auction.entity.RfqEntity;
import com.gocomet.auction.enums.ActivityEventType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface AuctionAuditLogService {

    AuctionActivityLogEntity logEvent(
        RfqEntity rfq,
        QuoteEntity quote,
        ActivityEventType eventType,
        String reason,
        Instant previousCloseTime,
        Instant newCloseTime,
        String carrierName,
        BigDecimal amount
);

    List<AuctionActivityLogResponseDto> getLogsForRfq(Long rfqId);
}
