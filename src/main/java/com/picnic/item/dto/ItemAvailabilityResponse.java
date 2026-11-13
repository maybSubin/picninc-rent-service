package com.picnic.item.dto;

import com.picnic.item.domain.Item;
import com.picnic.item.domain.ItemType;

import java.time.LocalDate;

public record ItemAvailabilityResponse(
        Long itemId,
        String name,
        ItemType type,
        int price,
        LocalDate date,
        int stock,
        int reserved,
        int available
) {
    public static ItemAvailabilityResponse of(Item item, LocalDate date, int reserved) {
        return new ItemAvailabilityResponse(item.getId(), item.getName(), item.getType(), item.getPrice(),
                date, item.getStock(), reserved, Math.max(item.getStock() - reserved, 0));
    }
}
