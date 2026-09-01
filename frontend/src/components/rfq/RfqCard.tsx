import React from "react";
import type { Rfq } from "../../types/auction.types";
import { StatusBadge } from "../common/StatusBadge";
import { formatCurrency } from "../../utils/currencyFormatter";
import { formatDateTime, formatDateOnly } from "../../utils/dateFormatter";
import { Trophy, ArrowRight, Layers } from "lucide-react";

interface RfqCardProps {
  rfq: Rfq;
  onSelect: (rfq: Rfq) => void;
}

export const RfqCard: React.FC<RfqCardProps> = ({ rfq, onSelect }) => {
  return (
    <div className="card card-clickable" onClick={() => onSelect(rfq)}>
      <div className="rfq-card-header">
        <div>
          <div className="rfq-ref">{rfq.referenceId}</div>
          <h3 className="rfq-title">{rfq.name}</h3>
        </div>
        <StatusBadge
          status={rfq.status}
          isExtended={rfq.totalExtensionsCount > 0}
        />
      </div>

      {rfq.currentLowestBid ? (
        <div className="rfq-lowest-banner">
          <div className="lowest-label">
            <span style={{ display: "flex", alignItems: "center", gap: 4 }}>
              <Trophy size={14} /> Current L1 (
              {rfq.currentLowestCarrier || "Supplier"})
            </span>
          </div>
          <div className="lowest-price">
            {formatCurrency(rfq.currentLowestBid)}
          </div>
        </div>
      ) : (
        <div
          className="rfq-lowest-banner"
          style={{
            background: "rgba(255,255,255,0.04)",
            borderColor: "var(--border-subtle)",
          }}
        >
          <div className="lowest-label" style={{ color: "var(--text-muted)" }}>
            No Bids Submitted Yet
          </div>
          <div
            className="lowest-price"
            style={{ color: "var(--text-muted)", fontSize: "0.875rem" }}
          >
            Open for Quotes
          </div>
        </div>
      )}

      <div className="rfq-meta-grid">
        <div className="meta-item">
          <span className="meta-label">Bid Close Time</span>
          <span className="meta-value">{formatDateTime(rfq.bidCloseTime)}</span>
        </div>
        <div className="meta-item">
          <span className="meta-label">Forced Close (Hard Stop)</span>
          <span
            className="meta-value"
            style={{ color: "var(--status-forced-text)" }}
          >
            {formatDateTime(rfq.forcedBidCloseTime)}
          </span>
        </div>
        <div className="meta-item">
          <span className="meta-label">Pickup Date</span>
          <span className="meta-value">
            {formatDateOnly(rfq.pickupServiceDate)}
          </span>
        </div>
        <div className="meta-item">
          <span className="meta-label">Rule / Extensions</span>
          <span className="meta-value">
            {rfq.extensionTriggerType} ({rfq.totalExtensionsCount}x)
          </span>
        </div>
      </div>

      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          paddingTop: 12,
          borderTop: "1px solid var(--border-subtle)",
        }}
      >
        <span
          style={{
            fontSize: "0.8125rem",
            color: "var(--text-muted)",
            display: "flex",
            alignItems: "center",
            gap: 4,
          }}
        >
          <Layers size={14} /> {rfq.totalQuotesCount} Quotes Received
        </span>
        <button className="btn btn-outline btn-sm">
          Enter Room <ArrowRight size={14} />
        </button>
      </div>
    </div>
  );
};
