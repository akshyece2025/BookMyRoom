package com.app.guesthouse.bookmyroom.dto;

import com.app.guesthouse.bookmyroom.entity.RoomType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RoomRequest(

        @NotBlank(message = "Room number is required")
        @Size(max = 20, message = "Room number must be at most 20 characters")
        String roomNumber,

        @NotNull(message = "Room type is required (SINGLE, DOUBLE, DELUXE, SUITE, DORMITORY)")
        RoomType type,

        @NotNull(message = "Capacity is required")
        @Positive(message = "Capacity must be greater than 0")
        @Max(value = 20, message = "Capacity cannot be more than 20")
        Integer capacity,

        @NotNull(message = "Rate per night is required")
        @Positive(message = "Rate per night must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Rate per night can have at most 2 decimal places")
        BigDecimal ratePerNight
) {
}
