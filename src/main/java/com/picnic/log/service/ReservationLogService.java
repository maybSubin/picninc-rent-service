package com.picnic.log.service;

import com.picnic.log.domain.ReservationLog;
import com.picnic.log.repository.ReservationLogRepository;
import com.picnic.reservation.event.ReservationActivityEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationLogService {

    private final ReservationLogRepository reservationLogRepository;

    /**
     * MySQL 트랜잭션이 성공적으로 커밋된 뒤에만 로그를 저장한다.
     * (실패·롤백된 작업은 로그가 남지 않음, 로그 저장 실패가 예약 처리에 영향을 주지 않음)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void record(ReservationActivityEvent event) {
        try {
            reservationLogRepository.save(ReservationLog.from(event));
        } catch (Exception e) {
            log.error("예약 로그 저장 실패 - reservationId={}, action={}", event.reservationId(), event.action(), e);
        }
    }

    public List<ReservationLog> findByReservationId(Long reservationId) {
        return reservationLogRepository.findByReservationIdOrderByCreatedAtAsc(reservationId);
    }
}
