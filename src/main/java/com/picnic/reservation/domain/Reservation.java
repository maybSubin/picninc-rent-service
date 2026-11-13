package com.picnic.reservation.domain;

import com.picnic.global.exception.BusinessException;
import com.picnic.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservation", indexes = @Index(name = "idx_reservation_use_date", columnList = "useDate"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String customerName;

    @Column(nullable = false, length = 20)
    private String phone;

    /** 이용 날짜 (하루 단위 대여, 당일 수령·반납) */
    @Column(nullable = false)
    private LocalDate useDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReservationStatus status;

    @Column(nullable = false)
    private int totalPrice;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationItem> reservationItems = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime canceledAt;
    private LocalDateTime rentedAt;
    private LocalDateTime returnedAt;

    @Builder
    private Reservation(String customerName, String phone, LocalDate useDate) {
        this.customerName = customerName;
        this.phone = phone;
        this.useDate = useDate;
        this.status = ReservationStatus.RESERVED;
        this.createdAt = LocalDateTime.now();
    }

    public void addItem(ReservationItem reservationItem) {
        reservationItems.add(reservationItem);
        reservationItem.assignReservation(this);
        totalPrice += reservationItem.getSubtotal();
    }

    public void cancel() {
        requireStatus(ReservationStatus.RESERVED);
        this.status = ReservationStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }

    public void rent(LocalDate today) {
        requireStatus(ReservationStatus.RESERVED);
        if (!useDate.equals(today)) {
            throw new BusinessException(ErrorCode.NOT_USE_DATE,
                    "대여는 이용 당일(" + useDate + ")에만 가능합니다.");
        }
        this.status = ReservationStatus.RENTED;
        this.rentedAt = LocalDateTime.now();
    }

    public void returnItems() {
        requireStatus(ReservationStatus.RENTED);
        this.status = ReservationStatus.RETURNED;
        this.returnedAt = LocalDateTime.now();
    }

    private void requireStatus(ReservationStatus expected) {
        if (status != expected) {
            throw new BusinessException(ErrorCode.INVALID_STATUS,
                    "현재 상태(" + status + ")에서는 처리할 수 없습니다. " + expected + " 상태에서만 가능합니다.");
        }
    }
}
