package com.picnic.reservation.event;

import com.picnic.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 예약 관련 작업이 일어났음을 알리는 이벤트 (커밋 성공 후 MongoDB 로그로 저장됨) */
public record ReservationActivityEvent(
        Long reservationId,
        ReservationAction action,
        ReservationStatus fromStatus,
        ReservationStatus toStatus,
        String customerName,
        LocalDate useDate,
        int totalPrice,
        String message,
        LocalDateTime occurredAt
) {
}
