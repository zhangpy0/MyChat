package top.zhangpy.mychat.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import top.zhangpy.mychat.data.local.entity.Group;
import top.zhangpy.mychat.data.local.entity.GroupInfo;

@Dao
public interface GroupDao {

    @Insert
    public void insertGroup(Group group);

    @Query("SELECT * FROM groups WHERE group_id = :groupId")
    public Group getGroupById(Integer groupId);

    @Query("SELECT * FROM groups ORDER BY message_time DESC")
    List<Group> getAllGroupsSortedByMessageTime();

    @Delete
    public void deleteGroup(Group group);

    /**
     * Update group (only message_time field)
     * @param group
     */
    @Update
    public void updateGroup(Group group);

    @Insert
    public void insertGroupInfo(GroupInfo groupInfo);

    @Query("SELECT * FROM group_info WHERE group_id = :groupId")
    public GroupInfo getGroupInfoById(Integer groupId);

    @Update
    public void updateGroupInfo(GroupInfo groupInfo);

    @Delete
    public void deleteGroupInfo(GroupInfo groupInfo);
}
