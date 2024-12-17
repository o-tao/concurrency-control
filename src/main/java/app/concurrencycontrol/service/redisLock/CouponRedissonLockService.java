package app.concurrencycontrol.service.redisLock;

import app.concurrencycontrol.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponRedissonLockService {

    private final RedissonClient redissonClient;
    private final CouponService couponService;

    public void decreaseWithRedissonLock(Long couponId) {
        RLock lock = redissonClient.getLock(couponId.toString());

        try {
            boolean acquireLock = lock.tryLock(10, 1, TimeUnit.SECONDS);
            if (!acquireLock) {
                throw new InterruptedException("Lock 획득에 실패했습니다.");
            }
            couponService.decrease(couponId);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }
}

/* Redisson RLock 방식 (분산락)
 *
 * 분산락: RLock 객체를 사용하여 여러 서버나 인스턴스가 동일한 락을 공유하고, 데이터 일관성을 유지
 * Redisson은 스핀락을 사용하지 않고 RLock을 사용하여 락을 구현하며, 기본적으로 SETNX 명령어와 PX 옵션을 사용하여 락을 획득하고 해제한다.
 * 이를 통해 락 획득 실패 시 데드락에 빠지는 위험을 방지
 * - SETNX: (Set if Not eXists) 락이 이미 존재하지 않는 경우에만 락을 획득하도록 함
 * - PX: 밀리세컨트 단위의 시간 제한을 설정하여 락이 지정된 시간 동안 유지, 이를 통해 데드락에 빠지는 위험을 방지
 *
 * RLock은 여러 Redis 인스턴스가 동일한 락을 공유하고, 분산 환경에서 락을 통합 관리, 이는 서버에서 동시에 같은 자원을 액세스할 때 충돌을 피하는데 유리
 * tryLock을 사용하여 락을 획득하려 시도하고, 지정된 시간(10초) 동안 실패하면 예외 발생, 이를 통해 락 획득을 시도하는 클라이언트가 없는 경우 예외가 발생하여 반복 시도를 제한
 * unlock을 사용하여 락을 획득한 후 작업이 완료되면, 락을 해제, 이는 다른 클라이언트가 락을 획득할 수 있도록 하는 과정
 * */
