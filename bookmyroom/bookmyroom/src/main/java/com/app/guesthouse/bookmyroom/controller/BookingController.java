package com.app.guesthouse.bookmyroom.controller;

import com.app.guesthouse.bookmyroom.dto.BookingRequest;
import com.app.guesthouse.bookmyroom.dto.BookingResponse;
import com.app.guesthouse.bookmyroom.entity.BookingStatus;
import com.app.guesthouse.bookmyroom.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** Feature 3 - create booking. 201 Created / 400 invalid / 404 room missing / 409 dates overlap */
    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    /** List / search bookings: ?status=CONFIRMED&roomId=1&guestEmail=ravi&from=2026-10-01&to=2026-10-31 */
    @GetMapping
    public List<BookingResponse> list(
            @RequestParam(name = "status", required = false) BookingStatus status,
            @RequestParam(name = "roomId", required = false) Long roomId,
            @RequestParam(name = "guestEmail", required = false) String guestEmail,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return bookingService.listBookings(status, roomId, guestEmail, from, to);
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable("id") Long id) {
        return bookingService.getBooking(id);
    }

    /** Feature 4 - cancel booking and free the room. 200 OK / 404 / 409 already cancelled */
    @DeleteMapping("/{id}")
    public BookingResponse cancel(@PathVariable("id") Long id) {
        return bookingService.cancelBooking(id);
    }

    /** Same as DELETE, for clients that prefer PUT. */
    @PutMapping("/{id}/cancel")
    public BookingResponse cancelWithPut(@PathVariable("id") Long id) {
        return bookingService.cancelBooking(id);
    }
}
