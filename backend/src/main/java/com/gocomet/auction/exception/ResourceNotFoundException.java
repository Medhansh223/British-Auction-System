package com.gocomet.auction.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseAuctionException {

    public static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }
}
