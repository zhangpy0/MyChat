package top.zhangpy.mychat.util;

import lombok.Data;

@Data
public class NameSort implements Comparable<NameSort>{

    private String name; // 姓名
    private String pinyin; // 姓名对应的拼音
    private String firstLetter; // 拼音的首字母

    public NameSort(String name) {
        this.name = name;
        this.pinyin = Converter.getPinYin(name);
        this.firstLetter = pinyin.substring(0, 1).toUpperCase();
        if (!firstLetter.matches("[A-Z]")) {
            firstLetter = "#";
        }
    }

    @Override
    public int compareTo(NameSort o) {
        if (firstLetter.equals("#") && !o.getFirstLetter().equals("#")) {
            return 1;
        } else if (!firstLetter.equals("#") && o.getFirstLetter().equals("#")) {
            return -1;
        } else {
            return pinyin.compareToIgnoreCase(o.getPinyin());
        }
    }
}
