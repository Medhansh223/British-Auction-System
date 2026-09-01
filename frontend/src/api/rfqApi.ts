import { apiClient } from "./apiClient";
import type {
  AuctionStatus,
  CreateRfqPayload,
  Rfq,
} from "../types/auction.types";

export const rfqApi = {
  async getAllRfqs(status?: AuctionStatus): Promise<Rfq[]> {
    const params = status ? { status } : {};
    const response = await apiClient.get<Rfq[]>("/rfqs", { params });
    return response.data;
  },

  async getRfqById(id: number): Promise<Rfq> {
    const response = await apiClient.get<Rfq>(`/rfqs/${id}`);
    return response.data;
  },

  async getRfqByReference(referenceId: string): Promise<Rfq> {
    const response = await apiClient.get<Rfq>(`/rfqs/reference/${referenceId}`);
    return response.data;
  },

  async createRfq(payload: CreateRfqPayload): Promise<Rfq> {
    const response = await apiClient.post<Rfq>("/rfqs", payload);
    return response.data;
  },
};
