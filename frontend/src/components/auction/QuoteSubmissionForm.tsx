import React, { useState } from "react";
import type { SubmitQuotePayload, Rfq } from "../../types/auction.types";
import { formatCurrency } from "../../utils/currencyFormatter";
import { Send, AlertCircle, CheckCircle2, Lock } from "lucide-react";
import confetti from "canvas-confetti";

interface QuoteSubmissionFormProps {
  rfq: Rfq;
  onSubmitQuote: (payload: SubmitQuotePayload) => Promise<void>;
}

export const QuoteSubmissionForm: React.FC<QuoteSubmissionFormProps> = ({
  rfq,
  onSubmitQuote,
}) => {
  const defaultValidity = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
    .toISOString()
    .split("T")[0];

  const [carrierName, setCarrierName] = useState("DHL Global Forwarding");
  const [freightCharges, setFreightCharges] = useState<number | "">(950);
  const [originCharges, setOriginCharges] = useState<number | "">(100);
  const [destinationCharges, setDestinationCharges] = useState<number | "">(
    150,
  );
  const [transitTimeDays, setTransitTimeDays] = useState<number | "">(14);
  const [quoteValidity, setQuoteValidity] = useState(defaultValidity);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const isAuctionClosed =
    rfq.status === "CLOSED" || rfq.status === "FORCE_CLOSED";

  const freightNum = typeof freightCharges === "number" ? freightCharges : 0;
  const originNum = typeof originCharges === "number" ? originCharges : 0;
  const destNum =
    typeof destinationCharges === "number" ? destinationCharges : 0;
  const computedTotal = freightNum + originNum + destNum;

  const handleQuickCarrierSelect = (
    carrier: string,
    f: number,
    o: number,
    d: number,
    days: number,
  ) => {
    setCarrierName(carrier);
    setFreightCharges(f);
    setOriginCharges(o);
    setDestinationCharges(d);
    setTransitTimeDays(days);
    setErrorMessage(null);
    setSuccessMessage(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);

    if (isAuctionClosed) {
      setErrorMessage(`Cannot submit quote: Auction is ${rfq.status}.`);
      return;
    }

    if (!carrierName.trim()) {
      setErrorMessage("Carrier name is required.");
      return;
    }

    try {
      setIsSubmitting(true);
      await onSubmitQuote({
        carrierName: carrierName.trim(),
        freightCharges: freightNum,
        originCharges: originNum,
        destinationCharges: destNum,
        transitTimeDays:
          typeof transitTimeDays === "number" ? transitTimeDays : 14,
        quoteValidity,
      });

      if (!rfq.currentLowestBid || computedTotal < rfq.currentLowestBid) {
        confetti({
          particleCount: 50,
          spread: 60,
          origin: { y: 0.8 },
        });
      }

      setSuccessMessage(
        `Quote of ${formatCurrency(computedTotal)} from '${carrierName}' submitted successfully!`,
      );
    } catch (err: any) {
      setErrorMessage(err.message || "Failed to submit quote");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="bidding-panel">
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 16,
        }}
      >
        <h3 style={{ fontSize: "1.125rem", fontWeight: 700, color: "#fff" }}>
          Submit Quote / Counter-Bid
        </h3>
        {isAuctionClosed ? (
          <span className="badge badge-closed">
            <Lock size={12} /> Bidding Closed
          </span>
        ) : (
          <span className="badge badge-active">Live Bidding Open</span>
        )}
      </div>

      {}
      {!isAuctionClosed && (
        <div style={{ marginBottom: 16 }}>
          <div
            style={{
              fontSize: "0.75rem",
              color: "var(--text-muted)",
              marginBottom: 6,
            }}
          >
            Quick fill sample carriers:
          </div>
          <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              style={{ fontSize: "0.75rem", padding: "4px 8px" }}
              onClick={() =>
                handleQuickCarrierSelect("Maersk Line", 900, 100, 100, 12)
              }
            >
              Maersk ($1.1k)
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              style={{ fontSize: "0.75rem", padding: "4px 8px" }}
              onClick={() =>
                handleQuickCarrierSelect("CMA CGM", 800, 100, 120, 15)
              }
            >
              CMA CGM ($1.02k)
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              style={{ fontSize: "0.75rem", padding: "4px 8px" }}
              onClick={() =>
                handleQuickCarrierSelect("MSC Mediterranean", 750, 100, 100, 16)
              }
            >
              MSC ($950)
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              style={{ fontSize: "0.75rem", padding: "4px 8px" }}
              onClick={() =>
                handleQuickCarrierSelect("Hapag-Lloyd", 700, 90, 110, 14)
              }
            >
              Hapag ($900)
            </button>
          </div>
        </div>
      )}

      {errorMessage && (
        <div
          style={{
            background: "var(--status-forced-bg)",
            border: "1px solid var(--status-forced-border)",
            color: "var(--status-forced-text)",
            padding: "10px 12px",
            borderRadius: "var(--radius-md)",
            marginBottom: 16,
            display: "flex",
            alignItems: "center",
            gap: 8,
            fontSize: "0.8125rem",
          }}
        >
          <AlertCircle size={16} />
          {errorMessage}
        </div>
      )}

      {successMessage && (
        <div
          style={{
            background: "var(--status-active-bg)",
            border: "1px solid var(--status-active-border)",
            color: "var(--status-active-text)",
            padding: "10px 12px",
            borderRadius: "var(--radius-md)",
            marginBottom: 16,
            display: "flex",
            alignItems: "center",
            gap: 8,
            fontSize: "0.8125rem",
          }}
        >
          <CheckCircle2 size={16} />
          {successMessage}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label">Carrier / Forwarder Name *</label>
          <input
            className="form-input"
            type="text"
            required
            disabled={isAuctionClosed || isSubmitting}
            value={carrierName}
            onChange={(e) => setCarrierName(e.target.value)}
            placeholder="e.g. DHL Global Logistics"
          />
        </div>

        <div
          style={{
            display: "grid",
            gridTemplateColumns: "1fr 1fr 1fr",
            gap: 10,
          }}
        >
          <div className="form-group">
            <label className="form-label">Freight ($) *</label>
            <input
              className="form-input font-mono"
              type="number"
              min="0"
              step="10"
              required
              disabled={isAuctionClosed || isSubmitting}
              value={freightCharges}
              onChange={(e) =>
                setFreightCharges(
                  e.target.value === "" ? "" : Number(e.target.value),
                )
              }
            />
          </div>
          <div className="form-group">
            <label className="form-label">Origin ($) *</label>
            <input
              className="form-input font-mono"
              type="number"
              min="0"
              step="5"
              required
              disabled={isAuctionClosed || isSubmitting}
              value={originCharges}
              onChange={(e) =>
                setOriginCharges(
                  e.target.value === "" ? "" : Number(e.target.value),
                )
              }
            />
          </div>
          <div className="form-group">
            <label className="form-label">Dest ($) *</label>
            <input
              className="form-input font-mono"
              type="number"
              min="0"
              step="5"
              required
              disabled={isAuctionClosed || isSubmitting}
              value={destinationCharges}
              onChange={(e) =>
                setDestinationCharges(
                  e.target.value === "" ? "" : Number(e.target.value),
                )
              }
            />
          </div>
        </div>

        <div
          style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10 }}
        >
          <div className="form-group">
            <label className="form-label">Transit (Days) *</label>
            <input
              className="form-input"
              type="number"
              min="1"
              max="90"
              required
              disabled={isAuctionClosed || isSubmitting}
              value={transitTimeDays}
              onChange={(e) =>
                setTransitTimeDays(
                  e.target.value === "" ? "" : Number(e.target.value),
                )
              }
            />
          </div>
          <div className="form-group">
            <label className="form-label">Quote Validity *</label>
            <input
              className="form-input"
              type="date"
              required
              disabled={isAuctionClosed || isSubmitting}
              value={quoteValidity}
              onChange={(e) => setQuoteValidity(e.target.value)}
            />
          </div>
        </div>

        {}
        <div className="price-breakdown-box">
          <div className="breakdown-row">
            <span>Base Ocean/Air Freight:</span>
            <span className="font-mono">{formatCurrency(freightNum)}</span>
          </div>
          <div className="breakdown-row">
            <span>Origin Charges:</span>
            <span className="font-mono">{formatCurrency(originNum)}</span>
          </div>
          <div className="breakdown-row">
            <span>Destination Charges:</span>
            <span className="font-mono">{formatCurrency(destNum)}</span>
          </div>
          <div className="breakdown-total">
            <span>Calculated Total Bid:</span>
            <span className="font-mono" style={{ color: "#34d399" }}>
              {formatCurrency(computedTotal)}
            </span>
          </div>
        </div>

        <button
          type="submit"
          className="btn btn-primary"
          style={{ width: "100%", padding: "12px" }}
          disabled={isAuctionClosed || isSubmitting}
        >
          {isSubmitting ? (
            "Submitting Quote..."
          ) : isAuctionClosed ? (
            "Auction Closed"
          ) : (
            <>
              <Send size={16} /> Submit Competitive Quote
            </>
          )}
        </button>
      </form>
    </div>
  );
};
