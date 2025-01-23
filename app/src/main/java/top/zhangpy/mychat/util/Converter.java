package top.zhangpy.mychat.util;

import androidx.room.TypeConverter;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

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

    public static String getPinYinHeadChar(String chines) {
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        char[] chars = chines.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] > 128) {
                try {
                    sb.append(PinyinHelper.toHanyuPinyinStringArray(chars[i], defaultFormat)[0].charAt(0));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }
    /**     * 获取汉字字符串的第一个字母     */
    public static String getPinYinFirstLetter(String str) {
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        char c = str.charAt(0);
        String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
        if (pinyinArray != null) {
            sb.append(pinyinArray[0].charAt(0));
        }
        else {
            sb.append(c);
        }
        return sb.toString();
    }
    /**     * 获取汉字字符串的汉语拼音，英文字符不变     */
    public static String getPinYin(String s) {
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        char[] nameChar = s.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        for (int i = 0; i < nameChar.length; i++) {
            char currentChar = nameChar[i];

            // 判断是否是汉字（Unicode范围）
            if (Character.toString(currentChar).matches("[\u4e00-\u9fa5]")) {
                try {
                    // 如果是汉字，转拼音
                    sb.append(PinyinHelper.toHanyuPinyinStringArray(currentChar, defaultFormat)[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                    sb.append("#"); // 出现异常时，使用#字符
                }
            }
            // 判断是否是英文字符（ASCII范围）
            else if (Character.isLetter(currentChar)) {
                sb.append(currentChar); // 英文字符直接保留
            }
            else {
                sb.append("#"); // 其他字符转为#
            }
        }
        return sb.toString();
    }
}
