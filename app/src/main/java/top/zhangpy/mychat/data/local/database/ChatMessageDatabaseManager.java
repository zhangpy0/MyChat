package top.zhangpy.mychat.data.local.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class ChatMessageDatabaseManager {
    private static final String DATABASE_NAME = "chat_messages.db";
    private static volatile ChatMessageDatabaseManager INSTANCE;
    private final SQLiteDatabase database;

    private ChatMessageDatabaseManager(Context context) {
        database = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
    }

    public static ChatMessageDatabaseManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ChatMessageDatabaseManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ChatMessageDatabaseManager(context);
                }
            }
        }
        return INSTANCE;
    }

    // 创建聊天对象的表
    public void createChatTable(String tableName) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "message_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender_id INTEGER, " +
                "group_id INTEGER, " +
                "send_time TIMESTAMP, " +
                "content TEXT, " +
                "message_type TEXT, " +
                "file_path TEXT, " +
                "file_name TEXT, " +
                "file_id INTEGER, " +
                "is_read INTEGER, " +
                "is_download INTEGER)";
        database.execSQL(createTableSQL);
    }

    // 删除聊天表
    public void deleteChatTable(String tableName) {
        String dropTableSQL = "DROP TABLE IF EXISTS " + tableName;
        database.execSQL(dropTableSQL);
    }

    // 获取数据库实例
    public SQLiteDatabase getDatabase() {
        return database;
    }

    public boolean isTableExist(String tableName) {
        String sql = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
        return database.rawQuery(sql, null).moveToFirst();
    }
}
