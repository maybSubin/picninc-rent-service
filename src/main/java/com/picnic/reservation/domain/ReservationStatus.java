package com.picnic.reservation.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * RESERVED ─┬─ rent ──▶ RENTED ── return ──▶ RETURNED
 *           └─ cancel ─▶ CANCELED
 */
public enum ReservationStatus {
    RESERVED,
    RENTED,
    RETURNED,
    CANCELED;

    /** 재고를 점유하고 있는 상태 */
    public static final Set<ReservationStatus> OCCUPYING = EnumSet.of(RESERVED, RENTED);
}
