package com.picnic.item.service;

import com.picnic.global.exception.BusinessException;
import com.picnic.global.exception.ErrorCode;
import com.picnic.item.domain.Item;
import com.picnic.item.domain.ItemType;
import com.picnic.item.dto.ItemAvailabilityResponse;
import com.picnic.item.dto.ItemCreateRequest;
import com.picnic.item.dto.ItemResponse;
import com.picnic.item.repository.ItemRepository;
import com.picnic.reservation.repository.ReservationItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    private final ReservationItemRepository reservationItemRepository;

    @Transactional
    public ItemResponse create(ItemCreateRequest request) {
        return ItemResponse.from(itemRepository.save(request.toEntity()));
    }

    public List<ItemResponse> findAll(ItemType type) {
        List<Item> items = (type == null)
                ? itemRepository.findAllByOrderByIdAsc()
                : itemRepository.findByTypeOrderByIdAsc(type);
        return items.stream().map(ItemResponse::from).toList();
    }

    public ItemResponse findById(Long id) {
        return ItemResponse.from(getItem(id));
    }

    /** 이용 날짜 기준 용품별 예약 가능 수량 */
    public List<ItemAvailabilityResponse> findAvailability(LocalDate date, ItemType type) {
        List<Item> items = (type == null)
                ? itemRepository.findAllByOrderByIdAsc()
                : itemRepository.findByTypeOrderByIdAsc(type);
        Map<Long, Integer> reserved = reservationItemRepository.sumReservedQuantityByItem(
                date, items.stream().map(Item::getId).toList());
        return items.stream()
                .map(item -> ItemAvailabilityResponse.of(item, date, reserved.getOrDefault(item.getId(), 0)))
                .toList();
    }

    private Item getItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
    }
}
