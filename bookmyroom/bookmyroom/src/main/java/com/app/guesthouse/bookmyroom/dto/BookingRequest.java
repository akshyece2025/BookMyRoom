package com.app.guesthouse.bookmyroom.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Request to book a room. The guest is found by e-mail, or created if new.
 * Dates are in ISO format: yyyy-MM-dd
 */
public record BookingRequest(

        @NotNull(message = "Room id is required")
        @Positive(message = "Room id must be a positive number")
        Long roomId,

        @NotBlank(message = "Guest name is required")
        @Size(max = 100, message = "Guest name must be at most 100 characters")
        String guestName,

        @NotBlank(message = "Guest e-mail is required")
        @Email(message = "Guest e-mail must be a valid address")
        String guestEmail,

        @Pattern(regexp = "^[-0-9+ ]{7,20}$", message = "Phone must contain 7-20 digits (spaces, + and - allowed)")
        String guestPhone,

        @NotNull(message = "Check-in date is required (yyyy-MM-dd)")
        @FutureOrPresent(message = "Check-in date cannot be in the past")
        LocalDate checkInDate,

        @NotNull(message = "Check-out date is required (yyyy-MM-dd)")
        @Future(message = "Check-out date must be in the future")
        LocalDate checkOutDate,

        @NotNull(message = "Number of guests is required")
        @Min(value = 1, message = "Number of guests must be at least 1")
        Integer numberOfGuests
) {
}
