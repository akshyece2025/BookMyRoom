package com.app.guesthouse.bookmyroom.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GuestRequest(

        @NotBlank(message = "Guest name is required")
        @Size(max = 100, message = "Guest name must be at most 100 characters")
        String fullName,

        @NotBlank(message = "E-mail is required")
        @Email(message = "E-mail must be a valid address")
        @Size(max = 120, message = "E-mail must be at most 120 characters")
        String email,

        @Pattern(regexp = "^[-0-9+ ]{7,20}$", message = "Phone must contain 7-20 digits (spaces, + and - allowed)")
        String phone
) {
}
