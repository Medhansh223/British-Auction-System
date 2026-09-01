import { apiClient } from "./apiClient";
import type { Quote, SubmitQuotePayload } from "../types/auction.types";

export const quoteApi = {
  async submitQuote(
    rfqId: number,
    payload: SubmitQuotePayload,
  ): Promise<Quote> {
    const response = await apiClient.post<Quote>(
      `/rfqs/${rfqId}/quotes`,
      payload,
    );
    return response.data;
  },

  async getQuotesForRfq(rfqId: number): Promise<Quote[]> {
    const response = await apiClient.get<Quote[]>(`/rfqs/${rfqId}/quotes`);
    return response.data;
  },
};
