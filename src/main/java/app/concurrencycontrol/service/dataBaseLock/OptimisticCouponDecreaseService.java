package app.concurrencycontrol.service.dataBaseLock;

import app.concurrencycontrol.domain.OptimisticCoupon;
import app.concurrencycontrol.domain.repository.OptimisticCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OptimisticCouponDecreaseService {

    private final OptimisticCouponRepository optimisticCouponRepository;

    @Transactional
    public void decrease(Long couponId) {
        OptimisticCoupon optimisticCoupon = validateCoupon(couponId);
        optimisticCoupon.decrease();
    }

    private OptimisticCoupon validateCoupon(Long couponId) {
        return optimisticCouponRepository.findById(couponId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 쿠폰입니다.")
        );
    }
}
