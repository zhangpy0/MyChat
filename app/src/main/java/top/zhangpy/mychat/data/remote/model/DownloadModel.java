package top.zhangpy.mychat.data.remote.model;

import java.io.InputStream;

import lombok.Data;

@Data
public class DownloadModel {

    String fileName;
    InputStream inputStream;

    public DownloadModel(String fileName, InputStream inputStream) {
        this.fileName = fileName;
        this.inputStream = inputStream;
    }
}
