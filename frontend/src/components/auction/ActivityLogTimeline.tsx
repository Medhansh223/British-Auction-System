import React from "react";
import type { AuctionActivityLog } from "../../types/auction.types";
import { formatTimeOnly, formatDateTime } from "../../utils/dateFormatter";
import {
  Zap,
  CheckCircle2,
  ShieldAlert,
  PlusCircle,
  DollarSign,
} from "lucide-react";

interface ActivityLogTimelineProps {
  logs: AuctionActivityLog[];
}

export const ActivityLogTimeline: React.FC<ActivityLogTimelineProps> = ({
  logs,
}) => {
  if (!logs || logs.length === 0) {
    return (
      <div
        className="card"
        style={{ marginTop: 24, textAlign: "center", padding: "32px" }}
      >
        <p style={{ fontSize: "0.875rem", color: "var(--text-muted)" }}>
          No activity logs recorded yet. Bids and time extensions will appear
          here in real-time.
        </p>
      </div>
    );
  }

  const getEventIcon = (type: string) => {
    switch (type) {
      case "AUCTION_EXTENDED":
        return <Zap size={18} />;
      case "BID_SUBMITTED":
        return <DollarSign size={18} />;
      case "AUCTION_FORCE_CLOSED":
        return <ShieldAlert size={18} />;
      case "AUCTION_CLOSED":
        return <CheckCircle2 size={18} />;
      case "RFQ_CREATED":
      default:
        return <PlusCircle size={18} />;
    }
  };

  const getEventClass = (type: string) => {
    switch (type) {
      case "AUCTION_EXTENDED":
        return "extended";
      case "BID_SUBMITTED":
        return "bid";
      default:
        return "";
    }
  };

  return (
    <div className="card" style={{ marginTop: 24 }}>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 8,
        }}
      >
        <div>
          <h3 style={{ fontSize: "1.125rem", fontWeight: 700, color: "#fff" }}>
            Auction Activity & Extension Log
          </h3>
          <p style={{ fontSize: "0.8125rem", color: "var(--text-muted)" }}>
            Immutable audit trail of bid submissions, extensions, and exact
            extension rationale
          </p>
        </div>
        <span className="badge badge-active">{logs.length} Events</span>
      </div>

      <div className="timeline">
        {logs.map((log) => {
          const isExtended = log.eventType === "AUCTION_EXTENDED";
          return (
            <div key={log.id} className="timeline-item">
              <div className={`timeline-icon ${getEventClass(log.eventType)}`}>
                {getEventIcon(log.eventType)}
              </div>
              <div
                className="timeline-content"
                style={
                  isExtended
                    ? { borderColor: "var(--status-extended-border)" }
                    : {}
                }
              >
                <div className="timeline-header">
                  <span
                    className="timeline-type"
                    style={{
                      color: isExtended
                        ? "var(--status-extended-text)"
                        : undefined,
                    }}
                  >
                    {log.eventType.replace("_", " ")}
                  </span>
                  <span className="timeline-time">
                    {formatTimeOnly(log.createdAt)} (
                    {formatDateTime(log.createdAt)})
                  </span>
                </div>
                <p className="timeline-reason">{log.reason}</p>

                {isExtended && log.previousCloseTime && log.newCloseTime && (
                  <div
                    style={{
                      marginTop: 8,
                      fontSize: "0.75rem",
                      color: "var(--text-secondary)",
                      display: "flex",
                      gap: 16,
                      fontFamily: "JetBrains Mono, monospace",
                    }}
                  >
                    <span>
                      Prev Close: {formatTimeOnly(log.previousCloseTime)}
                    </span>
                    <span>→</span>
                    <span
                      style={{
                        color: "var(--status-extended-text)",
                        fontWeight: 700,
                      }}
                    >
                      New Close: {formatTimeOnly(log.newCloseTime)}
                    </span>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
