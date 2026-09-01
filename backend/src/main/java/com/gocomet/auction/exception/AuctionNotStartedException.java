package com.gocomet.auction.exception;

import org.springframework.http.HttpStatus;

public class AuctionNotStartedException extends BaseAuctionException {

    public static final String ERROR_CODE = "AUCTION_NOT_STARTED";

    public AuctionNotStartedException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ERROR_CODE);
    }
}
