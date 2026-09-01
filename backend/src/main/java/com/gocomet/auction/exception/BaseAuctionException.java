package com.gocomet.auction.exception;

import org.springframework.http.HttpStatus;

public abstract class BaseAuctionException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    protected BaseAuctionException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    protected BaseAuctionException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
