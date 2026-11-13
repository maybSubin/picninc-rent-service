package com.picnic.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 용품입니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."),
    BASIC_ITEM_REQUIRED(HttpStatus.BAD_REQUEST, "기본 용품(텐트 또는 돗자리)은 정확히 한 종류를 1개 선택해야 합니다."),
    DUPLICATED_ITEM(HttpStatus.BAD_REQUEST, "같은 용품이 중복으로 요청되었습니다."),
    PAST_USE_DATE(HttpStatus.BAD_REQUEST, "지난 날짜는 예약할 수 없습니다."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "선택한 날짜에 예약 가능한 수량이 부족합니다."),
    INVALID_STATUS(HttpStatus.CONFLICT, "현재 예약 상태에서는 처리할 수 없는 요청입니다."),
    NOT_USE_DATE(HttpStatus.CONFLICT, "대여는 이용 당일에만 가능합니다.");

    private final HttpStatus status;
    private final String message;
}
