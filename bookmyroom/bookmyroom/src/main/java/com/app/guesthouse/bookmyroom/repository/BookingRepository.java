package com.app.guesthouse.bookmyroom.repository;

import com.app.guesthouse.bookmyroom.entity.Booking;
import com.app.guesthouse.bookmyroom.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** Existing bookings of a room that overlap the requested range (used to reject double booking). */
    @Query("select b from Booking b where b.room.id = :roomId and b.status = :status"
            + " and b.checkInDate < :checkOut and b.checkOutDate > :checkIn")
    List<Booking> findOverlapping(@Param("roomId") Long roomId,
                                  @Param("checkIn") LocalDate checkIn,
                                  @Param("checkOut") LocalDate checkOut,
                                  @Param("status") BookingStatus status);

    /** Bookings that touch the period [start, end) - used for the monthly occupancy report. */
    @Query("select b from Booking b join fetch b.room where b.status = :status"
            + " and b.checkInDate < :end and b.checkOutDate > :start")
    List<Booking> findInPeriod(@Param("start") LocalDate start,
                               @Param("end") LocalDate end,
                               @Param("status") BookingStatus status);

    @Query("select b from Booking b join fetch b.room join fetch b.guest order by b.checkInDate desc, b.id desc")
    List<Booking> findAllWithDetails();

    @Query("select b from Booking b join fetch b.room join fetch b.guest where b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    @Query("select b from Booking b join fetch b.room join fetch b.guest where b.guest.id = :guestId"
            + " order by b.checkInDate desc")
    List<Booking> findByGuestIdWithDetails(@Param("guestId") Long guestId);

    long countByStatus(BookingStatus status);

    long countByRoomId(Long roomId);

    long countByGuestId(Long guestId);
}
