import React from "react";
import type { Rfq } from "../../types/auction.types";
import { StatusBadge } from "../common/StatusBadge";
import { CountdownTimer } from "../common/CountdownTimer";
import { formatDateTime, formatDateOnly } from "../../utils/dateFormatter";
import { ArrowLeft, Calendar, Ship } from "lucide-react";

interface AuctionHeaderProps {
  rfq: Rfq;
  onBack: () => void;
  onRefresh: () => void;
}

export const AuctionHeader: React.FC<AuctionHeaderProps> = ({
  rfq,
  onBack,
  onRefresh,
}) => {
  return (
    <div style={{ marginBottom: 24 }}>
      <button
        className="btn btn-outline btn-sm"
        onClick={onBack}
        style={{ marginBottom: 16 }}
      >
        <ArrowLeft size={16} /> Back to Auctions
      </button>

      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "flex-start",
          flexWrap: "wrap",
          gap: 16,
          marginBottom: 20,
        }}
      >
        <div>
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: 10,
              marginBottom: 4,
            }}
          >
            <span className="rfq-ref" style={{ fontSize: "0.875rem" }}>
              {rfq.referenceId}
            </span>
            <StatusBadge
              status={rfq.status}
              isExtended={rfq.totalExtensionsCount > 0}
            />
          </div>
          <h1
            style={{
              fontSize: "1.75rem",
              fontWeight: 800,
              color: "#fff",
              letterSpacing: "-0.02em",
            }}
          >
            {rfq.name}
          </h1>
          <div
            style={{
              display: "flex",
              gap: 16,
              marginTop: 8,
              fontSize: "0.8125rem",
              color: "var(--text-muted)",
              flexWrap: "wrap",
            }}
          >
            <span style={{ display: "flex", alignItems: "center", gap: 4 }}>
              <Calendar size={14} /> Pickup Date:{" "}
              <strong>{formatDateOnly(rfq.pickupServiceDate)}</strong>
            </span>
            <span style={{ display: "flex", alignItems: "center", gap: 4 }}>
              <Ship size={14} /> Started: {formatDateTime(rfq.bidStartTime)}
            </span>
          </div>
        </div>
      </div>

      {}
      <div className="timer-container">
        <CountdownTimer
          targetDate={rfq.bidCloseTime}
          label="Current Bid Close Time (Dynamic)"
          subtext={`Closes: ${formatDateTime(rfq.bidCloseTime)}`}
          isTriggerWindowActive={rfq.isInsideTriggerWindow}
          onExpire={onRefresh}
        />
        <CountdownTimer
          targetDate={rfq.forcedBidCloseTime}
          label="Forced Bid Close Time (Absolute Hard Stop)"
          subtext={`Ceiling: ${formatDateTime(rfq.forcedBidCloseTime)}`}
          isForcedClose={true}
          onExpire={onRefresh}
        />
      </div>
    </div>
  );
};
