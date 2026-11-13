package com.picnic.reservation.repository;

import com.picnic.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("select distinct r from Reservation r " +
            "left join fetch r.reservationItems ri left join fetch ri.item where r.id = :id")
    Optional<Reservation> findWithItemsById(@Param("id") Long id);
}
