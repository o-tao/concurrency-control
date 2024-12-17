package app.concurrencycontrol.service.redisLock;

import app.concurrencycontrol.domain.repository.RedisLockRepository;
import app.concurrencycontrol.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponLettuceService {

    private final RedisLockRepository redisLockRepository;
    private final CouponService couponService;

    public void decreaseWithLettuceLock(Long id) {
        while (!redisLockRepository.lock(id)) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            couponService.decrease(id);
        } finally {
            redisLockRepository.unlock(id);
        }
    }
}

/* Redis LettuceLock (SETNX 방식)
 *
 * Lettuce는 Netty기반의 Redis Client이며, 요청을 논블로킹으로 처리하여 높은 성능을 가진다.
 * lock: Redis의 setIfAbsent(SETNX)를 사용하여 락을 획득, lock의 만료 시간을 설정(timeout)하여 유실된 락을 방지
 * unlock: Redis의 delete 명령어로 락을 해제
 * - 락을 획득한 클라이언트만 해제할 수 있도록 락 키의 소유 여부를 확인해야함 (소유 확인 없이 진행 시 다른 클라이언트의 락을 해제할 가능성 존재)
 *
 * 장점
 * Lettuce는 Redis 의존성을 추가하는 경우 기본 Redis Client로 제공되어, 별도의 설정 없이간단히 구현 가능
 * 락에 만료 시간을 설정해 스레드가 락을 획득한 후 예기치 못한 종료가 발생하더라도 락이 영구적으로 남아있지 않도록 방지
 *
 * 단점
 * 스핀락으로 방식으로 Redis에 반복적으로 접근하여 성능저하와 네트워크 부하를 초래할 수 있다.
 *
 * 개선 방안
 * 스핀락 성능 저하를 줄이기 위해 Thread.sleep이나 Backoff를 사용하여 Redis 접근 빈도 수 줄이기 (하지만 스핀락의 성능 문제를 완전히 해결하지는 못함)
 * */
