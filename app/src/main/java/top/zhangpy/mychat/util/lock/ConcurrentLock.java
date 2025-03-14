package top.zhangpy.mychat.util.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConcurrentLock {
    /**
     * 指定作为锁key的参数索引位置
     * 支持多参数组合（如{0,1}）
     */
    int[] params() default {0};

    /**
     * 锁最大存活时间
     * 默认5分钟
     */
    long maxLockAliveTime() default 300_000;

    /**
     * 锁等待超时时间（毫秒）
     * 0表示不超时
     */
    long timeout() default 0;

    /**
     * 是否使用弱引用锁对象
     */
    boolean weakReference() default true;
}
