package com.gocomet.auction.constant;

public final class LogMessageConstants {

    private LogMessageConstants() {

    }

    public static final String LOG_RFQ_CREATED =
    "RFQ created successfully with ID: {}, Ref: '{}', Start: {}, Close: {}, ForcedClose: {}, Strategy: {}";
    public static final String LOG_RFQ_FETCHED = "Retrieved RFQ ID: {}, Status: {}";
    public static final String LOG_RFQ_STATUS_TRANSITION = "Transitioning RFQ ID: {} status from {} to {}";

    public static final String LOG_QUOTE_SUBMITTING =
    "Processing quote submission for RFQ ID: {}, Carrier: '{}', TotalAmount: {}";
    public static final String LOG_QUOTE_SUBMITTED =
    "Quote successfully saved with ID: {}, RFQ ID: {}, Rank: L{}, Total: {}";
    public static final String LOG_RANKINGS_RECALCULATED =
    "Recalculated rankings for RFQ ID: {}. Total active quotes: {}";

    public static final String LOG_EVALUATING_EXTENSION =
    "Evaluating British Auction extension for RFQ ID: {} under Strategy: {}";
    public static final String LOG_TRIGGER_WINDOW_ACTIVE =
    "Bid timestamp {} falls inside trigger window [{} - {}] for RFQ ID: {}";
    public static final String LOG_TRIGGER_WINDOW_INACTIVE =
    "Bid timestamp {} is outside trigger window for RFQ ID: {}. No extension check needed.";
    public static final String LOG_AUCTION_EXTENDED =
    "Auction EXTENDED for RFQ ID: {}! OldClose: {}, NewClose: {}, ExtensionCount: {}, Reason: '{}'";
    public static final String LOG_AUCTION_EXTENSION_CAPPED =
    "Auction extension CAPPED by Forced Close Time for RFQ ID: {}! Capped at: {}";

    public static final String LOG_TIMELINE_VALIDATION_FAILED =
    "RFQ Timeline validation failed: {}";
    public static final String LOG_QUOTE_ON_CLOSED_AUCTION =
    "Rejected quote from carrier '{}' on RFQ ID: {} because auction status is {}";
    public static final String LOG_RESOURCE_NOT_FOUND =
    "Resource not found: {}";
}
