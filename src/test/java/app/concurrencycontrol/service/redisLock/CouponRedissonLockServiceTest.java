package app.concurrencycontrol.service.redisLock;

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
class CouponRedissonLockServiceTest {

    @Autowired
    private CouponRedissonLockService couponRedissonLockService;
    @Autowired
    private CouponRepository couponRepository;

    @Test
    @DisplayName("Redisson, RLock을 사용해 쿠폰 감소 작업이 동시성 문제 없이 완료된다.")
    public void decreaseWithRedissonLockTest() throws InterruptedException {
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
                    couponRedissonLockService.decreaseWithRedissonLock(coupon.getId());
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
