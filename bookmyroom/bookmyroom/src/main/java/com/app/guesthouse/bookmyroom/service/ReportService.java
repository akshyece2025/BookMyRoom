package com.app.guesthouse.bookmyroom.service;

import com.app.guesthouse.bookmyroom.dto.DashboardResponse;
import com.app.guesthouse.bookmyroom.dto.OccupancyReport;
import com.app.guesthouse.bookmyroom.dto.RoomOccupancy;
import com.app.guesthouse.bookmyroom.entity.Booking;
import com.app.guesthouse.bookmyroom.entity.BookingStatus;
import com.app.guesthouse.bookmyroom.entity.Room;
import com.app.guesthouse.bookmyroom.exception.InvalidRequestException;
import com.app.guesthouse.bookmyroom.repository.BookingRepository;
import com.app.guesthouse.bookmyroom.repository.GuestRepository;
import com.app.guesthouse.bookmyroom.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReportService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;

    public ReportService(RoomRepository roomRepository, BookingRepository bookingRepository,
                         GuestRepository guestRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.guestRepository = guestRepository;
    }

    // ---------- Feature 5: occupancy report for a month ----------

    /** @param month in the format yyyy-MM, e.g. 2026-10 */
    @Transactional(readOnly = true)
    public OccupancyReport occupancyReport(String month) {
        YearMonth ym;
        try {
            ym = YearMonth.parse(month.trim());
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new InvalidRequestException("Month must be in the format yyyy-MM, for example 2026-10");
        }

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1); // exclusive
        int days = ym.lengthOfMonth();

        List<Room> rooms = new ArrayList<>(roomRepository.findAll());
        rooms.sort(Comparator.comparing(Room::getRoomNumber));
        List<Booking> bookings = bookingRepository.findInPeriod(start, end, BookingStatus.CONFIRMED);

        Map<Long, Long> nightsByRoom = new HashMap<>();
        Map<Long, BigDecimal> revenueByRoom = new HashMap<>();
        for (Booking b : bookings) {
            // Only count the nights that fall inside this month
            LocalDate from = b.getCheckInDate().isBefore(start) ? start : b.getCheckInDate();
            LocalDate to = b.getCheckOutDate().isAfter(end) ? end : b.getCheckOutDate();
            long nights = ChronoUnit.DAYS.between(from, to);
            if (nights <= 0) {
                continue;
            }
            Long roomId = b.getRoom().getId();
            nightsByRoom.merge(roomId, nights, Long::sum);
            revenueByRoom.merge(roomId, b.getRatePerNight().multiply(BigDecimal.valueOf(nights)), BigDecimal::add);
        }

        List<RoomOccupancy> perRoom = new ArrayList<>();
        long bookedTotal = 0;
        BigDecimal revenueTotal = BigDecimal.ZERO;
        for (Room r : rooms) {
            long nights = nightsByRoom.getOrDefault(r.getId(), 0L);
            BigDecimal revenue = revenueByRoom.getOrDefault(r.getId(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            perRoom.add(new RoomOccupancy(r.getId(), r.getRoomNumber(), r.getType(), nights,
                    percent(nights, days), revenue));
            bookedTotal += nights;
            revenueTotal = revenueTotal.add(revenue);
        }

        long totalRoomNights = (long) rooms.size() * days;
        return new OccupancyReport(ym.toString(), days, rooms.size(), totalRoomNights, bookedTotal,
                percent(bookedTotal, totalRoomNights), revenueTotal.setScale(2, RoundingMode.HALF_UP),
                bookings.size(), perRoom);
    }

    // ---------- Extra: dashboard counts ----------

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        LocalDate today = LocalDate.now();
        List<Booking> confirmed = bookingRepository.findAllWithDetails().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .toList();

        Set<Long> occupiedRoomIds = new HashSet<>();
        long upcoming = 0;
        BigDecimal revenue = BigDecimal.ZERO;
        for (Booking b : confirmed) {
            if (!b.getCheckInDate().isAfter(today) && b.getCheckOutDate().isAfter(today)) {
                occupiedRoomIds.add(b.getRoom().getId());
            }
            if (!b.getCheckInDate().isBefore(today) && b.getCheckInDate().isBefore(today.plusDays(7))) {
                upcoming++;
            }
            revenue = revenue.add(b.getTotalCost());
        }

        long totalRooms = roomRepository.count();
        return new DashboardResponse(
                totalRooms,
                guestRepository.count(),
                bookingRepository.count(),
                confirmed.size(),
                bookingRepository.countByStatus(BookingStatus.CANCELLED),
                occupiedRoomIds.size(),
                totalRooms - occupiedRoomIds.size(),
                upcoming,
                revenue.setScale(2, RoundingMode.HALF_UP));
    }

    private static BigDecimal percent(long part, long whole) {
        if (whole == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(part * 100.0 / whole).setScale(2, RoundingMode.HALF_UP);
    }
}
