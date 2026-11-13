package com.picnic.item.dto;

import com.picnic.item.domain.Item;
import com.picnic.item.domain.ItemType;

public record ItemResponse(Long id, String name, ItemType type, int price, int stock, String description) {

    public static ItemResponse from(Item item) {
        return new ItemResponse(item.getId(), item.getName(), item.getType(),
                item.getPrice(), item.getStock(), item.getDescription());
    }
}
