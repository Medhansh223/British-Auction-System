import React from "react";
import type { Quote } from "../../types/auction.types";
import { formatCurrency } from "../../utils/currencyFormatter";
import { formatTimeOnly, formatDateOnly } from "../../utils/dateFormatter";
import { Trophy, Inbox } from "lucide-react";

interface LeaderboardTableProps {
  quotes: Quote[];
}

export const LeaderboardTable: React.FC<LeaderboardTableProps> = ({
  quotes,
}) => {
  if (!quotes || quotes.length === 0) {
    return (
      <div
        className="card"
        style={{ textAlign: "center", padding: "48px 24px" }}
      >
        <div
          style={{
            display: "inline-flex",
            padding: 16,
            background: "rgba(255,255,255,0.04)",
            borderRadius: "50%",
            marginBottom: 16,
          }}
        >
          <Inbox size={36} className="text-slate-400" />
        </div>
        <h3
          style={{
            fontSize: "1.125rem",
            fontWeight: 600,
            color: "#fff",
            marginBottom: 6,
          }}
        >
          No Quotes Submitted Yet
        </h3>
        <p
          style={{
            fontSize: "0.875rem",
            color: "var(--text-muted)",
            maxWidth: 380,
            margin: "0 auto",
          }}
        >
          This auction is live. Use the Quote Submission form to place
          competitive quotes and discover real-time rankings.
        </p>
      </div>
    );
  }

  return (
    <div className="card">
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 16,
        }}
      >
        <div>
          <h3 style={{ fontSize: "1.125rem", fontWeight: 700, color: "#fff" }}>
            Supplier Leaderboard & Bids
          </h3>
          <p style={{ fontSize: "0.8125rem", color: "var(--text-muted)" }}>
            Real-time price ranking (Lowest total price holds Rank L1)
          </p>
        </div>
        <span className="badge badge-active">
          {quotes.length} {quotes.length === 1 ? "Quote" : "Quotes"} Ranked
        </span>
      </div>

      <div className="table-container">
        <table className="custom-table">
          <thead>
            <tr>
              <th style={{ width: 70 }}>Rank</th>
              <th>Carrier / Supplier</th>
              <th>Freight</th>
              <th>Origin</th>
              <th>Destination</th>
              <th>Total Price</th>
              <th>Transit</th>
              <th>Validity</th>
              <th>Submitted</th>
            </tr>
          </thead>
          <tbody>
            {quotes.map((quote) => {
              const isL1 = quote.supplierRank === 1;
              const isL2 = quote.supplierRank === 2;
              const isL3 = quote.supplierRank === 3;

              return (
                <tr key={quote.id} className={isL1 ? "row-l1" : ""}>
                  <td>
                    <div
                      className={`rank-badge ${
                        isL1
                          ? "rank-badge-l1"
                          : isL2
                            ? "rank-badge-l2"
                            : isL3
                              ? "rank-badge-l3"
                              : "rank-badge-other"
                      }`}
                    >
                      {isL1
                        ? "L1"
                        : quote.rankLabel || `L${quote.supplierRank}`}
                    </div>
                  </td>
                  <td>
                    <div
                      style={{ display: "flex", alignItems: "center", gap: 8 }}
                    >
                      <span
                        style={{
                          fontWeight: 600,
                          color: isL1 ? "#fef08a" : "#fff",
                        }}
                      >
                        {quote.carrierName}
                      </span>
                      {isL1 && (
                        <span
                          title="Current Best Bid"
                          style={{ display: "inline-flex", color: "#eab308" }}
                        >
                          <Trophy size={14} />
                        </span>
                      )}
                    </div>
                  </td>
                  <td className="font-mono">
                    {formatCurrency(quote.freightCharges)}
                  </td>
                  <td className="font-mono">
                    {formatCurrency(quote.originCharges)}
                  </td>
                  <td className="font-mono">
                    {formatCurrency(quote.destinationCharges)}
                  </td>
                  <td>
                    <span
                      className="font-mono"
                      style={{
                        fontWeight: 800,
                        fontSize: "0.9375rem",
                        color: isL1 ? "#34d399" : "#fff",
                      }}
                    >
                      {formatCurrency(quote.totalAmount)}
                    </span>
                  </td>
                  <td>
                    <span
                      style={{
                        fontSize: "0.8125rem",
                        color: "var(--text-secondary)",
                      }}
                    >
                      {quote.transitTimeDays} days
                    </span>
                  </td>
                  <td>
                    <span
                      style={{
                        fontSize: "0.8125rem",
                        color: "var(--text-muted)",
                      }}
                    >
                      {formatDateOnly(quote.quoteValidity)}
                    </span>
                  </td>
                  <td>
                    <span
                      style={{
                        fontSize: "0.75rem",
                        color: "var(--text-muted)",
                      }}
                    >
                      {formatTimeOnly(quote.submittedAt)}
                    </span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
