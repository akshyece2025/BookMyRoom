package com.app.guesthouse.bookmyroom.controller;

import com.app.guesthouse.bookmyroom.dto.AvailabilityResponse;
import com.app.guesthouse.bookmyroom.dto.RoomRequest;
import com.app.guesthouse.bookmyroom.dto.RoomResponse;
import com.app.guesthouse.bookmyroom.entity.RoomType;
import com.app.guesthouse.bookmyroom.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /** Feature 1 - add a room. 201 Created / 400 invalid / 409 duplicate room number */
    @PostMapping
    public ResponseEntity<RoomResponse> addRoom(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.addRoom(request));
    }

    /** List rooms, optional filters: ?type=DELUXE&minCapacity=2&maxRate=3000 */
    @GetMapping
    public List<RoomResponse> listRooms(@RequestParam(name = "type", required = false) RoomType type,
                                        @RequestParam(name = "minCapacity", required = false) Integer minCapacity,
                                        @RequestParam(name = "maxRate", required = false) BigDecimal maxRate) {
        return roomService.listRooms(type, minCapacity, maxRate);
    }

    /** Feature 2 - availability: /api/rooms/available?checkIn=2026-10-10&checkOut=2026-10-12&guests=2 */
    @GetMapping("/available")
    public AvailabilityResponse available(
            @RequestParam(name = "checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(name = "checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(name = "guests", required = false, defaultValue = "1") Integer guests,
            @RequestParam(name = "type", required = false) RoomType type) {
        return roomService.findAvailableRooms(checkIn, checkOut, guests, type);
    }

    @GetMapping("/{id}")
    public RoomResponse getRoom(@PathVariable("id") Long id) {
        return roomService.getRoom(id);
    }

    @PutMapping("/{id}")
    public RoomResponse updateRoom(@PathVariable("id") Long id, @Valid @RequestBody RoomRequest request) {
        return roomService.updateRoom(id, request);
    }

    /** 204 No Content / 404 / 409 if the room has bookings */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("id") Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
