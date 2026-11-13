package com.picnic.reservation.controller;

import com.picnic.log.domain.ReservationLog;
import com.picnic.log.service.ReservationLogService;
import com.picnic.reservation.dto.ReservationCreateRequest;
import com.picnic.reservation.dto.ReservationResponse;
import com.picnic.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationLogService reservationLogService;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationCreateRequest request) {
        ReservationResponse response = reservationService.create(request);
        return ResponseEntity.created(URI.create("/api/reservations/" + response.id())).body(response);
    }

    @GetMapping("/{reservationId}")
    public ReservationResponse findById(@PathVariable Long reservationId) {
        return reservationService.findById(reservationId);
    }

    @PatchMapping("/{reservationId}/cancel")
    public ReservationResponse cancel(@PathVariable Long reservationId) {
        return reservationService.cancel(reservationId);
    }

    @PatchMapping("/{reservationId}/rent")
    public ReservationResponse rent(@PathVariable Long reservationId) {
        return reservationService.rent(reservationId);
    }

    @PatchMapping("/{reservationId}/return")
    public ReservationResponse returnItems(@PathVariable Long reservationId) {
        return reservationService.returnItems(reservationId);
    }

    /** MongoDB에 저장된 예약별 활동 로그 조회 */
    @GetMapping("/{reservationId}/logs")
    public List<ReservationLog> findLogs(@PathVariable Long reservationId) {
        reservationService.findById(reservationId); // 존재하지 않는 예약이면 404
        return reservationLogService.findByReservationId(reservationId);
    }
}
