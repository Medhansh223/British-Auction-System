package com.gocomet.auction.constant;

public final class ExceptionMessageConstants {

    private ExceptionMessageConstants() {}

    public static final String MSG_FORCED_CLOSE_BEFORE_CLOSE =
            "Validation Failed: Forced Bid Close Time must always be strictly greater than Bid Close Time.";
    public static final String MSG_CLOSE_BEFORE_START =
            "Validation Failed: Bid Close Time must be strictly later than Bid Start Time.";
    public static final String MSG_START_IN_PAST =
            "Validation Failed: Bid Start Time cannot be in the past when creating a new RFQ.";
    public static final String MSG_INVALID_TRIGGER_WINDOW =
            "Validation Failed: Trigger Window (X) must be between 1 and 120 minutes.";
    public static final String MSG_INVALID_EXTENSION_DURATION =
            "Validation Failed: Extension Duration (Y) must be between 1 and 60 minutes.";

    public static final String MSG_RFQ_NOT_FOUND = "RFQ with ID '%s' not found.";
    public static final String MSG_RFQ_REF_NOT_FOUND = "RFQ with reference ID '%s' not found.";
    public static final String MSG_QUOTE_NOT_FOUND = "Quote with ID '%s' not found.";

    public static final String MSG_AUCTION_CLOSED =
            "Quote Rejected: Auction '%s' is CLOSED. No further bids can be submitted.";
    public static final String MSG_AUCTION_FORCE_CLOSED =
            "Quote Rejected: Auction '%s' has reached its Forced Bid Close Time and is FORCE_CLOSED.";
    public static final String MSG_AUCTION_NOT_STARTED =
            "Quote Rejected: Auction '%s' has not started yet. Starts at %s.";

    public static final String MSG_CARRIER_REQUIRED = "Carrier name is required and cannot be blank.";
    public static final String MSG_FREIGHT_POSITIVE = "Freight charges must be greater than or equal to zero.";
    public static final String MSG_ORIGIN_POSITIVE = "Origin charges must be greater than or equal to zero.";
    public static final String MSG_DESTINATION_POSITIVE = "Destination charges must be greater than or equal to zero.";
    public static final String MSG_TRANSIT_POSITIVE = "Transit time must be at least 1 day.";
    public static final String MSG_VALIDITY_REQUIRED = "Quote validity date is required and must be in the future.";

    public static final String MSG_UNKNOWN_TRIGGER_STRATEGY =
            "Unsupported Extension Trigger Strategy: '%s'. Supported types: ANY_BID, ANY_RANK_CHANGE, L1_CHANGE.";
}
