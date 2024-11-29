package top.zhangpy.mychat.ui.model;

import lombok.Data;
import top.zhangpy.mychat.util.NameSort;

@Data
public class ContactListItem implements Comparable<ContactListItem> {
    Integer id;
    String name;
    String avatarPath;
    String type; // user or group or page
    NameSort nameSort;

    public ContactListItem(Integer id, String name, String avatarPath, String type) {
        this.id = id;
        this.name = name;
        this.avatarPath = avatarPath;
        this.type = type;
        this.nameSort = new NameSort(name);
    }

    @Override
    public int compareTo(ContactListItem o) {
        return nameSort.compareTo(o.getNameSort());
    }
}
