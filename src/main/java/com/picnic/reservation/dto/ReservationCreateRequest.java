package com.picnic.reservation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record ReservationCreateRequest(
        @NotBlank(message = "예약자 이름은 필수입니다.") @Size(max = 30) String customerName,
        @NotBlank(message = "연락처는 필수입니다.")
        @Pattern(regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$", message = "연락처 형식이 올바르지 않습니다.") String phone,
        @NotNull(message = "이용 날짜는 필수입니다.") LocalDate useDate,
        @NotEmpty(message = "예약할 용품을 선택해주세요.") List<@Valid ItemQuantity> items
) {
    public record ItemQuantity(
            @NotNull(message = "용품 ID는 필수입니다.") Long itemId,
            @NotNull(message = "수량은 필수입니다.") @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
            @Max(value = 20, message = "수량은 20개 이하로 선택해주세요.") Integer quantity
    ) {
    }
}
