package top.zhangpy.mychat.util.download;

import java.io.File;

public interface ProgressListener {
    void onProgress(long downloadedBytes, long totalBytes);
    void onComplete(File file);
    void onError(Throwable throwable);
}
