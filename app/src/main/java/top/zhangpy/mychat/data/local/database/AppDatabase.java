package top.zhangpy.mychat.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import top.zhangpy.mychat.data.local.dao.ContactDao;
import top.zhangpy.mychat.data.local.dao.GroupDao;
import top.zhangpy.mychat.data.local.dao.UserDao;
import top.zhangpy.mychat.data.local.dao.UserProfileDao;
import top.zhangpy.mychat.data.local.entity.ContactApply;
import top.zhangpy.mychat.data.local.entity.Friend;
import top.zhangpy.mychat.data.local.entity.Group;
import top.zhangpy.mychat.data.local.entity.GroupInfo;
import top.zhangpy.mychat.data.local.entity.GroupMember;
import top.zhangpy.mychat.data.local.entity.User;
import top.zhangpy.mychat.data.local.entity.UserProfile;
import top.zhangpy.mychat.util.Converter;

@Database(
        entities = {
                ContactApply.class,
                Friend.class,
                Group.class,
                GroupInfo.class,
                GroupMember.class,
                User.class,
                UserProfile.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters({Converter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract ContactDao contactDao();
    public abstract GroupDao groupDao();
    public abstract UserDao userDao();
    public abstract UserProfileDao userProfileDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context, boolean inMemory) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    if (inMemory) {
                        INSTANCE = Room.inMemoryDatabaseBuilder(
                                context.getApplicationContext(),
                                AppDatabase.class
                        ).allowMainThreadQueries().build();
                        System.out.println("Using in-memory database for testing");
                    } else {
                        INSTANCE = Room.databaseBuilder(
                                context.getApplicationContext(),
                                AppDatabase.class,
                                "main_database"
                        ).fallbackToDestructiveMigration().build();
                        System.out.println("Using file-based database");
                    }
                }
            }
        } else {
            System.out.println("Reusing existing database instance");
        }
        return INSTANCE;
    }

    public static AppDatabase getTestInstance(Context context) {
        return Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "test_database" // 独立的测试数据库
                ).allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    }

    public void clearDatabase() {
        clearAllTables();
    }
}
