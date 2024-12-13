package app.concurrencycontrol.service;

import app.concurrencycontrol.domain.Coupon;
import app.concurrencycontrol.domain.repository.CouponRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootTest
class CouponSynchronizedServiceTest {

    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private CouponSynchronizedService couponSynchronizedService;

    @Test
    @DisplayName("쿠폰 수량이 300개일 때, @Transactional 없이 synchronized로 처리하면 수량이 0이 된다.")
    public void decreaseCouponWithoutTransactionalTest() throws InterruptedException {
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
                    couponSynchronizedService.decreaseCouponWithoutTransactional(coupon.getId());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        Coupon persistedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        log.info("잔여 쿠폰 수량 {}", persistedCoupon.getQuantity());
        Assertions.assertThat(persistedCoupon.getQuantity()).isZero();
    }

    @Test
    @DisplayName("쿠폰 수량이 300개일 때, 외부 메서드를 synchronized로 호출하여 처리하면 수량이 0이 된다.")
    public void decreaseCouponWithExternalCallTest() throws InterruptedException {
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
                    couponSynchronizedService.decreaseCouponWithExternalCall(coupon.getId());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        Coupon persistedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        log.info("잔여 쿠폰 수량 {}", persistedCoupon.getQuantity());
        Assertions.assertThat(persistedCoupon.getQuantity()).isZero();
    }

}
