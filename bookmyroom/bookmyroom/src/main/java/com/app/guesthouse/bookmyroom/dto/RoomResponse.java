package com.app.guesthouse.bookmyroom.dto;

import com.app.guesthouse.bookmyroom.entity.Room;
import com.app.guesthouse.bookmyroom.entity.RoomType;

import java.math.BigDecimal;

public record RoomResponse(Long id, String roomNumber, RoomType type, Integer capacity, BigDecimal ratePerNight) {

    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getRoomNumber(), room.getType(),
                room.getCapacity(), room.getRatePerNight());
    }
}
