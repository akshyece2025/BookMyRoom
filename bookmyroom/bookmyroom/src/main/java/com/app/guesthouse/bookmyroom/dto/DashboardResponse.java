package com.app.guesthouse.bookmyroom.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        long totalRooms,
        long totalGuests,
        long totalBookings,
        long confirmedBookings,
        long cancelledBookings,
        long roomsOccupiedToday,
        long roomsAvailableToday,
        long checkInsNext7Days,
        BigDecimal confirmedRevenue) {
}
