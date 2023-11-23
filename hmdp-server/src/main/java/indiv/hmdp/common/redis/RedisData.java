package indiv.hmdp.common.redis;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author
 * @date 2023/11/20 21 56
 * discription
 */
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
