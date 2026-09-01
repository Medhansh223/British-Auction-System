export type AuctionStatus =
  "DRAFT" | "ACTIVE" | "EXTENDED" | "CLOSED" | "FORCE_CLOSED";

export type ExtensionTriggerType = "ANY_BID" | "ANY_RANK_CHANGE" | "L1_CHANGE";

export type ActivityEventType =
  | "RFQ_CREATED"
  | "BID_SUBMITTED"
  | "AUCTION_EXTENDED"
  | "AUCTION_CLOSED"
  | "AUCTION_FORCE_CLOSED";

export interface Quote {
  id: number;
  rfqId: number;
  carrierName: string;
  freightCharges: number;
  originCharges: number;
  destinationCharges: number;
  totalAmount: number;
  transitTimeDays: number;
  quoteValidity: string;
  supplierRank: number;
  rankLabel: string;
  submittedAt: string;
}

export interface AuctionActivityLog {
  id: number;
  rfqId: number;
  quoteId?: number;
  eventType: ActivityEventType;
  reason: string;
  previousCloseTime?: string;
  newCloseTime?: string;
  carrierName?: string;
  bidAmount?: number;
  createdAt: string;
}

export interface Rfq {
  id: number;
  referenceId: string;
  name: string;
  bidStartTime: string;
  bidCloseTime: string;
  originalBidCloseTime: string;
  forcedBidCloseTime: string;
  pickupServiceDate: string;
  status: AuctionStatus;
  triggerWindowMinutes: number;
  extensionDurationMinutes: number;
  extensionTriggerType: ExtensionTriggerType;
  totalExtensionsCount: number;
  createdAt: string;
  updatedAt: string;

  currentLowestBid?: number;
  currentLowestCarrier?: string;
  totalQuotesCount: number;
  remainingSecondsToClose: number;
  remainingSecondsToForcedClose: number;
  isInsideTriggerWindow: boolean;
  isForceCloseImminent: boolean;

  quotes?: Quote[];
  activityLogs?: AuctionActivityLog[];
}

export interface CreateRfqPayload {
  name: string;
  referenceId?: string;
  bidStartTime: string;
  bidCloseTime: string;
  forcedBidCloseTime: string;
  pickupServiceDate: string;
  triggerWindowMinutes: number;
  extensionDurationMinutes: number;
  extensionTriggerType: ExtensionTriggerType;
}

export interface SubmitQuotePayload {
  carrierName: string;
  freightCharges: number;
  originCharges: number;
  destinationCharges: number;
  transitTimeDays: number;
  quoteValidity: string;
}

export interface ApiErrorResponse {
  errorCode: string;
  message: string;
  status: number;
  path: string;
  timestamp: string;
  fieldErrors?: Record<string, string>;
}
