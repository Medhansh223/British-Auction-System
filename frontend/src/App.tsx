import React, { useState, useEffect, useCallback } from "react";
import { rfqApi } from "./api/rfqApi";
import { quoteApi } from "./api/quoteApi";
import type {
  Rfq,
  AuctionStatus,
  CreateRfqPayload,
  SubmitQuotePayload,
} from "./types/auction.types";
import { Header } from "./components/common/Header";
import { RfqCard } from "./components/rfq/RfqCard";
import { CreateRfqModal } from "./components/rfq/CreateRfqModal";
import { AuctionHeader } from "./components/auction/AuctionHeader";
import { BritishAuctionConfigBanner } from "./components/auction/BritishAuctionConfigBanner";
import { LeaderboardTable } from "./components/auction/LeaderboardTable";
import { QuoteSubmissionForm } from "./components/auction/QuoteSubmissionForm";
import { ActivityLogTimeline } from "./components/auction/ActivityLogTimeline";
import { QuickPresetBar } from "./components/demo/QuickPresetBar";
import { Search, AlertCircle, Inbox } from "lucide-react";

export const App: React.FC = () => {
  const [rfqs, setRfqs] = useState<Rfq[]>([]);
  const [selectedRfqId, setSelectedRfqId] = useState<number | null>(null);
  const [selectedRfq, setSelectedRfq] = useState<Rfq | null>(null);

  const [statusFilter, setStatusFilter] = useState<AuctionStatus | "ALL">(
    "ALL",
  );
  const [searchQuery, setSearchQuery] = useState("");

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fetchRfqs = useCallback(async () => {
    try {
      setErrorMessage(null);
      const filter = statusFilter === "ALL" ? undefined : statusFilter;
      const data = await rfqApi.getAllRfqs(filter);
      setRfqs(data);
    } catch (err: any) {
      console.error("Error fetching RFQs:", err);
      setErrorMessage(err.message || "Failed to load RFQs from backend");
    }
  }, [statusFilter]);

  const fetchSelectedRfqDetails = useCallback(async (id: number) => {
    try {
      const data = await rfqApi.getRfqById(id);
      setSelectedRfq(data);
    } catch (err: any) {
      console.error("Error fetching RFQ details:", err);
    }
  }, []);

  useEffect(() => {
    fetchRfqs();
  }, [fetchRfqs]);

  useEffect(() => {
    const interval = setInterval(() => {
      if (selectedRfqId) {
        fetchSelectedRfqDetails(selectedRfqId);
      } else {
        fetchRfqs();
      }
    }, 3000);

    return () => clearInterval(interval);
  }, [selectedRfqId, fetchSelectedRfqDetails, fetchRfqs]);

  const handleSelectRfq = (rfq: Rfq) => {
    setSelectedRfqId(rfq.id);
    setSelectedRfq(rfq);
    fetchSelectedRfqDetails(rfq.id);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleBackToList = () => {
    setSelectedRfqId(null);
    setSelectedRfq(null);
    fetchRfqs();
  };

  const handleCreateRfq = async (payload: CreateRfqPayload) => {
    const created = await rfqApi.createRfq(payload);
    await fetchRfqs();
    handleSelectRfq(created);
  };

  const handleSubmitQuote = async (payload: SubmitQuotePayload) => {
    if (!selectedRfqId) return;
    await quoteApi.submitQuote(selectedRfqId, payload);
    await fetchSelectedRfqDetails(selectedRfqId);
  };

  const filteredRfqs = rfqs.filter((r) => {
    const matchesSearch =
      r.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      r.referenceId.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesSearch;
  });

  return (
    <div className="app-container">
      <Header
        onOpenCreateModal={() => setIsCreateModalOpen(true)}
        onGoHome={handleBackToList}
      />

      <main className="main-content">
        {}
        {errorMessage && (
          <div
            style={{
              background: "var(--status-forced-bg)",
              border: "1px solid var(--status-forced-border)",
              color: "var(--status-forced-text)",
              padding: "14px 18px",
              borderRadius: "var(--radius-md)",
              marginBottom: 24,
              display: "flex",
              alignItems: "center",
              gap: 10,
              fontSize: "0.875rem",
            }}
          >
            <AlertCircle size={18} />
            <div>
              <strong>Backend Connection Notice:</strong> {errorMessage} (Ensure
              your backend API is running and accessible)
            </div>
          </div>
        )}

        {}
        {selectedRfqId && selectedRfq ? (
          <div>
            <AuctionHeader
              rfq={selectedRfq}
              onBack={handleBackToList}
              onRefresh={() => fetchSelectedRfqDetails(selectedRfq.id)}
            />

            <BritishAuctionConfigBanner
              triggerWindowMinutes={selectedRfq.triggerWindowMinutes}
              extensionDurationMinutes={selectedRfq.extensionDurationMinutes}
              triggerType={selectedRfq.extensionTriggerType}
              totalExtensionsCount={selectedRfq.totalExtensionsCount}
            />

            <div className="detail-layout">
              <div className="left-pane">
                <LeaderboardTable quotes={selectedRfq.quotes || []} />
                <ActivityLogTimeline logs={selectedRfq.activityLogs || []} />
              </div>

              <div className="right-pane">
                <QuoteSubmissionForm
                  rfq={selectedRfq}
                  onSubmitQuote={handleSubmitQuote}
                />
              </div>
            </div>
          </div>
        ) : (
          <div>
            <QuickPresetBar onRefresh={fetchRfqs} />

            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                flexWrap: "wrap",
                gap: 16,
                marginBottom: 20,
              }}
            >
              <div>
                <h1
                  style={{
                    fontSize: "1.75rem",
                    fontWeight: 800,
                    color: "#fff",
                    letterSpacing: "-0.02em",
                  }}
                >
                  British Auction RFQ Management
                </h1>
                <p style={{ fontSize: "0.875rem", color: "var(--text-muted)" }}>
                  Monitor ongoing auctions, track supplier rank changes, and
                  simulate dynamic anti-sniping extensions
                </p>
              </div>

              {}
              <div style={{ position: "relative", width: 300 }}>
                <Search
                  size={16}
                  style={{
                    position: "absolute",
                    left: 12,
                    top: 12,
                    color: "var(--text-muted)",
                  }}
                />
                <input
                  className="form-input"
                  style={{ paddingLeft: 36 }}
                  type="text"
                  placeholder="Search by name or reference ID..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
            </div>

            {}
            <div className="filter-tabs">
              {(
                [
                  "ALL",
                  "ACTIVE",
                  "EXTENDED",
                  "CLOSED",
                  "FORCE_CLOSED",
                  "DRAFT",
                ] as const
              ).map((status) => (
                <button
                  key={status}
                  className={`tab-btn ${statusFilter === status ? "active" : ""}`}
                  onClick={() => setStatusFilter(status)}
                >
                  {status === "ALL" ? "All Auctions" : status.replace("_", " ")}
                  <span
                    style={{ marginLeft: 6, fontSize: "0.75rem", opacity: 0.7 }}
                  >
                    (
                    {status === "ALL"
                      ? rfqs.length
                      : rfqs.filter((r) => r.status === status).length}
                    )
                  </span>
                </button>
              ))}
            </div>

            {}
            {filteredRfqs.length > 0 ? (
              <div className="rfq-grid">
                {filteredRfqs.map((rfq) => (
                  <RfqCard key={rfq.id} rfq={rfq} onSelect={handleSelectRfq} />
                ))}
              </div>
            ) : (
              <div
                className="card"
                style={{ textAlign: "center", padding: "60px 24px" }}
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
                  <Inbox size={40} className="text-slate-400" />
                </div>
                <h3
                  style={{
                    fontSize: "1.25rem",
                    fontWeight: 600,
                    color: "#fff",
                    marginBottom: 8,
                  }}
                >
                  No RFQs Found
                </h3>
                <p
                  style={{
                    fontSize: "0.875rem",
                    color: "var(--text-muted)",
                    maxWidth: 420,
                    margin: "0 auto 20px",
                  }}
                >
                  {searchQuery
                    ? `No auctions match your search query "${searchQuery}".`
                    : "Get started by creating your first British Auction RFQ or use the Quick Demo Sandbox button above."}
                </p>
                <button
                  className="btn btn-primary"
                  onClick={() => setIsCreateModalOpen(true)}
                >
                  Create Your First RFQ
                </button>
              </div>
            )}
          </div>
        )}
      </main>

      {}
      <CreateRfqModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSubmit={handleCreateRfq}
      />
    </div>
  );
};

export default App;
