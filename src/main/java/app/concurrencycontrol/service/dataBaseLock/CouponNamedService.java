package app.concurrencycontrol.service.dataBaseLock;

import app.concurrencycontrol.domain.repository.CouponRepository;
import app.concurrencycontrol.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponNamedService {

    private final CouponRepository couponRepository;
    private final CouponService couponService;

    public void decreaseWithNamedLock(Long couponId) {
        try {
            couponRepository.getLock(couponId.toString());
            couponService.decrease(couponId);
        } finally {
            couponRepository.releaseLock(couponId.toString());
        }
    }
}

/* NamedLock 네임드락
 *
 * 분산락을 구현하는 방법 중 하나. 분산락을 사용하면 여러 시스템 간에 데이터를 안전하게 공유하고 동시성 문제를 해결할 수 있다.
 * NamedLock은 특정 테이블이나 레코드가 아니라 별도의 락 공간에 락을 설정하여 특정 작업 간의 충돌을 방지할 수 있다.
 * -> 분산락을 구현할 수 있다.
 *
 * 그러나 트랜잭션 종료 시 Lock 해제, 세션 관리 등을 수동으로 처리해야하기 때문에 구현이 복잡할 수 있다.
 * 트랜잭션 종료 시 자동으로 해제되지 않기 때문에, 수동으로 락을 해제하는 작업이 필요
 * */
