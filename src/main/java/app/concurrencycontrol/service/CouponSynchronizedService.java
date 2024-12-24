package app.concurrencycontrol.service;

import app.concurrencycontrol.domain.Coupon;
import app.concurrencycontrol.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponSynchronizedService {

    private final CouponRepository couponRepository;
    private final CouponService couponService;

    // 선언적 Transactional 제거
    public synchronized void decreaseWithoutTransactional(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));
        coupon.decrease();
        couponRepository.save(coupon);
    }

    // 외부 메서드 호출
    public synchronized void decreaseWithExternalCall(Long couponId) {
        couponService.decrease(couponId);
    }
}

/* Java에서 제공하는 synchronized를 이용한 Lock
 *
 * 해당 동기화는 특정 객체에 대한 Lock을 획득하고, 해당 Lock이 해제될 때까지 다른 스레드들이 Lock을 획득하려고 하는 것을 방지
 * 또한, main memory와 thread가 작업하는 local memory 사이 일관성도 보장
 * 이는 synchronized 블록에 진입 혹은 빠져나올 때, 모든 local cache(스레드가 보유한 변수 복사본)가 main memory와 동기화 되도록 하여,
 * Thread가 최신 데이터를 볼 수 있도록 하기 때문
 * */

/* 문제점
 *
 * 선언적 Transactional은 Spring의 AOP 기능을 활용, 실제 메서드를 사용하는 것이 아닌 Proxy mode로 동작
 * 선언적 Transactional을 메서드 단위에 붙일 경우, Advisor에서 메서드의 시그니처 정보(메서드 명, 파라미터 등)를 가지고 Transaction을 동작
 * Call → AOP Proxy → Transaction Manager → ••• → target Method
 * 이는 Advisor가 실제로 실제로 실행하는 메서드는 Proxy 객체인데, 여기에 synchronized 키워드가 없기 때문에 동시성 제어가 되지않음
 * */

/* 해결방안
 *
 * 1. 선언적 Transactional 제거
 * 이는 더이상 메서드 레벨에서 Proxy를 사용하지 않아 동시성 제어가 가능해짐 → 하지만 더이상 JPA의 영속화 기능을 사용할 수 없음
 *
 * 2. 외부 메서드 호출
 * 선언적 Transactional 제거할때보다 약 2배 빠른 성능 → 코드복잡도 증가
 * */

/* 주의사항
 *
 * synchronized 블록은 스레드 간의 직렬화된 접근을 보장하지만, 경쟁 상태가 발생할 때 성능 저하가 발생할 수 있음
 * 높은 트래픽을  처리하는 애플리케이션에서는 RaceCondition 문제가 발생하여 동시성 제어의 보장이 어려울 수 있음
 * 성능 저하와 트랜잭션 분리 부족 등의 문제를 해결하는 데 한계가 있음
 * */
