package com.gocomet.auction.dto.response;

import com.gocomet.auction.enums.ActivityEventType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Auction activity and audit log item")
public class AuctionActivityLogResponseDto {

    private Long id;
    private Long rfqId;
    private Long quoteId;
    private ActivityEventType eventType;
    private String reason;
    private Instant previousCloseTime;
    private Instant newCloseTime;
    private String carrierName;
    private BigDecimal bidAmount;
    private Instant createdAt;
}
