package com.gocomet.auction.exception;

import org.springframework.http.HttpStatus;

public class QuoteValidationException extends BaseAuctionException {

    public static final String ERROR_CODE = "INVALID_QUOTE_DATA";

    public QuoteValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ERROR_CODE);
    }
}
