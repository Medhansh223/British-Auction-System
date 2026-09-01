package com.gocomet.auction.entity;

import com.gocomet.auction.enums.AuctionStatus;
import com.gocomet.auction.enums.ExtensionTriggerType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rfqs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"quotes", "activityLogs"})
public class RfqEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_id", nullable = false, unique = true, length = 64)
    private String referenceId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "bid_start_time", nullable = false)
    private Instant bidStartTime;

    @Column(name = "bid_close_time", nullable = false)
    private Instant bidCloseTime;

    @Column(name = "original_bid_close_time", nullable = false)
    private Instant originalBidCloseTime;

    @Column(name = "forced_bid_close_time", nullable = false)
    private Instant forcedBidCloseTime;

    @Column(name = "pickup_service_date", nullable = false)
    private LocalDate pickupServiceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AuctionStatus status;

    @Column(name = "trigger_window_minutes", nullable = false)
    private Integer triggerWindowMinutes;

    @Column(name = "extension_duration_minutes", nullable = false)
    private Integer extensionDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "extension_trigger_type", nullable = false, length = 32)
    private ExtensionTriggerType extensionTriggerType;

    @Column(name = "total_extensions_count", nullable = false)
    @Builder.Default
    private Integer totalExtensionsCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "rfq", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("totalAmount ASC, submittedAt ASC")
    @Builder.Default
    private List<QuoteEntity> quotes = new ArrayList<>();

    @OneToMany(mappedBy = "rfq", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    @Builder.Default
    private List<AuctionActivityLogEntity> activityLogs = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.originalBidCloseTime == null) {
            this.originalBidCloseTime = this.bidCloseTime;
        }
        if (this.totalExtensionsCount == null) {
            this.totalExtensionsCount = 0;
        }
        if (this.status == null) {
            this.status = AuctionStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
