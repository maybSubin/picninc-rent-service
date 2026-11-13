package com.picnic.reservation.repository;

import com.picnic.reservation.domain.ReservationItem;
import com.picnic.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ReservationItemRepository extends JpaRepository<ReservationItem, Long> {

    @Query("select ri.item.id, sum(ri.quantity) from ReservationItem ri " +
            "where ri.reservation.useDate = :date " +
            "and ri.reservation.status in :statuses " +
            "and ri.item.id in :itemIds " +
            "group by ri.item.id")
    List<Object[]> sumQuantityByItem(@Param("date") LocalDate date,
                                     @Param("statuses") Collection<ReservationStatus> statuses,
                                     @Param("itemIds") Collection<Long> itemIds);

    /** 해당 날짜에 재고를 점유 중인(예약·대여 중) 수량을 용품별로 합산 */
    default Map<Long, Integer> sumReservedQuantityByItem(LocalDate date, Collection<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Map.of();
        }
        return sumQuantityByItem(date, ReservationStatus.OCCUPYING, itemIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()));
    }
}
