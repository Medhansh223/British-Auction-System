package com.gocomet.auction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "quotes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "rfq")
public class QuoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rfq_id", nullable = false)
    private RfqEntity rfq;

    @Column(name = "carrier_name", nullable = false, length = 255)
    private String carrierName;

    @Column(name = "freight_charges", nullable = false, precision = 14, scale = 2)
    private BigDecimal freightCharges;

    @Column(name = "origin_charges", nullable = false, precision = 14, scale = 2)
    private BigDecimal originCharges;

    @Column(name = "destination_charges", nullable = false, precision = 14, scale = 2)
    private BigDecimal destinationCharges;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "transit_time_days", nullable = false)
    private Integer transitTimeDays;

    @Column(name = "quote_validity", nullable = false)
    private LocalDate quoteValidity;

    @Column(name = "supplier_rank", nullable = false)
    private Integer supplierRank;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @PrePersist
    protected void onCreate() {
        if (this.submittedAt == null) {
            this.submittedAt = Instant.now();
        }
        if (this.totalAmount == null) {
            this.totalAmount = calculateTotal(this.freightCharges, this.originCharges, this.destinationCharges);
        }
    }

    public static BigDecimal calculateTotal(BigDecimal freight, BigDecimal origin, BigDecimal destination) {
        BigDecimal total = BigDecimal.ZERO;
        if (freight != null) total = total.add(freight);
        if (origin != null) total = total.add(origin);
        if (destination != null) total = total.add(destination);
        return total;
    }
}
