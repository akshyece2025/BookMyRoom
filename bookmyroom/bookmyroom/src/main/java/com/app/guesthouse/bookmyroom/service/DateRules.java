package com.app.guesthouse.bookmyroom.service;

import com.app.guesthouse.bookmyroom.exception.InvalidRequestException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Shared date checks used by availability search and booking creation. */
final class DateRules {

    static final int MAX_NIGHTS = 60;

    private DateRules() {
    }

    static void validateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidRequestException("Both check-in and check-out dates are required (yyyy-MM-dd)");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidRequestException("Check-out date (" + checkOut
                    + ") must be after check-in date (" + checkIn + ")");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Check-in date (" + checkIn + ") cannot be in the past");
        }
        if (ChronoUnit.DAYS.between(checkIn, checkOut) > MAX_NIGHTS) {
            throw new InvalidRequestException("A single booking cannot be longer than " + MAX_NIGHTS + " nights");
        }
    }
}
