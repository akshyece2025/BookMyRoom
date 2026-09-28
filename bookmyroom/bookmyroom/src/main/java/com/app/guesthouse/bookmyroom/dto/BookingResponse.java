package com.app.guesthouse.bookmyroom.dto;

import com.app.guesthouse.bookmyroom.entity.Booking;
import com.app.guesthouse.bookmyroom.entity.BookingStatus;
import com.app.guesthouse.bookmyroom.entity.RoomType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long roomId,
        String roomNumber,
        RoomType roomType,
        Long guestId,
        String guestName,
        String guestEmail,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Integer nights,
        Integer numberOfGuests,
        BigDecimal ratePerNight,
        BigDecimal totalCost,
        BookingStatus status,
        LocalDateTime createdAt,
        LocalDateTime cancelledAt) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getRoom().getId(),
                b.getRoom().getRoomNumber(),
                b.getRoom().getType(),
                b.getGuest().getId(),
                b.getGuest().getFullName(),
                b.getGuest().getEmail(),
                b.getCheckInDate(),
                b.getCheckOutDate(),
                b.getNights(),
                b.getNumberOfGuests(),
                b.getRatePerNight(),
                b.getTotalCost(),
                b.getStatus(),
                b.getCreatedAt(),
                b.getCancelledAt());
    }
}
