package com.app.guesthouse.bookmyroom.service;

import com.app.guesthouse.bookmyroom.dto.BookingResponse;
import com.app.guesthouse.bookmyroom.dto.GuestRequest;
import com.app.guesthouse.bookmyroom.dto.GuestResponse;
import com.app.guesthouse.bookmyroom.entity.Guest;
import com.app.guesthouse.bookmyroom.exception.BookingConflictException;
import com.app.guesthouse.bookmyroom.exception.ResourceNotFoundException;
import com.app.guesthouse.bookmyroom.repository.BookingRepository;
import com.app.guesthouse.bookmyroom.repository.GuestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class GuestService {

    private final GuestRepository guestRepository;
    private final BookingRepository bookingRepository;

    public GuestService(GuestRepository guestRepository, BookingRepository bookingRepository) {
        this.guestRepository = guestRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public GuestResponse register(GuestRequest request) {
        String email = request.email().trim().toLowerCase();
        if (guestRepository.existsByEmailIgnoreCase(email)) {
            throw new BookingConflictException("A guest with e-mail '" + email + "' is already registered");
        }
        Guest guest = guestRepository.save(new Guest(request.fullName().trim(), email, blankToNull(request.phone())));
        return toResponse(guest);
    }

    @Transactional(readOnly = true)
    public List<GuestResponse> listGuests(String search) {
        String q = (search == null) ? "" : search.trim().toLowerCase();
        return guestRepository.findAll().stream()
                .filter(g -> q.isEmpty()
                        || g.getFullName().toLowerCase().contains(q)
                        || g.getEmail().toLowerCase().contains(q))
                .sorted(Comparator.comparing(Guest::getFullName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GuestResponse getGuest(Long id) {
        return toResponse(findGuest(id));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getGuestBookings(Long id) {
        findGuest(id);
        return bookingRepository.findByGuestIdWithDetails(id).stream().map(BookingResponse::from).toList();
    }

    /** Used by BookingService: returns the existing guest for this e-mail or creates a new one. */
    @Transactional
    public Guest findOrCreate(String name, String email, String phone) {
        String normalisedEmail = email.trim().toLowerCase();
        return guestRepository.findByEmailIgnoreCase(normalisedEmail)
                .map(existing -> {
                    if (phone != null && !phone.isBlank()) {
                        existing.setPhone(phone.trim());
                    }
                    return existing;
                })
                .orElseGet(() -> guestRepository.save(new Guest(name.trim(), normalisedEmail, blankToNull(phone))));
    }

    private Guest findGuest(Long id) {
        return guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest with id " + id + " not found"));
    }

    private GuestResponse toResponse(Guest g) {
        return new GuestResponse(g.getId(), g.getFullName(), g.getEmail(), g.getPhone(),
                bookingRepository.countByGuestId(g.getId()));
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
