package com.picnic.reservation.dto;

import com.picnic.item.domain.ItemType;
import com.picnic.reservation.domain.Reservation;
import com.picnic.reservation.domain.ReservationItem;
import com.picnic.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReservationResponse(
        Long id,
        String customerName,
        String phone,
        LocalDate useDate,
        ReservationStatus status,
        int totalPrice,
        List<ReservationItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime canceledAt,
        LocalDateTime rentedAt,
        LocalDateTime returnedAt
) {
    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(r.getId(), r.getCustomerName(), r.getPhone(), r.getUseDate(),
                r.getStatus(), r.getTotalPrice(),
                r.getReservationItems().stream().map(ReservationItemResponse::from).toList(),
                r.getCreatedAt(), r.getCanceledAt(), r.getRentedAt(), r.getReturnedAt());
    }

    public record ReservationItemResponse(Long itemId, String name, ItemType type,
                                          int unitPrice, int quantity, int subtotal) {
        static ReservationItemResponse from(ReservationItem ri) {
            return new ReservationItemResponse(ri.getItem().getId(), ri.getItem().getName(), ri.getItem().getType(),
                    ri.getUnitPrice(), ri.getQuantity(), ri.getSubtotal());
        }
    }
}
