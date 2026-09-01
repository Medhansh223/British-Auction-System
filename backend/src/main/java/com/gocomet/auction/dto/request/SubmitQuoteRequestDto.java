package com.gocomet.auction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload for submitting a quote/bid on an RFQ")
public class SubmitQuoteRequestDto {

    @NotBlank(message = "Carrier / Supplier name cannot be blank")
    @Size(max = 255, message = "Carrier name cannot exceed 255 characters")
    @Schema(description = "Carrier / Supplier Name", example = "Maersk Ocean Logistics")
    private String carrierName;

    @NotNull(message = "Freight charges are required")
    @DecimalMin(value = "0.00", message = "Freight charges cannot be negative")
    @Schema(description = "Base Ocean/Air Freight Charges (USD)", example = "1200.00")
    private BigDecimal freightCharges;

    @NotNull(message = "Origin charges are required")
    @DecimalMin(value = "0.00", message = "Origin charges cannot be negative")
    @Schema(description = "Origin / Port Handling Charges (USD)", example = "150.00")
    private BigDecimal originCharges;

    @NotNull(message = "Destination charges are required")
    @DecimalMin(value = "0.00", message = "Destination charges cannot be negative")
    @Schema(description = "Destination Port / Clearance Charges (USD)", example = "200.00")
    private BigDecimal destinationCharges;

    @NotNull(message = "Transit time is required")
    @Min(value = 1, message = "Transit time must be at least 1 day")
    @Schema(description = "Estimated Transit Time in Days", example = "14")
    private Integer transitTimeDays;

    @NotNull(message = "Quote validity date is required")
    @FutureOrPresent(message = "Quote validity date must be today or in the future")
    @Schema(description = "Validity Expiration Date of the Quote", example = "2026-09-30")
    private LocalDate quoteValidity;
}
