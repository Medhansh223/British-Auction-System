package com.gocomet.auction.exception;

import org.springframework.http.HttpStatus;

public class InvalidRfqTimelineException extends BaseAuctionException {

    public static final String ERROR_CODE = "INVALID_RFQ_TIMELINE";

    public InvalidRfqTimelineException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ERROR_CODE);
    }
}
