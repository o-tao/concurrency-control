package app.concurrencycontrol.domain.repository;

import app.concurrencycontrol.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
