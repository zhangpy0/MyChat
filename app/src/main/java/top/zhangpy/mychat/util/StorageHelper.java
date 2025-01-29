package top.zhangpy.mychat.util;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        File targetDir = switch (directoryType) {
            case "user" -> getUserAvatarDirectory(context, id1);
            case "group" -> getGroupAvatarDirectory(context, id1);
            case "chat" -> {
                if (id2 == null) {
                    throw new IllegalArgumentException("Sender ID is required for chat files.");
                }
                yield getChatFileDirectory(context, id1, id2);
            }
            case "chat_group" -> {
                if (id2 == null) {
                    throw new IllegalArgumentException("Sender ID is required for chat files.");
                }
                yield getChatGroupFileDirectory(context, id1, id2);
            }
            default ->
                    throw new IllegalArgumentException("Invalid directory type: " + directoryType);
        };

        // 根据类型获取目录

        // 创建目标文件
        File targetFile = new File(targetDir, fileName);
        String targetPath = targetFile.getAbsolutePath();

        // 确保目标文件的父目录存在，不存在则创建
        if (!Objects.requireNonNull(targetFile.getParentFile()).exists()) {
            targetFile.getParentFile().mkdirs();
        }

        while (targetFile.exists()) {
            // 检查是否已有类似 "(1)" 的数字后缀
            int dotIndex = fileName.lastIndexOf(".");
            String baseName;
            String extension = "";

            if (dotIndex != -1) {
                baseName = fileName.substring(0, dotIndex);
                extension = fileName.substring(dotIndex);
            } else {
                baseName = fileName;
            }

            // 使用正则匹配 "(数字)" 后缀
            String pattern = "^(.*)\\((\\d+)\\)$";
            Pattern regex = java.util.regex.Pattern.compile(pattern);
            Matcher matcher = regex.matcher(baseName);

            int number = 1;
            if (matcher.matches()) {
                // 如果已包含数字后缀，提取基础名和数字
                baseName = matcher.group(1).trim();
                number = Integer.parseInt(matcher.group(2)) + 1;
            }

            // 添加或更新数字后缀
            fileName = baseName + "(" + number + ")" + extension;
            targetFile = new File(targetDir, fileName);
            targetPath = targetFile.getAbsolutePath();
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
            Logger.e("StorageHelper", "Failed to save file", e);
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
        File file = new File(avatarPath);
        if (!file.exists()) {
            return "";
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            fis.read(bytes);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            Logger.e("StorageHelper", "Failed to read file", e);
            return "";
        }
    }

    public static void copyFile(File cachedAvatar, File newAvatar) {

        try (InputStream in = new FileInputStream(cachedAvatar);
             FileOutputStream out = new FileOutputStream(newAvatar)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            Logger.e("StorageHelper", "File copy failed", e);
        }
    }

    // 将 content:// URI 转换为真实路径
    public static String getRealPathFromURI(Context context, String uriString) {
        Uri uri = Uri.parse(uriString);
        String realPath = null;

        // 处理不同类型的 URI
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是 DocumentProvider 类型的 URI
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] split = docId.split(":");
                    String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return context.getExternalFilesDir(null) + "/" + split[1];
                    }
                    // 处理其他类型的存储（如 SD 卡）
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    String id = DocumentsContract.getDocumentId(uri);
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            Long.parseLong(id)
                    );
                    return getDataColumn(context, contentUri, null, null);
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] split = docId.split(":");
                    String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    String selection = "_id=?";
                    String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            } else {
                // ContentProvider (非 DocumentProvider)
                return getDataColumn(context, uri, null, null);
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是文件路径
            return uri.getPath();
        } else {
            realPath = uriString;
        }
        return realPath;
    }

    // 检查 URI 是否是 ExternalStorageProvider
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    // 检查 URI 是否是 DownloadsProvider
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    // 检查 URI 是否是 MediaProvider
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    // 从 ContentProvider 查询路径
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static String formatFileSize(long sizeInBytes) {
        String formattedSize = "";
        if (sizeInBytes >= 1024 * 1024) {
            formattedSize = String.format("%.2f MB", sizeInBytes / (1024.0 * 1024.0));
        } else if (sizeInBytes >= 1024) {
            formattedSize = String.format("%.2f KB", sizeInBytes / 1024.0);
        } else {
            formattedSize = sizeInBytes + " B";
        }
        return formattedSize;
    }


}
