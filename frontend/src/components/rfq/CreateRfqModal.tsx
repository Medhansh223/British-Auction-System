import React, { useState } from "react";
import type {
  CreateRfqPayload,
  ExtensionTriggerType,
} from "../../types/auction.types";
import { X, Sparkles, AlertCircle } from "lucide-react";

interface CreateRfqModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (payload: CreateRfqPayload) => Promise<void>;
}

export const CreateRfqModal: React.FC<CreateRfqModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
}) => {
  const now = new Date();
  const formatForInput = (d: Date) => {
    const pad = (n: number) => n.toString().padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  };

  const defaultStart = formatForInput(now);
  const defaultClose = formatForInput(new Date(now.getTime() + 60 * 60 * 1000));
  const defaultForced = formatForInput(
    new Date(now.getTime() + 90 * 60 * 1000),
  );
  const defaultPickup = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
    .toISOString()
    .split("T")[0];

  const [name, setName] = useState(
    "Mumbai to Rotterdam 20x 40ft High-Cube Cargo",
  );
  const [referenceId, setReferenceId] = useState("");
  const [bidStartTime, setBidStartTime] = useState(defaultStart);
  const [bidCloseTime, setBidCloseTime] = useState(defaultClose);
  const [forcedBidCloseTime, setForcedBidCloseTime] = useState(defaultForced);
  const [pickupServiceDate, setPickupServiceDate] = useState(defaultPickup);
  const [triggerWindowMinutes, setTriggerWindowMinutes] = useState(10);
  const [extensionDurationMinutes, setExtensionDurationMinutes] = useState(5);
  const [extensionTriggerType, setExtensionTriggerType] =
    useState<ExtensionTriggerType>("ANY_BID");

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  if (!isOpen) return null;

  const applyFastDemoPreset = () => {
    const start = new Date();
    const close = new Date(start.getTime() + 3 * 60 * 1000);
    const forced = new Date(start.getTime() + 6 * 60 * 1000);

    setName("Live Demo: Shanghai to Los Angeles Fast Bidding");
    setBidStartTime(formatForInput(start));
    setBidCloseTime(formatForInput(close));
    setForcedBidCloseTime(formatForInput(forced));
    setTriggerWindowMinutes(2);
    setExtensionDurationMinutes(1);
    setExtensionTriggerType("L1_CHANGE");
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    const startMs = new Date(bidStartTime).getTime();
    const closeMs = new Date(bidCloseTime).getTime();
    const forcedMs = new Date(forcedBidCloseTime).getTime();

    if (closeMs <= startMs) {
      setErrorMessage(
        "Bid Close Time must be strictly later than Bid Start Time.",
      );
      return;
    }
    if (forcedMs <= closeMs) {
      setErrorMessage(
        "Forced Bid Close Time (Hard Stop) must be strictly greater than Bid Close Time.",
      );
      return;
    }

    try {
      setIsSubmitting(true);
      await onSubmit({
        name,
        referenceId: referenceId.trim() || undefined,
        bidStartTime: new Date(bidStartTime).toISOString(),
        bidCloseTime: new Date(bidCloseTime).toISOString(),
        forcedBidCloseTime: new Date(forcedBidCloseTime).toISOString(),
        pickupServiceDate,
        triggerWindowMinutes: Number(triggerWindowMinutes),
        extensionDurationMinutes: Number(extensionDurationMinutes),
        extensionTriggerType,
      });
      onClose();
    } catch (err: any) {
      setErrorMessage(err.message || "Failed to create RFQ");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <h2 className="modal-title">Create British Auction RFQ</h2>
            <p style={{ fontSize: "0.8125rem", color: "var(--text-muted)" }}>
              Configure freight parameters, timeline rules, and anti-sniping
              trigger extensions
            </p>
          </div>
          <button className="modal-close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        <div style={{ marginBottom: 20 }}>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={applyFastDemoPreset}
          >
            <Sparkles size={14} className="text-cyan-400" /> Apply Fast 3-Minute
            Demo Preset
          </button>
        </div>

        {errorMessage && (
          <div
            style={{
              background: "var(--status-forced-bg)",
              border: "1px solid var(--status-forced-border)",
              color: "var(--status-forced-text)",
              padding: "10px 14px",
              borderRadius: "var(--radius-md)",
              marginBottom: 20,
              display: "flex",
              alignItems: "center",
              gap: 8,
              fontSize: "0.875rem",
            }}
          >
            <AlertCircle size={16} />
            {errorMessage}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">RFQ Title / Description *</label>
            <input
              className="form-input"
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g., JNPT to Jebel Ali 40x Container Shipment"
            />
          </div>

          <div
            style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}
          >
            <div className="form-group">
              <label className="form-label">Reference ID (Optional)</label>
              <input
                className="form-input"
                type="text"
                value={referenceId}
                onChange={(e) => setReferenceId(e.target.value)}
                placeholder="e.g., RFQ-2026-001 (Auto if blank)"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Pickup / Service Date *</label>
              <input
                className="form-input"
                type="date"
                required
                value={pickupServiceDate}
                onChange={(e) => setPickupServiceDate(e.target.value)}
              />
            </div>
          </div>

          <div
            style={{
              display: "grid",
              gridTemplateColumns: "1fr 1fr",
              gap: 14,
            }}
          >
            <div className="form-group">
              <label className="form-label">Bid Start Time *</label>
              <input
                className="form-input"
                type="datetime-local"
                required
                value={bidStartTime}
                onChange={(e) => setBidStartTime(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Bid Close Time *</label>
              <input
                className="form-input"
                type="datetime-local"
                required
                value={bidCloseTime}
                onChange={(e) => setBidCloseTime(e.target.value)}
              />
            </div>
          </div>
          <div className="form-group">
            <label
              className="form-label"
              style={{ color: "var(--status-forced-text)" }}
            >
              Forced Close (Hard Stop) *
            </label>
            <input
              className="form-input"
              type="datetime-local"
              required
              value={forcedBidCloseTime}
              onChange={(e) => setForcedBidCloseTime(e.target.value)}
            />
          </div>

          <div
            style={{
              background: "rgba(0,0,0,0.3)",
              border: "1px solid var(--border-subtle)",
              borderRadius: "var(--radius-md)",
              padding: 16,
              marginTop: 10,
              marginBottom: 20,
            }}
          >
            <h4
              style={{
                fontSize: "0.875rem",
                fontWeight: 700,
                marginBottom: 12,
                color: "var(--color-cyan)",
              }}
            >
              British Auction Configuration
            </h4>

            <div
              style={{
                display: "grid",
                gridTemplateColumns: "1fr 1fr",
                gap: 14,
                marginBottom: 14,
              }}
            >
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Trigger Window X (Minutes)</label>
                <input
                  className="form-input"
                  type="number"
                  min="1"
                  max="120"
                  required
                  value={triggerWindowMinutes}
                  onChange={(e) =>
                    setTriggerWindowMinutes(Number(e.target.value))
                  }
                />
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">
                  Extension Duration Y (Minutes)
                </label>
                <input
                  className="form-input"
                  type="number"
                  min="1"
                  max="60"
                  required
                  value={extensionDurationMinutes}
                  onChange={(e) =>
                    setExtensionDurationMinutes(Number(e.target.value))
                  }
                />
              </div>
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Extension Trigger Strategy *</label>
              <select
                className="form-select"
                value={extensionTriggerType}
                onChange={(e) =>
                  setExtensionTriggerType(
                    e.target.value as ExtensionTriggerType,
                  )
                }
              >
                <option value="ANY_BID">
                  Mode A: Any Bid placed within last X minutes
                </option>
                <option value="ANY_RANK_CHANGE">
                  Mode B: Any Supplier Rank Change within last X minutes
                </option>
                <option value="L1_CHANGE">
                  Mode C: Lowest Bidder (L1) Change within last X minutes
                </option>
              </select>
            </div>
          </div>

          <div style={{ display: "flex", justifyContent: "flex-end", gap: 12 }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={isSubmitting}
            >
              {isSubmitting ? "Creating..." : "Create British Auction"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
