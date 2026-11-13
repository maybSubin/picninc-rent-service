package com.picnic.item.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ItemType type;

    /** 하루 대여 가격 */
    @Column(nullable = false)
    private int price;

    /** 보유 수량 (하루 기준 최대 대여 가능 수량) */
    @Column(nullable = false)
    private int stock;

    @Column(length = 255)
    private String description;

    @Builder
    private Item(String name, ItemType type, int price, int stock, String description) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.description = description;
    }

    public boolean isBasic() {
        return type == ItemType.BASIC;
    }
}
