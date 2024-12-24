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
class CouponServiceTest {

    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private CouponService couponService;

    @Test
    @DisplayName("300개의 스레드에서 동시에 300번 쿠폰 수량 감소 요청 시 남은 쿠폰 수량은 0이다.")
    public void couponDecreaseTest() throws InterruptedException {
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
                    couponService.decrease(coupon.getId());
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
