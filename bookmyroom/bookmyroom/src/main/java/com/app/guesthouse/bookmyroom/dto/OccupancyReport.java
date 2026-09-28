package com.app.guesthouse.bookmyroom.dto;

import java.math.BigDecimal;
import java.util.List;

public record OccupancyReport(
        String month,
        int daysInMonth,
        int totalRooms,
        long totalRoomNights,
        long bookedRoomNights,
        BigDecimal occupancyPercent,
        BigDecimal totalRevenue,
        int bookingsInMonth,
        List<RoomOccupancy> rooms) {
}
