package top.zhangpy.mychat.util;

import androidx.room.TypeConverter;

import java.sql.Timestamp;

public class Converter {

    @TypeConverter
    public static Timestamp fromUnixTimestamp(Long value) {
        return value == null ? null : new Timestamp(value * 1000);
    }

    @TypeConverter
    public static Long dateToTimestamp(Timestamp date) {
        return date == null ? null : date.getTime() / 1000;
    }
}
