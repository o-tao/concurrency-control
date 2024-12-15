package app.concurrencycontrol.service.dataBaseLock;

import app.concurrencycontrol.domain.Coupon;
import app.concurrencycontrol.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponPessimisticService {

    private final CouponRepository couponRepository;

    @Transactional
    public void decrease(Long couponId) {
        Coupon pessimisticCoupon = validateCoupon(couponId);
        pessimisticCoupon.decrease();
    }

    private Coupon validateCoupon(Long couponId) {
        return couponRepository.findByIdWithPessimisticLock(couponId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 쿠폰입니다.")
        );
    }
}

/* PessimisticLock 비관적락
 *
 * 비관적락은 동시성 문제가 발생하는 것에 대한 가정하에 자원에 대한 다른 접근을 차단하고 시작
 * 따라서 Transactional이 시작할 때 공유락 혹은 배타적락을 걸음
 * 공유락(SharedLock): Read Lock이라고도 부르며, 데이터를 읽을 때는 같은 공유락끼리 접근을 허용하지만 write 작업은 차단
 * 배타적락(Exclusive Lock): Write Lock이라고도 부르며, Transaction이 완료될 때까지 유지되면서 배타락이 끝나기 전까진 read/write 작업을 모두 차단
 *
 * 장점
 * Race Condition이 빈번하게 일어난다면 OptimisticLock 낙관적락보다 성능이 좋음
 * DB단의 Lock을 통해 동시성을 제어하기 때문에 확실한 데이터 정합성이 보장됨
 *
 * 단점
 * DB단의 Lock을 설정하기 때문에 한 트랜잭션 작업이 정상적으로 끝나지 않으면 다른 트랜잭션 작업들이 대기해야 하므로 성능이 감소할 수 있음
 *
 * 충돌이 많이 발생할 수 있는 환경이라면 낙관적락보다는 비관적락이 적합
 * */
