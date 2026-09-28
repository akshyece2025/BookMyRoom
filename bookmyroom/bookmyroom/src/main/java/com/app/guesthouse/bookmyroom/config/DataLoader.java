package com.app.guesthouse.bookmyroom.config;

import com.app.guesthouse.bookmyroom.dto.BookingRequest;
import com.app.guesthouse.bookmyroom.dto.RoomRequest;
import com.app.guesthouse.bookmyroom.entity.RoomType;
import com.app.guesthouse.bookmyroom.repository.RoomRepository;
import com.app.guesthouse.bookmyroom.service.BookingService;
import com.app.guesthouse.bookmyroom.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Adds sample rooms and two bookings on first start, so the app can be tried immediately.
 * Turn off with app.seed-data=false in application.properties.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final boolean seedData;

    public DataLoader(RoomRepository roomRepository, RoomService roomService, BookingService bookingService,
                      @Value("${app.seed-data:true}") boolean seedData) {
        this.roomRepository = roomRepository;
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.seedData = seedData;
    }

    @Override
    public void run(String... args) {
        if (!seedData || roomRepository.count() > 0) {
            return;
        }
        Long r101 = roomService.addRoom(new RoomRequest("101", RoomType.SINGLE, 1, new BigDecimal("1200.00"))).id();
        roomService.addRoom(new RoomRequest("102", RoomType.SINGLE, 1, new BigDecimal("1200.00")));
        Long r201 = roomService.addRoom(new RoomRequest("201", RoomType.DOUBLE, 2, new BigDecimal("2000.00"))).id();
        roomService.addRoom(new RoomRequest("202", RoomType.DOUBLE, 2, new BigDecimal("2000.00")));
        roomService.addRoom(new RoomRequest("301", RoomType.DELUXE, 3, new BigDecimal("3500.00")));
        roomService.addRoom(new RoomRequest("401", RoomType.SUITE, 4, new BigDecimal("5000.00")));

        LocalDate today = LocalDate.now();
        bookingService.createBooking(new BookingRequest(r101, "Ravi Kumar", "ravi.kumar@example.com",
                "9876543210", today, today.plusDays(3), 1));
        bookingService.createBooking(new BookingRequest(r201, "Meena Iyer", "meena.iyer@example.com",
                "9123456780", today.plusDays(5), today.plusDays(8), 2));

        log.info("Sample data loaded: 6 rooms, 2 guests, 2 bookings");
    }
}
