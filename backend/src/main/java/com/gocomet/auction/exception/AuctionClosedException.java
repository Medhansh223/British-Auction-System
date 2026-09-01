package com.gocomet.auction.exception;

import org.springframework.http.HttpStatus;

public class AuctionClosedException extends BaseAuctionException {

    public static final String ERROR_CODE = "AUCTION_CLOSED";

    public AuctionClosedException(String message) {
        super(message, HttpStatus.CONFLICT, ERROR_CODE);
    }
}
