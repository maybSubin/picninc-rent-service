package com.picnic.reservation.service;

import com.picnic.global.exception.BusinessException;
import com.picnic.global.exception.ErrorCode;
import com.picnic.item.domain.Item;
import com.picnic.item.repository.ItemRepository;
import com.picnic.reservation.domain.Reservation;
import com.picnic.reservation.domain.ReservationItem;
import com.picnic.reservation.domain.ReservationStatus;
import com.picnic.reservation.dto.ReservationCreateRequest;
import com.picnic.reservation.dto.ReservationCreateRequest.ItemQuantity;
import com.picnic.reservation.dto.ReservationResponse;
import com.picnic.reservation.event.ReservationAction;
import com.picnic.reservation.event.ReservationActivityEvent;
import com.picnic.reservation.repository.ReservationItemRepository;
import com.picnic.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final ItemRepository itemRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public ReservationResponse create(ReservationCreateRequest request) {
        LocalDate useDate = request.useDate();
        if (useDate.isBefore(LocalDate.now(clock))) {
            throw new BusinessException(ErrorCode.PAST_USE_DATE);
        }

        Map<Long, Integer> quantities = toQuantityMap(request.items());

        // 1. 용품 조회 (행 잠금으로 동시 예약 시 초과 예약 방지)
        List<Item> items = itemRepository.findAllByIdForUpdate(quantities.keySet());
        if (items.size() != quantities.size()) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }

        // 2. 기본 용품 검증: 텐트 또는 돗자리 중 한 종류를 1개
        validateBasicItem(items, quantities);

        // 3. 날짜별 재고 확인
        validateStock(useDate, items, quantities);

        // 4. 예약 생성 및 가격 계산 (단가 x 수량, 하루 단위)
        Reservation reservation = Reservation.builder()
                .customerName(request.customerName())
                .phone(request.phone())
                .useDate(useDate)
                .build();
        items.forEach(item -> reservation.addItem(new ReservationItem(item, quantities.get(item.getId()))));
        reservationRepository.save(reservation);

        publish(reservation, ReservationAction.CREATED, null,
                "예약 생성 - 용품 " + items.size() + "종, 총 " + reservation.getTotalPrice() + "원");
        return ReservationResponse.from(reservation);
    }

    public ReservationResponse findById(Long reservationId) {
        return ReservationResponse.from(getReservation(reservationId));
    }

    @Transactional
    public ReservationResponse cancel(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        ReservationStatus before = reservation.getStatus();
        reservation.cancel();
        publish(reservation, ReservationAction.CANCELED, before, "예약 취소");
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse rent(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        ReservationStatus before = reservation.getStatus();
        reservation.rent(LocalDate.now(clock));
        publish(reservation, ReservationAction.RENTED, before, "용품 수령(대여 시작)");
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse returnItems(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        ReservationStatus before = reservation.getStatus();
        reservation.returnItems();
        publish(reservation, ReservationAction.RETURNED, before, "용품 반납 완료");
        return ReservationResponse.from(reservation);
    }

    private Map<Long, Integer> toQuantityMap(List<ItemQuantity> requested) {
        try {
            return requested.stream()
                    .collect(Collectors.toMap(ItemQuantity::itemId, ItemQuantity::quantity));
        } catch (IllegalStateException duplicateKey) {
            throw new BusinessException(ErrorCode.DUPLICATED_ITEM);
        }
    }

    private void validateBasicItem(List<Item> items, Map<Long, Integer> quantities) {
        List<Item> basics = items.stream().filter(Item::isBasic).toList();
        if (basics.size() != 1 || quantities.get(basics.get(0).getId()) != 1) {
            throw new BusinessException(ErrorCode.BASIC_ITEM_REQUIRED);
        }
    }

    private void validateStock(LocalDate useDate, List<Item> items, Map<Long, Integer> quantities) {
        Map<Long, Integer> reserved = reservationItemRepository.sumReservedQuantityByItem(useDate, quantities.keySet());
        for (Item item : items) {
            int available = item.getStock() - reserved.getOrDefault(item.getId(), 0);
            int requested = quantities.get(item.getId());
            if (requested > available) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK,
                        String.format("%s의 %s 예약 가능 수량이 부족합니다. (요청 %d개 / 가능 %d개)",
                                item.getName(), useDate, requested, Math.max(available, 0)));
            }
        }
    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findWithItemsById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private void publish(Reservation r, ReservationAction action, ReservationStatus before, String message) {
        eventPublisher.publishEvent(new ReservationActivityEvent(r.getId(), action, before, r.getStatus(),
                r.getCustomerName(), r.getUseDate(), r.getTotalPrice(), message, LocalDateTime.now(clock)));
    }
}
