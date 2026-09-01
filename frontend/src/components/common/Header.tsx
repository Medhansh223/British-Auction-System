import React from "react";
import { Gavel, Plus } from "lucide-react";

interface HeaderProps {
  onOpenCreateModal: () => void;
  onGoHome: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  onOpenCreateModal,
  onGoHome,
}) => {
  return (
    <header className="app-header">
      <div className="header-inner">
        <div
          className="brand-title"
          onClick={onGoHome}
          style={{ cursor: "pointer" }}
        >
          <div className="brand-icon">
            <Gavel size={22} />
          </div>
          <div>
            <div>GoComet British Auction</div>
            <div
              style={{
                fontSize: "0.75rem",
                fontWeight: 400,
                color: "var(--text-muted)",
              }}
            >
              Dynamic Anti-Sniping RFQ Procurement System
            </div>
          </div>
        </div>

        <div className="header-actions">
          <button className="btn btn-primary" onClick={onOpenCreateModal}>
            <Plus size={18} />
            Create RFQ
          </button>
        </div>
      </div>
    </header>
  );
};
