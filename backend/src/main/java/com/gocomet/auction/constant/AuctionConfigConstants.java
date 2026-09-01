package com.gocomet.auction.constant;

public final class AuctionConfigConstants {

    private AuctionConfigConstants() {}

    public static final String TRIGGER_ANY_BID = "ANY_BID";
    public static final String TRIGGER_ANY_RANK_CHANGE = "ANY_RANK_CHANGE";
    public static final String TRIGGER_L1_CHANGE = "L1_CHANGE";

    public static final String EVENT_RFQ_CREATED = "RFQ_CREATED";
    public static final String EVENT_BID_SUBMITTED = "BID_SUBMITTED";
    public static final String EVENT_AUCTION_EXTENDED = "AUCTION_EXTENDED";
    public static final String EVENT_AUCTION_CLOSED = "AUCTION_CLOSED";
    public static final String EVENT_AUCTION_FORCE_CLOSED = "AUCTION_FORCE_CLOSED";

    public static final String WS_TOPIC_PREFIX = "/topic/rfq/";
    public static final String WS_GLOBAL_TOPIC = "/topic/rfqs";
}
