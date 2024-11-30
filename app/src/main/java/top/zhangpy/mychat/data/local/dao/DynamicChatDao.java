package top.zhangpy.mychat.data.local.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import top.zhangpy.mychat.data.local.entity.ChatMessage;

public class DynamicChatDao {
    private final SQLiteDatabase database;

    public DynamicChatDao(SQLiteDatabase database) {
        this.database = database;
    }

    // 插入消息
    public void insertMessage(String tableName, ChatMessage message) {
        ContentValues values = new ContentValues();
        values.put("message_id", message.getMessageId());
        values.put("sender_id", message.getSenderId());
        values.put("group_id", message.getGroupId());
        values.put("send_time", message.getSendTime().getTime());
        values.put("content", message.getContent());
        values.put("message_type", message.getMessageType());
        values.put("file_path", message.getFilePath());
        values.put("file_name", message.getFileName());
        values.put("file_id", message.getFileId());
        values.put("is_read", message.getIsRead() ? 1 : 0);
        values.put("is_download", message.getIsDownload() ? 1 : 0);
        database.insert(tableName, null, values);
    }

    // 升序
    // 查询消息
    public List<ChatMessage> getMessages(String tableName) {
        List<ChatMessage> messages = new ArrayList<>();
        Cursor cursor = database.query(tableName, null, null, null, null, null, "send_time ASC");
        int groupId = 0;
        int receiverId = 0;
        String receiverType;
        String[] tableNames = tableName.split("_");
        if (tableNames[0].equals("group")) {
            groupId = Integer.parseInt(tableNames[1]);
            receiverType = "group";
        } else {
            receiverId = Integer.parseInt(tableNames[2]);
            receiverType = "user";
        }

        if (cursor.moveToFirst()) {
            do {
                ChatMessage message = new ChatMessage();
                message.setReceiverType(receiverType);
                message.setReceiverId(receiverId);
                message.setMessageId(cursor.getInt(cursor.getColumnIndexOrThrow("message_id")));
                message.setSenderId(cursor.getInt(cursor.getColumnIndexOrThrow("sender_id")));
                message.setGroupId(cursor.getInt(cursor.getColumnIndexOrThrow("group_id")));
                message.setSendTime(new java.sql.Timestamp(cursor.getLong(cursor.getColumnIndexOrThrow("send_time"))));
                message.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
                message.setMessageType(cursor.getString(cursor.getColumnIndexOrThrow("message_type")));
                message.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow("file_path")));
                message.setFileName(cursor.getString(cursor.getColumnIndexOrThrow("file_name")));
                message.setFileId(cursor.getInt(cursor.getColumnIndexOrThrow("file_id")));
                message.setIsRead(cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) == 1);
                message.setIsDownload(cursor.getInt(cursor.getColumnIndexOrThrow("is_download")) == 1);
                messages.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return messages;
    }

    // 查询 message_id 最大值
    public int getMaxMessageId(String tableName) {
        Cursor cursor = database.rawQuery("SELECT MAX(message_id) FROM " + tableName, null);
        cursor.moveToFirst();
        int maxMessageId = cursor.getInt(0);
        cursor.close();
        return maxMessageId;
    }

    // 查询 以[message_id - 49, message_id] 之间的消息
    public List<ChatMessage> getMessages(String tableName, int messageId) {
        List<ChatMessage> messages = new ArrayList<>();
        Cursor cursor = database.query(tableName, null, "message_id <= ?", new String[]{String.valueOf(messageId)}, null, null, "send_time DESC", "50");

        int groupId = 0;
        int receiverId = 0;
        String receiverType;
        String[] tableNames = tableName.split("_");
        if (tableNames[0].equals("group")) {
            groupId = Integer.parseInt(tableNames[1]);
            receiverType = "group";
        } else {
            receiverId = Integer.parseInt(tableNames[2]);
            receiverType = "user";
        }

        if (cursor.moveToFirst()) {
            do {
                ChatMessage message = new ChatMessage();
                message.setReceiverType(receiverType);
                message.setReceiverId(receiverId);
                message.setMessageId(cursor.getInt(cursor.getColumnIndexOrThrow("message_id")));
                message.setSenderId(cursor.getInt(cursor.getColumnIndexOrThrow("sender_id")));
                message.setGroupId(cursor.getInt(cursor.getColumnIndexOrThrow("group_id")));
                message.setSendTime(new java.sql.Timestamp(cursor.getLong(cursor.getColumnIndexOrThrow("send_time"))));
                message.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
                message.setMessageType(cursor.getString(cursor.getColumnIndexOrThrow("message_type")));
                message.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow("file_path")));
                message.setFileName(cursor.getString(cursor.getColumnIndexOrThrow("file_name")));
                message.setFileId(cursor.getInt(cursor.getColumnIndexOrThrow("file_id")));
                message.setIsRead(cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) == 1);
                message.setIsDownload(cursor.getInt(cursor.getColumnIndexOrThrow("is_download")) == 1);
                messages.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return messages;
    }

    // 删除单条消息
    public void deleteMessage(String tableName, int messageId) {
        database.delete(tableName, "message_id = ?", new String[]{String.valueOf(messageId)});
    }

    // 更新消息 isRead 和 isDownload 字段
    public void updateMessage(String tableName, ChatMessage message) {
        ContentValues values = new ContentValues();
        values.put("is_read", message.getIsRead() ? 1 : 0);
        values.put("is_download", message.getIsDownload() ? 1 : 0);
        values.put("file_path", message.getFilePath());
        database.update(tableName, values, "message_id = ?", new String[]{String.valueOf(message.getMessageId())});
    }

    public ChatMessage getLastMessage(String tableName) {
        Cursor cursor = database.query(tableName, null, null, null, null, null, "send_time DESC", "1");
        ChatMessage message = new ChatMessage();
        int groupId = 0;
        int receiverId = 0;
        String receiverType;
        String[] tableNames = tableName.split("_");
        if (tableNames[0].equals("group")) {
            groupId = Integer.parseInt(tableNames[1]);
            receiverType = "group";
        } else {
            receiverId = Integer.parseInt(tableNames[2]);
            receiverType = "user";
        }

        if (cursor.moveToFirst()) {
            message.setReceiverType(receiverType);
            message.setReceiverId(receiverId);
            message.setMessageId(cursor.getInt(cursor.getColumnIndexOrThrow("message_id")));
            message.setSenderId(cursor.getInt(cursor.getColumnIndexOrThrow("sender_id")));
            message.setGroupId(cursor.getInt(cursor.getColumnIndexOrThrow("group_id")));
            message.setSendTime(new java.sql.Timestamp(cursor.getLong(cursor.getColumnIndexOrThrow("send_time"))));
            message.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
            message.setMessageType(cursor.getString(cursor.getColumnIndexOrThrow("message_type")));
            message.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow("file_path")));
            message.setFileName(cursor.getString(cursor.getColumnIndexOrThrow("file_name")));
            message.setFileId(cursor.getInt(cursor.getColumnIndexOrThrow("file_id")));
            message.setIsRead(cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) == 1);
            message.setIsDownload(cursor.getInt(cursor.getColumnIndexOrThrow("is_download")) == 1);
        }
        cursor.close();
        if (message.getMessageId() == null || message.getMessageId() == 0) {
            return null;
        }
        return message;
    }

    public int getUnreadCount(String tableName) {
        Cursor cursor = database.query(tableName, null, "is_read = 0", null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void updateAllMessageRead(String tableName) {
        ContentValues values = new ContentValues();
        values.put("is_read", 1);
        database.update(tableName, values, "is_read = 0", null);
    }
}
