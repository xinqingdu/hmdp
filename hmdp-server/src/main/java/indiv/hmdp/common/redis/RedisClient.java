package indiv.hmdp.common.redis;

import java.time.Duration;

public interface RedisClient<K, V> {

    void set(K k, V v, Duration ofMinutes);

    V get(K k);
}
