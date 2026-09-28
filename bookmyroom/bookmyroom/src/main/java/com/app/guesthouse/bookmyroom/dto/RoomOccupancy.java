package com.app.guesthouse.bookmyroom.dto;

import com.app.guesthouse.bookmyroom.entity.RoomType;

import java.math.BigDecimal;

public record RoomOccupancy(Long roomId, String roomNumber, RoomType type,
                            long bookedNights, BigDecimal occupancyPercent, BigDecimal revenue) {
}
