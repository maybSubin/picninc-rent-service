package com.picnic.log.repository;

import com.picnic.log.domain.ReservationLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReservationLogRepository extends MongoRepository<ReservationLog, String> {

    List<ReservationLog> findByReservationIdOrderByCreatedAtAsc(Long reservationId);
}
