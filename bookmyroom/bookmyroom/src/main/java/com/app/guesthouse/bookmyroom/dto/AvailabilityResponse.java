package com.app.guesthouse.bookmyroom.dto;

import java.time.LocalDate;
import java.util.List;

public record AvailabilityResponse(LocalDate checkInDate, LocalDate checkOutDate, long nights,
                                   int guests, int availableCount, List<AvailableRoom> rooms) {
}
