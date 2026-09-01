package com.gocomet.auction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Quote details and current ranking")
public class QuoteResponseDto {

    private Long id;
    private Long rfqId;
    private String carrierName;
    private BigDecimal freightCharges;
    private BigDecimal originCharges;
    private BigDecimal destinationCharges;
    private BigDecimal totalAmount;
    private Integer transitTimeDays;
    private LocalDate quoteValidity;
    private Integer supplierRank;
    private String rankLabel;
    private Instant submittedAt;
}
