package com.app.guesthouse.bookmyroom.exception;

/** Thrown when a request breaks a business rule (check-out before check-in, too many guests...) -> HTTP 400. */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
