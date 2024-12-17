package app.concurrencycontrol.domain.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisLockRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public Boolean lock(Long lockKey) {
        return redisTemplate
                .opsForValue()
                .setIfAbsent(lockKey.toString(),
                        "lock",
                        300L,
                        TimeUnit.MILLISECONDS);
    }

    public Boolean unlock(Long lockKey) {
        return redisTemplate.delete(lockKey.toString());
    }
}
