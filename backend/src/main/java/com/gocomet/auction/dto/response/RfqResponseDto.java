package com.gocomet.auction.dto.response;

import com.gocomet.auction.enums.AuctionStatus;
import com.gocomet.auction.enums.ExtensionTriggerType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full RFQ details including current lowest bid, quotes, and audit logs")
public class RfqResponseDto {

    private Long id;
    private String referenceId;
    private String name;
    private Instant bidStartTime;
    private Instant bidCloseTime;
    private Instant originalBidCloseTime;
    private Instant forcedBidCloseTime;
    private LocalDate pickupServiceDate;
    private AuctionStatus status;
    private Integer triggerWindowMinutes;
    private Integer extensionDurationMinutes;
    private ExtensionTriggerType extensionTriggerType;
    private Integer totalExtensionsCount;
    private Instant createdAt;
    private Instant updatedAt;

    private BigDecimal currentLowestBid;
    private String currentLowestCarrier;
    private Integer totalQuotesCount;
    private Long remainingSecondsToClose;
    private Long remainingSecondsToForcedClose;
    private Boolean isInsideTriggerWindow;
    private Boolean isForceCloseImminent;

    private List<QuoteResponseDto> quotes;
    private List<AuctionActivityLogResponseDto> activityLogs;
}
