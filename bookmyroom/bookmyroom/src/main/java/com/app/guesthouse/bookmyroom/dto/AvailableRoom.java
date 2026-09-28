package com.app.guesthouse.bookmyroom.dto;

import com.app.guesthouse.bookmyroom.entity.RoomType;

import java.math.BigDecimal;

/** A free room for the searched dates, with the price the guest would pay. */
public record AvailableRoom(Long id, String roomNumber, RoomType type, Integer capacity,
                            BigDecimal ratePerNight, BigDecimal estimatedTotalCost) {
}
