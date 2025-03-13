package top.zhangpy.mychat.util.download;

import android.os.Handler;
import android.os.Looper;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

// 进度监测装饰流
public class ProgressInputStream extends FilterInputStream {
    private final ProgressListener listener;
    private final long totalBytes;
    private long downloadedBytes;

    public ProgressInputStream(InputStream in,
                               long totalBytes,
                               ProgressListener listener) {
        super(in);
        this.totalBytes = totalBytes;
        this.listener = listener;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytesRead = super.read(b, off, len);
        if (bytesRead != -1) {
            downloadedBytes += bytesRead;
            // 通过主线程回调
            new Handler(Looper.getMainLooper()).post(() -> {
                listener.onProgress(downloadedBytes, totalBytes);
            });
        }
        return bytesRead;
    }
}
