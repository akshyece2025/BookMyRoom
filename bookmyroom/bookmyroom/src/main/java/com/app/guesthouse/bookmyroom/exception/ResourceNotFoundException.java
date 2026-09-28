package com.app.guesthouse.bookmyroom.exception;

/** Thrown when a room / guest / booking id does not exist -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
