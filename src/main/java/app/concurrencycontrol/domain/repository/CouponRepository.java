package app.concurrencycontrol.domain.repository;

import app.concurrencycontrol.domain.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Coupon c where c.id = :id")
    Optional<Coupon> findByIdWithPessimisticLock(@Param("id") Long id);
}

/* Pessimistic: LockModeType
 *
 * PESSIMISTIC_READ: dirty read가 발생하지 않을 때마다 공유 락을 획득하여 데이터가 수정/삭제 됨을 방지
 * PESSIMISTIC_WRITE: 배타적 락을 획득하여 다른 Transactional에서 조회/수정/삭제하는 것을 방지
 * PESSIMISTIC_FORCE_INCREMENT: PESSIMISTIC_WRITE와 비슷하지만 @Version 어노테이션이 있는 Entity와 협력하기 위해 도입, Lock을 획득하면 Version 업데이트
 * */
