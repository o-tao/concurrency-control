package app.concurrencycontrol.domain.repository;

import app.concurrencycontrol.domain.OptimisticCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptimisticCouponRepository extends JpaRepository<OptimisticCoupon, Long> {
}
