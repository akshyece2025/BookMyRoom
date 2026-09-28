package com.app.guesthouse.bookmyroom.exception;

/** Thrown when a request clashes with existing data (double booking, duplicate room number...) -> HTTP 409. */
public class BookingConflictException extends RuntimeException {
    public BookingConflictException(String message) {
        super(message);
    }
}
