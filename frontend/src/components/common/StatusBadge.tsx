import React from "react";
import type { AuctionStatus } from "../../types/auction.types";

interface StatusBadgeProps {
  status: AuctionStatus;
  isExtended?: boolean;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  isExtended,
}) => {
  const effectiveStatus =
    isExtended && status === "ACTIVE" ? "EXTENDED" : status;

  switch (effectiveStatus) {
    case "ACTIVE":
      return (
        <span className="badge badge-active">
          <span className="badge-dot" />
          Active
        </span>
      );
    case "EXTENDED":
      return (
        <span className="badge badge-extended">
          <span className="badge-dot" />
          Extended
        </span>
      );
    case "CLOSED":
      return (
        <span className="badge badge-closed">
          <span className="badge-dot" />
          Closed
        </span>
      );
    case "FORCE_CLOSED":
      return (
        <span className="badge badge-forced">
          <span className="badge-dot" />
          Force Closed
        </span>
      );
    case "DRAFT":
    default:
      return (
        <span className="badge badge-draft">
          <span className="badge-dot" />
          Upcoming
        </span>
      );
  }
};
