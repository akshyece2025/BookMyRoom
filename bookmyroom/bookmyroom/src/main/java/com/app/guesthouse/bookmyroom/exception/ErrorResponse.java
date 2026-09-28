package com.app.guesthouse.bookmyroom.exception;

import java.time.LocalDateTime;
import java.util.Map;

/** Uniform JSON body returned for every error - no stack traces reach the client. */
public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message,
                            String path, Map<String, String> fieldErrors) {
}
