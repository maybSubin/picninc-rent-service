package com.picnic.global.config;

import com.picnic.item.domain.Item;
import com.picnic.item.domain.ItemType;
import com.picnic.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 용품 테이블이 비어 있을 때 데모용 샘플 용품을 등록한다. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "picnic.sample-data.enabled", havingValue = "true", matchIfMissing = true)
public class SampleDataInitializer implements ApplicationRunner {

    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (itemRepository.count() > 0) {
            return;
        }
        List<Item> items = List.of(
                item("감성 텐트", ItemType.BASIC, 30000, 5, "2~3인용 원터치 감성 텐트"),
                item("피크닉 돗자리", ItemType.BASIC, 10000, 10, "방수 대형 피크닉 돗자리"),
                item("피크닉 테이블", ItemType.OPTION, 8000, 8, "접이식 우드 롤 테이블"),
                item("캠핑 랜턴", ItemType.OPTION, 5000, 10, "충전식 LED 캠핑 랜턴"),
                item("감성 무드등", ItemType.OPTION, 5000, 10, "따뜻한 색감의 무드 조명"),
                item("캠핑 체어", ItemType.OPTION, 4000, 20, "경량 접이식 캠핑 체어"),
                item("릴렉스 체어", ItemType.OPTION, 6000, 10, "등받이가 깊은 릴렉스 체어"),
                item("피크닉 웨건", ItemType.OPTION, 10000, 4, "짐 운반용 폴딩 웨건"),
                item("라탄 바구니", ItemType.OPTION, 3000, 10, "피크닉 감성 라탄 바구니"),
                item("체크 패턴 피크닉 매트", ItemType.OPTION, 4000, 10, "레이어드용 체크 패턴 매트"),
                item("와인잔 세트", ItemType.OPTION, 5000, 6, "2인용 아크릴 와인잔 세트"),
                item("블루투스 스피커", ItemType.OPTION, 7000, 5, "휴대용 블루투스 스피커"),
                item("담요", ItemType.OPTION, 3000, 15, "포근한 극세사 담요")
        );
        itemRepository.saveAll(items);
        log.info("샘플 용품 {}건 등록 완료", items.size());
    }

    private Item item(String name, ItemType type, int price, int stock, String description) {
        return Item.builder().name(name).type(type).price(price).stock(stock).description(description).build();
    }
}
