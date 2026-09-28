package com.app.guesthouse.bookmyroom.repository;

import com.app.guesthouse.bookmyroom.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    Optional<Guest> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
