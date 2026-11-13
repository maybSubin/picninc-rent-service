package com.picnic.item.repository;

import com.picnic.item.domain.Item;
import com.picnic.item.domain.ItemType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOrderByIdAsc();

    List<Item> findByTypeOrderByIdAsc(ItemType type);

    /** 동시 예약 시 초과 예약을 막기 위해 용품 행에 쓰기 락을 건다. (id 순서로 잠가 데드락 방지) */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Item i where i.id in :ids order by i.id")
    List<Item> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);
}
