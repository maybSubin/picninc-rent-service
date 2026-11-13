package com.picnic.item.dto;

import com.picnic.item.domain.Item;
import com.picnic.item.domain.ItemType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ItemCreateRequest(
        @NotBlank(message = "용품 이름은 필수입니다.") @Size(max = 50) String name,
        @NotNull(message = "용품 종류(BASIC/OPTION)는 필수입니다.") ItemType type,
        @NotNull(message = "가격은 필수입니다.") @Min(value = 0, message = "가격은 0 이상이어야 합니다.") Integer price,
        @NotNull(message = "보유 수량은 필수입니다.") @Min(value = 1, message = "보유 수량은 1 이상이어야 합니다.") Integer stock,
        @Size(max = 255) String description
) {
    public Item toEntity() {
        return Item.builder()
                .name(name)
                .type(type)
                .price(price)
                .stock(stock)
                .description(description)
                .build();
    }
}
