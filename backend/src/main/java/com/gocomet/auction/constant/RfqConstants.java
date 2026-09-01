package com.gocomet.auction.constant;

public final class RfqConstants {

    private RfqConstants() {}

    public static final int DEFAULT_TRIGGER_WINDOW_MINUTES = 10;
    public static final int DEFAULT_EXTENSION_DURATION_MINUTES = 5;
    public static final int MIN_TRIGGER_WINDOW_MINUTES = 1;
    public static final int MAX_TRIGGER_WINDOW_MINUTES = 120;
    public static final int MIN_EXTENSION_DURATION_MINUTES = 1;
    public static final int MAX_EXTENSION_DURATION_MINUTES = 60;

    public static final String RFQ_REFERENCE_PREFIX = "RFQ-";
    public static final int REFERENCE_RANDOM_SUFFIX_BOUND = 90000;
    public static final int REFERENCE_RANDOM_SUFFIX_OFFSET = 10000;

    public static final int TOP_RANK_L1 = 1;
    public static final String RANK_PREFIX = "L";

    public static final String API_V1_BASE_PATH = "/api/v1";
    public static final String RFQS_ENDPOINT = API_V1_BASE_PATH + "/rfqs";
    public static final String QUOTES_ENDPOINT = API_V1_BASE_PATH + "/rfqs/{rfqId}/quotes";
    public static final String AUDIT_LOGS_ENDPOINT = API_V1_BASE_PATH + "/rfqs/{rfqId}/audit-logs";
}
