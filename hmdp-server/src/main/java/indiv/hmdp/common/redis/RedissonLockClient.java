package indiv.hmdp.common.redis;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

/**
 * @author
 * @date 2023/11/22 16 03
 * discription
 */
@Component
public class RedissonLockClient {
    public RedissonClient redissonClient;

    public RedissonLockClient() {
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379")
                .setPassword("123456");
        redissonClient = Redisson.create(config);
    }
    public RLock getLock(String lockKey){
        return redissonClient.getLock(lockKey);
    }
}
