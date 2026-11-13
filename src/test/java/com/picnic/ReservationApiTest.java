package com.picnic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picnic.item.domain.Item;
import com.picnic.item.domain.ItemType;
import com.picnic.item.repository.ItemRepository;
import com.picnic.log.domain.ReservationLog;
import com.picnic.log.repository.ReservationLogRepository;
import com.picnic.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReservationApiTest {

    static final String TODAY = "2026-05-02";
    static final String TOMORROW = "2026-05-03";

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-05-02T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired ItemRepository itemRepository;
    @Autowired ReservationRepository reservationRepository;
    @MockBean ReservationLogRepository reservationLogRepository;

    Long tent;
    Long mat;
    Long chair;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        itemRepository.deleteAll();
        tent = save("감성 텐트", ItemType.BASIC, 30000, 1);
        mat = save("피크닉 돗자리", ItemType.BASIC, 10000, 5);
        chair = save("캠핑 체어", ItemType.OPTION, 4000, 3);
        reset(reservationLogRepository);
    }

    @Test
    void 용품_등록_목록_상세() throws Exception {
        String body = om.writeValueAsString(Map.of("name", "담요", "type", "OPTION", "price", 3000, "stock", 10));
        JsonNode created = json(mvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()));

        mvc.perform(get("/api/items/" + created.get("id").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("담요"));
        mvc.perform(get("/api/items").param("type", "BASIC"))
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/items/9999")).andExpect(status().isNotFound());
        mvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_생성시_가격을_계산하고_로그를_남긴다() throws Exception {
        reserve(TOMORROW, List.of(item(mat, 1), item(chair, 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RESERVED"))
                .andExpect(jsonPath("$.totalPrice").value(10000 + 4000 * 2))
                .andExpect(jsonPath("$.items.length()").value(2));

        verify(reservationLogRepository, times(1)).save(any(ReservationLog.class));
    }

    @Test
    void 기본용품은_정확히_한종류_1개여야_한다() throws Exception {
        reserve(TOMORROW, List.of(item(chair, 1)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("BASIC_ITEM_REQUIRED"));
        reserve(TOMORROW, List.of(item(tent, 1), item(mat, 1)))
                .andExpect(jsonPath("$.code").value("BASIC_ITEM_REQUIRED"));
        reserve(TOMORROW, List.of(item(mat, 2)))
                .andExpect(jsonPath("$.code").value("BASIC_ITEM_REQUIRED"));
        reserve(TOMORROW, List.of(item(mat, 1), item(mat, 1)))
                .andExpect(jsonPath("$.code").value("DUPLICATED_ITEM"));
        reserve("2026-05-01", List.of(item(mat, 1)))
                .andExpect(jsonPath("$.code").value("PAST_USE_DATE"));

        verify(reservationLogRepository, never()).save(any());
    }

    @Test
    void 날짜별로_재고를_확인한다() throws Exception {
        reserve(TOMORROW, List.of(item(tent, 1), item(chair, 3))).andExpect(status().isCreated());

        // 같은 날짜: 텐트 재고(1개) 소진
        reserve(TOMORROW, List.of(item(tent, 1)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("OUT_OF_STOCK"));
        // 다른 날짜는 예약 가능
        reserve(TODAY, List.of(item(tent, 1), item(chair, 3))).andExpect(status().isCreated());

        mvc.perform(get("/api/items/availability").param("date", TOMORROW))
                .andExpect(jsonPath("$[0].reserved").value(1))
                .andExpect(jsonPath("$[0].available").value(0))
                .andExpect(jsonPath("$[2].available").value(0));
    }

    @Test
    void 취소하면_재고가_다시_열린다() throws Exception {
        long id = json(reserve(TOMORROW, List.of(item(tent, 1)))).get("id").asLong();

        mvc.perform(patch("/api/reservations/" + id + "/cancel"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELED"));
        mvc.perform(patch("/api/reservations/" + id + "/cancel"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("INVALID_STATUS"));

        reserve(TOMORROW, List.of(item(tent, 1))).andExpect(status().isCreated());
    }

    @Test
    void 대여와_반납은_허용된_상태에서만_가능하다() throws Exception {
        long future = json(reserve(TOMORROW, List.of(item(mat, 1)))).get("id").asLong();
        mvc.perform(patch("/api/reservations/" + future + "/rent"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("NOT_USE_DATE"));

        long id = json(reserve(TODAY, List.of(item(mat, 1)))).get("id").asLong();
        mvc.perform(patch("/api/reservations/" + id + "/return"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("INVALID_STATUS"));
        mvc.perform(patch("/api/reservations/" + id + "/rent"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RENTED"));
        mvc.perform(patch("/api/reservations/" + id + "/cancel"))
                .andExpect(status().isConflict());
        mvc.perform(patch("/api/reservations/" + id + "/return"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RETURNED"));

        mvc.perform(get("/api/reservations/" + id))
                .andExpect(jsonPath("$.rentedAt").isNotEmpty())
                .andExpect(jsonPath("$.returnedAt").isNotEmpty());
        mvc.perform(get("/api/reservations/9999")).andExpect(status().isNotFound());
        // 생성 2 + 대여 1 + 반납 1 = 성공한 작업만 4건
        verify(reservationLogRepository, times(4)).save(any(ReservationLog.class));
    }

    private Long save(String name, ItemType type, int price, int stock) {
        return itemRepository.save(Item.builder().name(name).type(type).price(price).stock(stock).build()).getId();
    }

    private Map<String, Object> item(Long id, int qty) {
        return Map.of("itemId", id, "quantity", qty);
    }

    private ResultActions reserve(String date, List<Map<String, Object>> items) throws Exception {
        String body = om.writeValueAsString(Map.of(
                "customerName", "김피크", "phone", "010-1234-5678", "useDate", date, "items", items));
        return mvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private JsonNode json(ResultActions actions) throws Exception {
        return om.readTree(actions.andReturn().getResponse().getContentAsString());
    }
}
