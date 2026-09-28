package com.app.guesthouse.bookmyroom.controller;

import com.app.guesthouse.bookmyroom.dto.BookingResponse;
import com.app.guesthouse.bookmyroom.dto.GuestRequest;
import com.app.guesthouse.bookmyroom.dto.GuestResponse;
import com.app.guesthouse.bookmyroom.service.GuestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @PostMapping
    public ResponseEntity<GuestResponse> register(@Valid @RequestBody GuestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guestService.register(request));
    }

    /** ?search=ravi matches name or e-mail */
    @GetMapping
    public List<GuestResponse> list(@RequestParam(name = "search", required = false) String search) {
        return guestService.listGuests(search);
    }

    @GetMapping("/{id}")
    public GuestResponse get(@PathVariable("id") Long id) {
        return guestService.getGuest(id);
    }

    @GetMapping("/{id}/bookings")
    public List<BookingResponse> bookings(@PathVariable("id") Long id) {
        return guestService.getGuestBookings(id);
    }
}
