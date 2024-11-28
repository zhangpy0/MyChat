package top.zhangpy.mychat.ui.model;

import lombok.Data;

@Data
public class TabModel {
    private int imageResource;
    private int textColor;

    public TabModel(int imageResource, int textColor) {
        this.imageResource = imageResource;
        this.textColor = textColor;
    }

}
