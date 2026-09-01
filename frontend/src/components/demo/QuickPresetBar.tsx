import React, { useState } from "react";
import { rfqApi } from "../../api/rfqApi";
import { quoteApi } from "../../api/quoteApi";
import { Sparkles, RefreshCw, Zap } from "lucide-react";

interface QuickPresetBarProps {
  onRefresh: () => void;
}

export const QuickPresetBar: React.FC<QuickPresetBarProps> = ({
  onRefresh,
}) => {
  const [isSeeding, setIsSeeding] = useState(false);

  const seedSampleRfqs = async () => {
    try {
      setIsSeeding(true);
      const now = new Date();
      const format = (d: Date) => d.toISOString();

      const rfq1 = await rfqApi.createRfq({
        name: "Mumbai (JNPT) to Rotterdam 15x 40ft HC Ocean Freight",
        referenceId: `RFQ-${now.getFullYear()}-101`,
        bidStartTime: format(now),
        bidCloseTime: format(new Date(now.getTime() + 45 * 60 * 1000)),
        forcedBidCloseTime: format(new Date(now.getTime() + 90 * 60 * 1000)),
        pickupServiceDate: new Date(now.getTime() + 10 * 24 * 60 * 60 * 1000)
          .toISOString()
          .split("T")[0],
        triggerWindowMinutes: 10,
        extensionDurationMinutes: 5,
        extensionTriggerType: "ANY_BID",
      });

      await quoteApi.submitQuote(rfq1.id, {
        carrierName: "Maersk Line",
        freightCharges: 1400,
        originCharges: 150,
        destinationCharges: 200,
        transitTimeDays: 22,
        quoteValidity: new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000)
          .toISOString()
          .split("T")[0],
      });
      await quoteApi.submitQuote(rfq1.id, {
        carrierName: "MSC Logistics",
        freightCharges: 1300,
        originCharges: 140,
        destinationCharges: 180,
        transitTimeDays: 24,
        quoteValidity: new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000)
          .toISOString()
          .split("T")[0],
      });

      const rfq2 = await rfqApi.createRfq({
        name: "Live Demo: Shanghai to Los Angeles Fast Bidding Room",
        referenceId: `RFQ-${now.getFullYear()}-FAST-DEMO`,
        bidStartTime: format(now),
        bidCloseTime: format(new Date(now.getTime() + 3 * 60 * 1000)),
        forcedBidCloseTime: format(new Date(now.getTime() + 6 * 60 * 1000)),
        pickupServiceDate: new Date(now.getTime() + 5 * 24 * 60 * 60 * 1000)
          .toISOString()
          .split("T")[0],
        triggerWindowMinutes: 2,
        extensionDurationMinutes: 1,
        extensionTriggerType: "L1_CHANGE",
      });

      await quoteApi.submitQuote(rfq2.id, {
        carrierName: "COSCO Shipping",
        freightCharges: 1100,
        originCharges: 100,
        destinationCharges: 150,
        transitTimeDays: 14,
        quoteValidity: new Date(now.getTime() + 15 * 24 * 60 * 60 * 1000)
          .toISOString()
          .split("T")[0],
      });

      onRefresh();
    } catch (err) {
      console.error("Failed to seed sample data", err);
    } finally {
      setIsSeeding(false);
    }
  };

  return (
    <div className="preset-bar">
      <div className="preset-title">
        <Sparkles size={16} />
        <span>Quick Demo Sandbox</span>
      </div>
      <div className="preset-actions">
        <button
          className="btn btn-secondary btn-sm"
          onClick={seedSampleRfqs}
          disabled={isSeeding}
        >
          <Zap size={14} className="text-amber-400" />
          {isSeeding ? "Seeding Data..." : "Seed Sample RFQs & Live Demo Room"}
        </button>
        <button className="btn btn-outline btn-sm" onClick={onRefresh}>
          <RefreshCw size={14} /> Refresh Data
        </button>
      </div>
    </div>
  );
};
