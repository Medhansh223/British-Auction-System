import { apiClient } from "./apiClient";
import type { AuctionActivityLog } from "../types/auction.types";

export const auditApi = {
  async getAuditLogs(rfqId: number): Promise<AuctionActivityLog[]> {
    const response = await apiClient.get<AuctionActivityLog[]>(
      `/rfqs/${rfqId}/audit-logs`,
    );
    return response.data;
  },
};
