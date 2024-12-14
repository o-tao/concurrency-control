package app.concurrencycontrol.service;

import app.concurrencycontrol.domain.Coupon;
import app.concurrencycontrol.domain.repository.CouponRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class CouponReentrantLockServiceTest {

    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private CouponReentrantLockService couponReentrantLockService;

    @Test
    @DisplayName("쿠폰 수량이 300개일 때, ReentrantLock을 사용해 쿠폰 감소 작업이 동시성 문제 없이 완료된다.")
    public void decreaseWithReentrantLockTest() throws InterruptedException {
        // given
        Coupon coupon = new Coupon("COUPON_001", 300L);
        couponRepository.save(coupon);

        int threadCount = 300;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    couponReentrantLockService.decreaseWithReentrantLock(coupon.getId());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        Coupon persistedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        log.info("잔여 쿠폰 수량 {}", persistedCoupon.getQuantity());
        assertThat(persistedCoupon.getQuantity()).isZero();
    }

    @Test
    @DisplayName("tryLock을 사용해 쿠폰 감소 작업이 동시성 문제 없이 완료된다.")
    public void tryDecreaseWithReentrantLockTest() throws InterruptedException {
        // given
        Coupon coupon = new Coupon("COUPON_002", 300L);
        couponRepository.save(coupon);

        int threadCount = 300;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    boolean result = couponReentrantLockService.tryDecreaseWithReentrantLock(coupon.getId());
                    assertThat(result).isTrue();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        Coupon persistedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        log.info("잔여 쿠폰 수량 {}", persistedCoupon.getQuantity());
        assertThat(persistedCoupon.getQuantity()).isZero();
    }

    @Test
    @DisplayName("waitCondition을 사용해 쿠폰 감소 작업이 동시성 문제 없이 완료된다.")
    public void awaitConditionToDecreaseTest() throws InterruptedException {
        // given
        Coupon coupon = new Coupon("COUPON_003", 300L);
        couponRepository.save(coupon);

        int threadCount = 300;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    couponReentrantLockService.awaitConditionToDecrease(coupon.getId());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        Coupon persistedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        log.info("잔여 쿠폰 수량 {}", persistedCoupon.getQuantity());
        assertThat(persistedCoupon.getQuantity()).isZero();
    }
}
