package com.app.guesthouse.bookmyroom;

import com.app.guesthouse.bookmyroom.dto.BookingRequest;
import com.app.guesthouse.bookmyroom.dto.BookingResponse;
import com.app.guesthouse.bookmyroom.dto.OccupancyReport;
import com.app.guesthouse.bookmyroom.dto.RoomOccupancy;
import com.app.guesthouse.bookmyroom.dto.RoomRequest;
import com.app.guesthouse.bookmyroom.entity.BookingStatus;
import com.app.guesthouse.bookmyroom.entity.RoomType;
import com.app.guesthouse.bookmyroom.exception.BookingConflictException;
import com.app.guesthouse.bookmyroom.exception.InvalidRequestException;
import com.app.guesthouse.bookmyroom.exception.ResourceNotFoundException;
import com.app.guesthouse.bookmyroom.service.BookingService;
import com.app.guesthouse.bookmyroom.service.ReportService;
import com.app.guesthouse.bookmyroom.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

/** Checks the business rules from the problem statement. Each test rolls back its data. */
@SpringBootTest
@Transactional
class BookingRulesTests {

    @Autowired
    private RoomService roomService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ReportService reportService;

    private final LocalDate base = LocalDate.now().plusDays(30);

    private Long newRoom(String number, String rate) {
        return roomService.addRoom(new RoomRequest(number, RoomType.DOUBLE, 2, new BigDecimal(rate))).id();
    }

    private BookingResponse book(Long roomId, LocalDate in, LocalDate out) {
        return bookingService.createBooking(new BookingRequest(roomId, "Test Guest", "test.guest@example.com",
                null, in, out, 1));
    }

    @Test
    void totalCostIsNightsTimesRate() {
        Long room = newRoom("T-1", "1500.00");
        BookingResponse b = book(room, base, base.plusDays(3));
        assertEquals(3, b.nights().intValue());
        assertEquals(0, new BigDecimal("4500.00").compareTo(b.totalCost()));
        assertEquals(BookingStatus.CONFIRMED, b.status());
    }

    @Test
    void overlappingBookingIsRejected() {
        Long room = newRoom("T-2", "1000.00");
        book(room, base, base.plusDays(4));
        assertThrows(BookingConflictException.class, () -> book(room, base.plusDays(2), base.plusDays(6)));
        assertThrows(BookingConflictException.class, () -> book(room, base.minusDays(1), base.plusDays(1)));
        assertThrows(BookingConflictException.class, () -> book(room, base.plusDays(1), base.plusDays(2)));
    }

    @Test
    void backToBackBookingIsAllowed() {
        Long room = newRoom("T-3", "1000.00");
        book(room, base, base.plusDays(2));
        BookingResponse next = book(room, base.plusDays(2), base.plusDays(4)); // check-in on previous check-out day
        assertNotNull(next.id());
    }

    @Test
    void cancellingFreesTheRoom() {
        Long room = newRoom("T-4", "1000.00");
        BookingResponse first = book(room, base, base.plusDays(3));
        BookingResponse cancelled = bookingService.cancelBooking(first.id());
        assertEquals(BookingStatus.CANCELLED, cancelled.status());
        assertNotNull(book(room, base, base.plusDays(3)).id());
        assertThrows(BookingConflictException.class, () -> bookingService.cancelBooking(first.id()));
    }

    @Test
    void invalidRequestsAreRejected() {
        Long room = newRoom("T-5", "1000.00");
        assertThrows(InvalidRequestException.class, () -> book(room, base.plusDays(3), base));
        assertThrows(InvalidRequestException.class, () -> book(room, base, base));
        assertThrows(InvalidRequestException.class,
                () -> book(room, LocalDate.now().minusDays(2), LocalDate.now().plusDays(1)));
        assertThrows(InvalidRequestException.class, () -> bookingService.createBooking(new BookingRequest(
                room, "Big Family", "family@example.com", null, base, base.plusDays(1), 5)));
        assertThrows(ResourceNotFoundException.class, () -> book(999999L, base, base.plusDays(1)));
    }

    @Test
    void duplicateRoomNumberIsRejected() {
        newRoom("T-6", "1000.00");
        assertThrows(BookingConflictException.class, () -> newRoom("t-6", "900.00"));
    }

    @Test
    void occupancyCountsOnlyNightsInsideTheMonth() {
        Long room = newRoom("T-7", "1000.00");
        YearMonth month = YearMonth.now().plusMonths(3);
        // 2 nights in 'month' (last two days) + 2 nights in the next month
        book(room, month.atEndOfMonth().minusDays(1), month.plusMonths(1).atDay(3));

        OccupancyReport report = reportService.occupancyReport(month.toString());
        RoomOccupancy row = report.rooms().stream()
                .filter(r -> r.roomId().equals(room)).findFirst().orElseThrow();
        assertEquals(2, row.bookedNights());
        assertEquals(0, new BigDecimal("2000.00").compareTo(row.revenue()));

        assertThrows(InvalidRequestException.class, () -> reportService.occupancyReport("2026/10"));
    }
}
