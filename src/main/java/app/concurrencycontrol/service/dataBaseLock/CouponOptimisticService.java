package app.concurrencycontrol.service.dataBaseLock;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CouponOptimisticService {

    private final OptimisticCouponDecreaseService optimisticCouponDecreaseService;

    public void decreaseWithOptimisticLock(Long couponId) {
        for (int i = 0; i < 100; i++) {
            try {
                optimisticCouponDecreaseService.decrease(couponId);
                return;
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(10, 100));
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IllegalArgumentException("재시도 중 스레드가 중단되었습니다.");
                }
            }
        }
        throw new IllegalArgumentException("재시도 횟수를 초과했습니다.");
    }
}

/* OptimisticLock 낙관적 락
 *
 * DB 단에 실제 Lock을 설정하지 않고, Version을 관리하는 컬럼을 테이블에 추가해서 데이터 수정 시 마다 맞는 버전의 데이터를 수정하는지 판단하는 방식
 * DB에서 값을 읽고 update를 하려고 할 때, where 절에 바꾸려는 version 정보를 함께 보냄
 * 다른 스레드에서 값을 수정했다면, Version이 바뀌어있을 것이고, 그럼 update하려는 row를 찾지 못해 예외 발생
 *
 * 문제점
 * 충돌이 빈번한 경우 성능 저하: 낙관적 락은 데이터 충돌이 드물다는 가정하에 동작. 충돌이 빈번하게 발생하면 롤백retry횟수가 증가하여 성능이 크게 저하됨
 * 재시도 비용증가: 트랜잭션 실패 시 재시도를 구현해야 하며, 재시도를 위한 추가적인 로직 및 처리 비용이 발생, 재시도가 증가할수록 스레드 대기 시간이 길어지고, 전체 처리 시간이 증가될 수 있음
 * 데이터 변경이 빈번한 경우 부적합: 데이터가 자주 업데이트되는 시스템에서는 낙관적 락의 장점이 사라지고 충돌 가능성이 커짐
 * 트랜잭션 경계에 대한 관리 필요: 낙관적 락은 트랜잭션 경계 안에서 @Version 필드가 업데이트되므로, 잘못된 트랜잭션 경계 설정은 충돌 검출 실패나 데이터 무결성 문제가 발생할 수 있음
 * */
