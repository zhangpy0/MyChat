package top.zhangpy.mychat.util;

public class StringJudge {
    public static boolean isInRange(String str) {
        if (str == null) return false;
        return str.length() >= 5 && str.length() <= 20;
    }

    public static boolean isIdLegal(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return str.length() >= 5 && str.length() <= 10;
    }

    public static boolean isEmail(String str) {
        if (str == null) return false;
        return str.contains("@") && str.contains(".") && str.length() <= 30;
    }
}