package top.zhangpy.mychat.util.lock;

public class ConcurrentAccessException extends RuntimeException {
    public ConcurrentAccessException(String message) {
        super(message);
    }
}
