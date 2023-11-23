package indiv.hmdp.common.redis;

import java.util.Queue;

public interface MessageQueue<T> {
    void offer(T t);

    void handle(T t);
}
