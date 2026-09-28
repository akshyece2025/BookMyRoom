package com.app.guesthouse.bookmyroom.service;

import com.app.guesthouse.bookmyroom.dto.AvailabilityResponse;
import com.app.guesthouse.bookmyroom.dto.AvailableRoom;
import com.app.guesthouse.bookmyroom.dto.RoomRequest;
import com.app.guesthouse.bookmyroom.dto.RoomResponse;
import com.app.guesthouse.bookmyroom.entity.BookingStatus;
import com.app.guesthouse.bookmyroom.entity.Room;
import com.app.guesthouse.bookmyroom.entity.RoomType;
import com.app.guesthouse.bookmyroom.exception.BookingConflictException;
import com.app.guesthouse.bookmyroom.exception.InvalidRequestException;
import com.app.guesthouse.bookmyroom.exception.ResourceNotFoundException;
import com.app.guesthouse.bookmyroom.repository.BookingRepository;
import com.app.guesthouse.bookmyroom.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    // ---------- Feature 1: add rooms ----------

    @Transactional
    public RoomResponse addRoom(RoomRequest request) {
        String number = request.roomNumber().trim();
        if (roomRepository.existsByRoomNumberIgnoreCase(number)) {
            throw new BookingConflictException("Room number '" + number + "' already exists");
        }
        Room room = new Room(number, request.type(), request.capacity(), request.ratePerNight());
        return RoomResponse.from(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = findRoom(id);
        String number = request.roomNumber().trim();
        roomRepository.findByRoomNumberIgnoreCase(number)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BookingConflictException("Room number '" + number + "' is already used by another room");
                });
        room.setRoomNumber(number);
        room.setType(request.type());
        room.setCapacity(request.capacity());
        room.setRatePerNight(request.ratePerNight()); // existing bookings keep the rate they were booked at
        return RoomResponse.from(roomRepository.save(room));
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = findRoom(id);
        if (bookingRepository.countByRoomId(id) > 0) {
            throw new BookingConflictException("Room " + room.getRoomNumber()
                    + " has booking history and cannot be deleted");
        }
        roomRepository.delete(room);
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoom(Long id) {
        return RoomResponse.from(findRoom(id));
    }

    /** List rooms with optional filters (type, minimum capacity, maximum rate). */
    @Transactional(readOnly = true)
    public List<RoomResponse> listRooms(RoomType type, Integer minCapacity, BigDecimal maxRate) {
        return roomRepository.findAll().stream()
                .filter(r -> type == null || r.getType() == type)
                .filter(r -> minCapacity == null || r.getCapacity() >= minCapacity)
                .filter(r -> maxRate == null || r.getRatePerNight().compareTo(maxRate) <= 0)
                .sorted(Comparator.comparing(Room::getRoomNumber))
                .map(RoomResponse::from)
                .toList();
    }

    // ---------- Feature 2: check availability ----------

    @Transactional(readOnly = true)
    public AvailabilityResponse findAvailableRooms(LocalDate checkIn, LocalDate checkOut, Integer guests, RoomType type) {
        DateRules.validateRange(checkIn, checkOut);
        int guestCount = (guests == null) ? 1 : guests;
        if (guestCount < 1) {
            throw new InvalidRequestException("Number of guests must be at least 1");
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

        List<AvailableRoom> rooms = roomRepository
                .findAvailableRooms(checkIn, checkOut, guestCount, BookingStatus.CONFIRMED).stream()
                .filter(r -> type == null || r.getType() == type)
                .map(r -> new AvailableRoom(r.getId(), r.getRoomNumber(), r.getType(), r.getCapacity(),
                        r.getRatePerNight(), r.getRatePerNight().multiply(BigDecimal.valueOf(nights))))
                .toList();

        return new AvailabilityResponse(checkIn, checkOut, nights, guestCount, rooms.size(), rooms);
    }

    Room findRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room with id " + id + " not found"));
    }
}
