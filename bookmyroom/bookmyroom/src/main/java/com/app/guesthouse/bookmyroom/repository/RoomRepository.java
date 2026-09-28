package com.app.guesthouse.bookmyroom.repository;

import com.app.guesthouse.bookmyroom.entity.BookingStatus;
import com.app.guesthouse.bookmyroom.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomNumberIgnoreCase(String roomNumber);

    boolean existsByRoomNumberIgnoreCase(String roomNumber);

    /**
     * Loads the room with a database row lock. Two staff members booking the same room
     * at the same moment are processed one after the other, so no double booking can slip through.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);

    /**
     * Rooms with enough capacity that have NO confirmed booking overlapping [checkIn, checkOut).
     * Two ranges overlap when existing.checkIn < new.checkOut AND existing.checkOut > new.checkIn.
     */
    @Query("select r from Room r where r.capacity >= :guests and not exists ("
            + " select b.id from Booking b where b.room = r and b.status = :status"
            + " and b.checkInDate < :checkOut and b.checkOutDate > :checkIn)"
            + " order by r.roomNumber")
    List<Room> findAvailableRooms(@Param("checkIn") LocalDate checkIn,
                                  @Param("checkOut") LocalDate checkOut,
                                  @Param("guests") Integer guests,
                                  @Param("status") BookingStatus status);
}
