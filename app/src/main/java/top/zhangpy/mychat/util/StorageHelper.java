package top.zhangpy.mychat.util;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Objects;

public class StorageHelper {
    private static final String BASE_DIRECTORY = "MyChat_files";

    // 获取基础目录
    private static File getBaseDirectory(Context context) {
//        return new File(context.getFilesDir(), BASE_DIRECTORY);
        return new File(context.getExternalFilesDir(null), BASE_DIRECTORY);
    }

    // 获取用户头像目录
    public static File getUserAvatarDirectory(Context context, String userId) {
        File userDir = new File(getBaseDirectory(context), "user/" + userId);
        if (!userDir.exists()) {
            userDir.mkdirs();
        }
        return userDir;
    }

    // 获取群组头像目录
    public static File getGroupAvatarDirectory(Context context, String groupId) {
        File groupDir = new File(getBaseDirectory(context), "group/" + groupId);
        if (!groupDir.exists()) {
            groupDir.mkdirs();
        }
        return groupDir;
    }

    // 获取聊天文件目录
    public static File getChatFileDirectory(Context context, String receiverId, String senderId) {
        File chatDir = new File(getBaseDirectory(context), "chat/" + receiverId + "/" + senderId);
        if (!chatDir.exists()) {
            chatDir.mkdirs();
        }
        return chatDir;
    }

    public static File getChatGroupFileDirectory(Context context, String receiverId, String groupId) {
        File chatDir = new File(getBaseDirectory(context), "chat_group/" + receiverId + "/" + groupId);
        if (!chatDir.exists()) {
            chatDir.mkdirs();
        }
        return chatDir;
    }

    /**
     * 保存文件
     * @param context 上下文
     * @param directoryType 目录类型
     * @param id1 用户ID或群组ID
     * @param id2 聊天对象ID
     * @param fileName 文件名
     * @param inputStream 文件输入流
     * @return 文件路径
     */
    public static String saveFile(Context context, String directoryType, String id1, String id2, String fileName, InputStream inputStream) {
        File targetDir;

        // 根据类型获取目录
        switch (directoryType) {
            case "user":
                targetDir = getUserAvatarDirectory(context, id1);
                break;
            case "group":
                targetDir = getGroupAvatarDirectory(context, id1);
                break;
            case "chat":
                if (id2 == null) {
                    throw new IllegalArgumentException("Sender ID is required for chat files.");
                }
                targetDir = getChatFileDirectory(context, id1, id2);
                break;
            case "chat_group":
                if (id2 == null) {
                    throw new IllegalArgumentException("Sender ID is required for chat files.");
                }
                targetDir = getChatGroupFileDirectory(context, id1, id2);
                break;
            default:
                throw new IllegalArgumentException("Invalid directory type: " + directoryType);
        }

        // 创建目标文件
        File targetFile = new File(targetDir, fileName);
//        String targetPath = targetFile.getAbsolutePath();
        if (!Objects.requireNonNull(targetFile.getParentFile()).exists()) {
            targetFile.getParentFile().mkdirs();
        }
        int i = 1;
        while (targetFile.exists()) {
            String [] split = fileName.split("\\.");
            fileName = split[0] + "(" + i +")." + split[1];
            targetFile = new File(targetDir, fileName);
//            targetPath = targetFile.getAbsolutePath();
            i++;
        }
        try {
            // 写入文件内容
            FileOutputStream fos = new FileOutputStream(targetFile);
            byte[] buffer = new byte[8192]; // 使用缓冲区读取数据
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.close();
            inputStream.close();
        } catch (Exception e) {
            Log.e("StorageHelper", "Failed to save file", e);
            return null; // 保存失败返回 null
        }

        // 返回文件的绝对路径
        return targetFile.getAbsolutePath();
    }



    public static InputStream base64ToInputStream(String base64) {
        byte[] bytes = Base64.getDecoder().decode(base64);
        return new ByteArrayInputStream(bytes);
    }

    public static String inputStreamToBase64(String avatarPath) {
        return Base64.getEncoder().encodeToString(avatarPath.getBytes());
    }

    public static void copyFile(File cachedAvatar, File newAvatar) {
        try (InputStream in = new FileInputStream(cachedAvatar);
             OutputStream out = new FileOutputStream(newAvatar)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            // 删除原文件
            if (!cachedAvatar.delete()) {
                Log.e("StorageHelper", "Failed to delete cachedAvatar");
            }
        } catch (IOException e) {
            Log.e("StorageHelper", "File copy failed", e);
        }
    }
}
