package app.concurrencycontrol.service.dataBaseLock;

import app.concurrencycontrol.domain.OptimisticCoupon;
import app.concurrencycontrol.domain.repository.OptimisticCouponRepository;
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
class CouponOptimisticServiceTest {

    @Autowired
    private CouponOptimisticService couponOptimisticService;
    @Autowired
    private OptimisticCouponRepository optimisticCouponRepository;

    @Test
    @DisplayName("Optimistic, 낙관적 잠금을 사용해 쿠폰 감소 작업이 동시성 문제 없이 완료된다.")
    public void decreaseWithOptimisticLockTest() throws InterruptedException {
        // given
        OptimisticCoupon optimisticCoupon = new OptimisticCoupon("COUPON_001", 300L);
        optimisticCouponRepository.save(optimisticCoupon);

        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    couponOptimisticService.decreaseWithOptimisticLock(optimisticCoupon.getId());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        OptimisticCoupon persistedCoupon = optimisticCouponRepository.findById(optimisticCoupon.getId()).orElseThrow();
        log.info("잔여 쿠폰 수량 {}", persistedCoupon.getQuantity());
        Assertions.assertThat(persistedCoupon.getQuantity()).isZero();
    }

}
