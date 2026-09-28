package com.app.guesthouse.bookmyroom.service;

import com.app.guesthouse.bookmyroom.dto.BookingRequest;
import com.app.guesthouse.bookmyroom.dto.BookingResponse;
import com.app.guesthouse.bookmyroom.entity.Booking;
import com.app.guesthouse.bookmyroom.entity.BookingStatus;
import com.app.guesthouse.bookmyroom.entity.Guest;
import com.app.guesthouse.bookmyroom.entity.Room;
import com.app.guesthouse.bookmyroom.exception.BookingConflictException;
import com.app.guesthouse.bookmyroom.exception.InvalidRequestException;
import com.app.guesthouse.bookmyroom.exception.ResourceNotFoundException;
import com.app.guesthouse.bookmyroom.repository.BookingRepository;
import com.app.guesthouse.bookmyroom.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final GuestService guestService;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository,
                          GuestService guestService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.guestService = guestService;
    }

    // ---------- Feature 3: create a booking only if the room is free ----------

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        LocalDate checkIn = request.checkInDate();
        LocalDate checkOut = request.checkOutDate();

        // Rule: valid date range (check-out after check-in, not in the past)
        DateRules.validateRange(checkIn, checkOut);

        // Lock the room row so two simultaneous requests for the same room cannot both succeed
        Room room = roomRepository.findByIdForUpdate(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room with id " + request.roomId() + " not found"));

        // Rule: guests must fit in the room
        if (request.numberOfGuests() > room.getCapacity()) {
            throw new InvalidRequestException("Room " + room.getRoomNumber() + " holds at most "
                    + room.getCapacity() + " guest(s), but " + request.numberOfGuests() + " were requested");
        }

        // Rule: two bookings for the same room cannot have overlapping dates - checked BEFORE saving
        List<Booking> clashes = bookingRepository.findOverlapping(room.getId(), checkIn, checkOut, BookingStatus.CONFIRMED);
        if (!clashes.isEmpty()) {
            String details = clashes.stream()
                    .map(b -> "booking #" + b.getId() + " (" + b.getCheckInDate() + " to " + b.getCheckOutDate() + ")")
                    .collect(Collectors.joining(", "));
            throw new BookingConflictException("Room " + room.getRoomNumber() + " is not available from "
                    + checkIn + " to " + checkOut + ". It overlaps with " + details + ".");
        }

        Guest guest = guestService.findOrCreate(request.guestName(), request.guestEmail(), request.guestPhone());

        // Rule: total cost = nights stayed x room rate
        int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal total = room.getRatePerNight().multiply(BigDecimal.valueOf(nights));

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setGuest(guest);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setNumberOfGuests(request.numberOfGuests());
        booking.setNights(nights);
        booking.setRatePerNight(room.getRatePerNight());
        booking.setTotalCost(total);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreatedAt(LocalDateTime.now());

        return BookingResponse.from(bookingRepository.save(booking));
    }

    // ---------- Feature 4: cancel a booking and free the room ----------

    @Transactional
    public BookingResponse cancelBooking(Long id) {
        Booking booking = findBooking(id);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingConflictException("Booking #" + id + " is already cancelled");
        }
        if (!booking.getCheckOutDate().isAfter(LocalDate.now())) {
            throw new InvalidRequestException("Booking #" + id + " ended on " + booking.getCheckOutDate()
                    + " and can no longer be cancelled");
        }
        // Only CONFIRMED bookings block a room, so changing the status frees the dates immediately
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long id) {
        return BookingResponse.from(findBooking(id));
    }

    /**
     * List bookings with optional filters. from/to returns bookings whose stay overlaps that period.
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> listBookings(BookingStatus status, Long roomId, String guestEmail,
                                              LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new InvalidRequestException("'to' date must not be before 'from' date");
        }
        String email = (guestEmail == null) ? "" : guestEmail.trim().toLowerCase();
        return bookingRepository.findAllWithDetails().stream()
                .filter(b -> status == null || b.getStatus() == status)
                .filter(b -> roomId == null || b.getRoom().getId().equals(roomId))
                .filter(b -> email.isEmpty() || b.getGuest().getEmail().toLowerCase().contains(email))
                .filter(b -> from == null || b.getCheckOutDate().isAfter(from))
                .filter(b -> to == null || !b.getCheckInDate().isAfter(to))
                .map(BookingResponse::from)
                .toList();
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with id " + id + " not found"));
    }
}
