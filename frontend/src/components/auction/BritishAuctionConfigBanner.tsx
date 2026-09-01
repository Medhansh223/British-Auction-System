import React from "react";
import type { ExtensionTriggerType } from "../../types/auction.types";
import { Zap, Clock, ShieldCheck, RefreshCw } from "lucide-react";

interface BritishAuctionConfigBannerProps {
  triggerWindowMinutes: number;
  extensionDurationMinutes: number;
  triggerType: ExtensionTriggerType;
  totalExtensionsCount: number;
}

export const BritishAuctionConfigBanner: React.FC<
  BritishAuctionConfigBannerProps
> = ({
  triggerWindowMinutes,
  extensionDurationMinutes,
  triggerType,
  totalExtensionsCount,
}) => {
  const getTriggerDescription = () => {
    switch (triggerType) {
      case "ANY_BID":
        return "Any new bid received in the trigger window extends the auction.";
      case "ANY_RANK_CHANGE":
        return "Any quote submission altering supplier rankings extends the auction.";
      case "L1_CHANGE":
        return "Only quotes that establish a new Lowest Bidder (L1) extend the auction.";
      default:
        return "";
    }
  };

  return (
    <div className="rules-banner">
      <div className="rules-info">
        <Zap size={22} className="text-blue-400" />
        <div>
          <div style={{ fontSize: "0.875rem", fontWeight: 700, color: "#fff" }}>
            British Auction Rules Active ({triggerType})
          </div>
          <div
            style={{ fontSize: "0.8125rem", color: "var(--text-secondary)" }}
          >
            {getTriggerDescription()}
          </div>
        </div>
      </div>

      <div className="rules-chips">
        <div className="rule-chip">
          <Clock size={12} style={{ display: "inline", marginRight: 4 }} />
          Trigger Window: <strong>Last {triggerWindowMinutes}m</strong>
        </div>
        <div className="rule-chip">
          <RefreshCw size={12} style={{ display: "inline", marginRight: 4 }} />
          Extension Duration: <strong>+{extensionDurationMinutes}m</strong>
        </div>
        <div
          className="rule-chip"
          style={{ color: "var(--status-extended-text)" }}
        >
          Extensions Applied: <strong>{totalExtensionsCount}x</strong>
        </div>
        <div
          className="rule-chip"
          style={{ color: "var(--status-forced-text)" }}
        >
          <ShieldCheck
            size={12}
            style={{ display: "inline", marginRight: 4 }}
          />
          Hard Ceiling Enforced
        </div>
      </div>
    </div>
  );
};
