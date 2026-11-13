package com.picnic.log.domain;

import com.picnic.reservation.event.ReservationAction;
import com.picnic.reservation.event.ReservationActivityEvent;
import com.picnic.reservation.domain.ReservationStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 예약·대여 활동 로그 (MongoDB 컬렉션: reservation_logs) */
@Document(collection = "reservation_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationLog {

    @Id
    private String id;

    @Indexed
    private Long reservationId;

    private ReservationAction action;
    private ReservationStatus fromStatus;
    private ReservationStatus toStatus;
    private String customerName;
    private LocalDate useDate;
    private int totalPrice;
    private String message;
    private LocalDateTime createdAt;

    public static ReservationLog from(ReservationActivityEvent e) {
        ReservationLog log = new ReservationLog();
        log.reservationId = e.reservationId();
        log.action = e.action();
        log.fromStatus = e.fromStatus();
        log.toStatus = e.toStatus();
        log.customerName = e.customerName();
        log.useDate = e.useDate();
        log.totalPrice = e.totalPrice();
        log.message = e.message();
        log.createdAt = e.occurredAt();
        return log;
    }
}
