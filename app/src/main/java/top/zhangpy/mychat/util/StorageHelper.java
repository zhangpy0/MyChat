package top.zhangpy.mychat.util;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Locale;
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
        Logger.initialize(context);
        Logger.enableLogging(true);
        Logger.d("StorageHelper", "Processing URI: " + uri.toString());

        // 优化点：使用提前返回减少嵌套层级
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            Logger.d("StorageHelper", "Handling file URI: " + uri);
            return uri.getPath().replaceAll("%20", " ");
        }

        if (!"content".equalsIgnoreCase(uri.getScheme())) {
            Logger.w("StorageHelper", "Unsupported URI scheme: " + uri.getScheme());
            return uriString;
        }

        if (isLegacyMediaStoreUri(uri)) {
            Logger.d("StorageHelper", "Handling legacy MediaStore URI");
            return getDataColumn(context, uri, null, null);
        }

        // 仅处理content协议
        if (DocumentsContract.isDocumentUri(context, uri)) {
            Logger.d("StorageHelper", "Handling document URI: " + uri);
            return handleDocumentUri(context, uri);
        } else {
            // 优化点：提取独立方法处理常规内容URI
            Logger.d("StorageHelper", "Handling content URI: " + uri);
            return handleDocumentUri(context, uri);
        }
    }

    private static String handleDocumentUri(Context context, Uri uri) {
        try {
            if (isThirdPartyFileProvider(uri)) {
                Logger.d("StorageHelper", "Handling third-party file provider: " + uri);
                return handleThirdPartyFileProvider(context, uri);
            }

            if (isExternalStorageDocument(uri)) {
                Logger.d("StorageHelper", "Handling external storage document: " + uri);
                return handleExternalStorageDocument(context, uri);
            } else if (isDownloadsDocument(uri)) {
                Logger.d("StorageHelper", "Handling downloads document: " + uri);
                return handleDownloadsDocument(context, uri);
            } else if (isMediaDocument(uri)) {
                Logger.d("StorageHelper", "Handling media document: " + uri);
                return handleMediaDocument(context, uri);
            }

            Logger.d("StorageHelper", "Handling unknown: " + uri);
            return handleThirdPartyFileProvider(context, uri);
        } catch (Exception e) {
            Logger.e("StorageHelper", "Error handling document URI: " + e.getMessage());
        }
        return null;
    }

    private static boolean isLegacyMediaStoreUri(Uri uri) {
        String authority = uri.getAuthority();
        return "media".equals(authority)
                || "media.external".equals(authority)
                || "media.internal".equals(authority);
    }

    private static boolean isThirdPartyFileProvider(Uri uri) {
        String authority = uri.getAuthority();
        return authority != null && authority.contains(".fileprovider"); // 匹配常见 FileProvider 命名模式
    }

    private static String handleThirdPartyFileProvider(Context context, Uri uri) {
        // 通过流复制文件（兼容性最好）
        try {
            // 生成临时文件名
            String fileName = getFileNameFromUri(context, uri);
            Logger.d("StorageHelper", "Third-party file name: " + fileName);
            SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String userId = String.valueOf(prefs.getInt("user_id", -1));
            InputStream is = context.getContentResolver().openInputStream(uri);
            String filePath = saveFile(context, "chat", userId, userId, fileName, is);
            Logger.d("StorageHelper", "Saved third-party file: " + filePath);
            if (filePath != null) {
                return filePath;
            }
        } catch (Exception e) {
            Logger.e("StorageHelper", "Error copying third-party file: " + e.getMessage());
            return null;
        }
        return null;
    }

    private static String getFileNameFromUri(Context context, Uri uri) {
        String name = null;
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } catch (Exception e) {
            Logger.e("StorageHelper", "Error getting filename: " + e.getMessage());
        }
        return name != null ? name : "temp_" + System.currentTimeMillis();
    }

    private static String handleExternalStorageDocument(Context context, Uri uri) {
        String docId;
        try {
            docId = DocumentsContract.getDocumentId(uri);
        } catch (Exception e) {
            Logger.e("StorageHelper", "Can't get document ID: " + e.getMessage());
            return null;
        }

        String[] split = docId.split(":");
        if (split.length < 2) {
            Logger.w("StorageHelper", "Invalid document ID format: " + docId);
            return null;
        }

        String type = split[0];
        String path = split[1];

        // 优化点：扩展支持更多存储类型
        if ("primary".equalsIgnoreCase(type)) {
            return new File(context.getExternalFilesDir(null), path).getAbsolutePath();
        } else {
            // 处理SD卡等外部存储（需要根据具体设备实现）
            File[] externalDirs = context.getExternalFilesDirs(null);
            if (externalDirs.length > 1 && externalDirs[1] != null) {
                return new File(externalDirs[1], path).getAbsolutePath();
            }
        }
        Logger.w("StorageHelper", "Unhandled external storage type: " + type);
        return null;
    }

    private static String handleDownloadsDocument(Context context, Uri uri) {
        String id;
        try {
            id = DocumentsContract.getDocumentId(uri);
        } catch (Exception e) {
            Logger.e("StorageHelper", "Can't get download document ID: " + e.getMessage());
            return null;
        }

        // 优化点：增强raw类型处理
        if (id.startsWith("raw:")) {
            return id.replaceFirst("raw:", "");
        }

        try {
            Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    Long.parseLong(id)
            );
            return getDataColumn(context, contentUri, null, null);
        } catch (NumberFormatException e) {
            Logger.e("StorageHelper", "Invalid download document ID: " + id);
            return null;
        }
    }

    private static String handleMediaDocument(Context context, Uri uri) {
        String docId;
        try {
            docId = DocumentsContract.getDocumentId(uri);
        } catch (Exception e) {
            Logger.e("StorageHelper", "Can't get media document ID: " + e.getMessage());
            return null;
        }

        String[] split = docId.split(":");
        if (split.length < 2) {
            Logger.w("StorageHelper", "Invalid media document ID: " + docId);
            return null;
        }

        String type = split[0];
        String id = split[1];

        Uri contentUri = getMediaContentUri(type);
        if (contentUri == null) {
            Logger.w("StorageHelper", "Unknown media type: " + type);
            return null;
        }

        return getDataColumn(context, contentUri, "_id=?", new String[]{id});
    }

    private static Uri getMediaContentUri(String type) {
        switch (type.toLowerCase(Locale.US)) {
            case "image":
                return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            case "video":
                return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            case "audio":
                return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            default:
                return null;
        }
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
        final String column = MediaStore.Files.FileColumns.DATA; // 使用标准常量
        final String[] projection = {column};

        try (Cursor cursor = context.getContentResolver().query(
                uri, projection, selection, selectionArgs, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnIndex);
            }
        } catch (IllegalArgumentException e) {
            Logger.e("StorageHelper", "Column '_data' not found: " + e.getMessage());
        } catch (SecurityException e) {
            Logger.e("StorageHelper", "Permission denied: " + e.getMessage());
        } catch (Exception e) {
            Logger.e("StorageHelper", "Unexpected error: " + e.getMessage());
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
