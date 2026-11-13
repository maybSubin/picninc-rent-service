package com.picnic.reservation.domain;

import com.picnic.item.domain.Item;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservation_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(nullable = false)
    private int quantity;

    /** 예약 시점의 1일 단가 (이후 용품 가격이 바뀌어도 예약 금액 유지) */
    @Column(nullable = false)
    private int unitPrice;

    @Column(nullable = false)
    private int subtotal;

    public ReservationItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
        this.unitPrice = item.getPrice();
        this.subtotal = unitPrice * quantity;
    }

    void assignReservation(Reservation reservation) {
        this.reservation = reservation;
    }
}
