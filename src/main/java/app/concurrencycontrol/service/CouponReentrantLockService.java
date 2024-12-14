package app.concurrencycontrol.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class CouponReentrantLockService {

    private final CouponService couponService;
    private final ReentrantLock reentrantLock = new ReentrantLock();
    private final Condition condition = reentrantLock.newCondition();

    public void decreaseWithReentrantLock(Long couponId) {
        reentrantLock.lock();
        try {
            couponService.decrease(couponId);
        } finally {
            reentrantLock.unlock();
        }
    }

    public boolean tryDecreaseWithReentrantLock(Long couponId) {
        try {
            if (reentrantLock.tryLock(1, TimeUnit.SECONDS)) {
                try {
                    couponService.decrease(couponId);
                    return true;
                } finally {
                    reentrantLock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    public void awaitConditionToDecrease(Long couponId) {
        reentrantLock.lock();
        try {
            while (!tryDecreaseWithReentrantLock(couponId)) {
                condition.await();
            }
            condition.signalAll();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            reentrantLock.unlock();
        }
    }
}

/* Java에서 제공하는 ReentrantLock를 이용한 Lock
 *
 * 주요 특징
 * - java.util.concurrent.locks 패키지에서 제공하는 Lock, synchronized 보다 세밀하게 락을 제어
 * - 잠금 획득 시도를 위한 시간 제한 설정 가능, Condition을 적용하여 대기중인 스레드를 선별적으로 깨울 수 있음
 * - Lock 획득 시 main memory에서 최신 데이터를 읽고 업데이트 시 반영, Lock 방출 시 변경 사항을 main memory에 반영
 * - synchronized와 마찬가지로 선언적 Transactional이 제한적
 * - 동시성 제어 및 병렬 처리 최적화가 가능, 유연한 동기화 처리가 가능
 *
 * decreaseWithReentrantLock: lock, unlock을 사용한 기본 처리방식을 적용한 메서드
 * tryDecreaseWithReentrantLock: 타임아웃을 적용한 잠금 획득 시도를 적용한 메서드
 * awaitConditionToDecrease: Condition을 사용하여 특정 조건이 만족될 때까지 대기하고, signal을 통해 스레드를 깨우는 것을 적용한 메서드
 *
 * decreaseWithReentrantLock, tryDecreaseWithReentrantLock 메서드는 스핀락을 사용하여 락을 획득
 * 이 과정에서 여러 스레드가 동시에 락을 시도할 수 있어, 데드락 상태에 빠질 위험이 존재
 *
 * awaitConditionToDecrease 메서드는 lockpooling을 적용하여 tryDecreaseWithReentrantLock 메서드를 사용하여 쿠폰 감소 작업을
 * 시도한 후 실패하면, await을 호출하여 스레드를 대기시킴. 이후 signalAll을 사용하여 다른 스레드가 대기에서 해제될 수 있도록 함
 * 이를 통해 스핀락에 의한 데드락 상태에 빠지는 가능성을 최소화시킬 수 있음
 * 즉, Lock을 계속해서 시도하지 않고 대기 상태에 있도록 하여 데드락을 방지
 * */
