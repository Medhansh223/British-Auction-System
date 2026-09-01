package com.gocomet.auction.dto.request;

import com.gocomet.auction.enums.ExtensionTriggerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload for creating an RFQ with British Auction configurations")
public class CreateRfqRequestDto {

    @NotBlank(message = "RFQ name / description cannot be blank")
    @Size(max = 255, message = "RFQ name cannot exceed 255 characters")
    @Schema(description = "RFQ Name or Shipment Title", example = "Mumbai to New York 10x 40ft FCL Shipping")
    private String name;

    @Schema(description = "Custom reference ID (Optional, auto-generated if blank)", example = "RFQ-2026-89421")
    private String referenceId;

    @NotNull(message = "Bid start time is required")
    @Schema(description = "Bid Start Time (ISO-8601 UTC)", example = "2026-09-01T10:00:00Z")
    private Instant bidStartTime;

    @NotNull(message = "Bid close time is required")
    @Schema(description = "Bid Close Time (ISO-8601 UTC)", example = "2026-09-01T18:00:00Z")
    private Instant bidCloseTime;

    @NotNull(message = "Forced bid close time is required")
    @Schema(description = "Hard Stop Close Time (Must be strictly > bidCloseTime)", example = "2026-09-01T19:00:00Z")
    private Instant forcedBidCloseTime;

    @NotNull(message = "Pickup / service date is required")
    @Schema(description = "Service / Cargo Pickup Date", example = "2026-09-15")
    private LocalDate pickupServiceDate;

    @NotNull(message = "Trigger window (X minutes) is required")
    @Min(value = 1, message = "Trigger window must be at least 1 minute")
    @Max(value = 120, message = "Trigger window cannot exceed 120 minutes")
    @Schema(description = "Trigger Window X in minutes before close time", example = "10")
    @Builder.Default
    private Integer triggerWindowMinutes = 10;

    @NotNull(message = "Extension duration (Y minutes) is required")
    @Min(value = 1, message = "Extension duration must be at least 1 minute")
    @Max(value = 60, message = "Extension duration cannot exceed 60 minutes")
    @Schema(description = "Extension Duration Y in minutes added on trigger", example = "5")
    @Builder.Default
    private Integer extensionDurationMinutes = 5;

    @NotNull(message = "Extension trigger type is required")
    @Schema(description = "Trigger strategy: ANY_BID, ANY_RANK_CHANGE, or L1_CHANGE", example = "ANY_BID")
    @Builder.Default
    private ExtensionTriggerType extensionTriggerType = ExtensionTriggerType.ANY_BID;
}
