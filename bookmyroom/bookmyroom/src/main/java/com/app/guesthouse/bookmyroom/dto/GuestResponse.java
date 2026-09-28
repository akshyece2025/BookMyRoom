package com.app.guesthouse.bookmyroom.dto;

public record GuestResponse(Long id, String fullName, String email, String phone, long totalBookings) {
}
