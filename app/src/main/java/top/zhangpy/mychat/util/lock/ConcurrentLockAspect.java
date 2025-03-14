package top.zhangpy.mychat.util.lock;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentLockAspect implements InvocationHandler {

    private final Object target;
    // 锁对象池（线程安全）
    private static final ConcurrentHashMap<String, LockWrapper> lockPool = new ConcurrentHashMap<>();

    public ConcurrentLockAspect(Object target) {
        this.target = target;
    }

    private String buildLockKey(Method method, Object[] args, int[] paramIndexes) {
        StringBuilder keyBuilder = new StringBuilder(method.getName());
        for (int index : paramIndexes) {
            if (index >= 0 && index < args.length) {
                keyBuilder.append(":").append(args[index].hashCode());
            }
        }
        return keyBuilder.toString();
    }

    private boolean acquireLock(Lock lock, long timeout) throws InterruptedException {
        return timeout > 0 ?
                lock.tryLock(timeout, TimeUnit.MILLISECONDS) :
                lock.tryLock();
    }

    private void releaseLock(String key, LockWrapper lockWrapper, ConcurrentLock config) {
        try {
            lockWrapper.getLock().unlock();

            // 清理过期锁
            if (lockWrapper.shouldClean(config.maxLockAliveTime())) {
                lockPool.remove(key, lockWrapper);
            }
        } catch (IllegalMonitorStateException ex) {
            // 锁状态异常处理
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ConcurrentLock concurrentLock = method.getAnnotation(ConcurrentLock.class);
        if (concurrentLock == null) {
            return method.invoke(target, args);
        }
        String lockKey = buildLockKey(method, args, concurrentLock.params());
        LockWrapper lockWrapper = lockPool.compute(lockKey, (k, v) -> {
            if (v == null || (concurrentLock.weakReference() && v.getLock() == null)) {
                return new LockWrapper(concurrentLock.weakReference());
            }
            return v;
        });

        try {
            if (!acquireLock(lockWrapper.getLock(), concurrentLock.timeout())) {
                throw new ConcurrentAccessException("Acquire lock timeout for key: " + lockKey);
            }
            return method.invoke(target, args);
        } finally {
            releaseLock(lockKey, lockWrapper, concurrentLock);
        }
    }

    // 锁包装类（带时间戳）
    private static class LockWrapper {
        private final Lock lock = new ReentrantLock();
        private final long createTime = System.currentTimeMillis();
        private final WeakReference<Lock> weakLock;

        LockWrapper(boolean useWeakReference) {
            this.weakLock = useWeakReference ?
                    new WeakReference<>(lock) : null;
        }

        Lock getLock() {
            return weakLock != null ?
                    weakLock.get() : lock;
        }

        boolean shouldClean(long maxAliveTime) {
            return System.currentTimeMillis() - createTime > maxAliveTime;
        }
    }
}
