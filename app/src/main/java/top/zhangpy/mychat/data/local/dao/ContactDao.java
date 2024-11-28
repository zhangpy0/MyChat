package top.zhangpy.mychat.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import top.zhangpy.mychat.data.local.entity.ContactApply;
import top.zhangpy.mychat.data.local.entity.Friend;
import top.zhangpy.mychat.data.local.entity.GroupMember;

@Dao
public interface ContactDao {

    @Insert
    public void insertContactApply(ContactApply contactApply);

    @Insert
    public void insertContactApplies(List<ContactApply> newContactApplies);

    /**
     * Update contact apply (only status field)
     * @param contactApply
     */
    @Update
    public void updateContactApply(ContactApply contactApply);

    @Delete
    public void deleteContactApply(ContactApply contactApply);

    @Query("SELECT * FROM contact_apply WHERE apply_id = :applyId")
    public ContactApply getContactApplyById(Integer applyId);

    @Query("SELECT * FROM contact_apply ORDER BY apply_time DESC")
    public List<ContactApply> getAllContactAppliesSortedByApplyTime();

    @Insert
    public void insertFriend(Friend friend);

    @Query("SELECT * FROM friends WHERE user_id = :userId")
    public List<Friend> getFriendsByUserId(Integer userId);

    @Delete
    public void deleteFriend(Friend friend);

    /**
     * Update friend (only status, messageTime field)
     * @param friend
     */
    @Update
    public void updateFriend(Friend friend);

    @Query("SELECT * FROM friends ORDER BY message_time DESC")
    public List<Friend> getAllFriendsSortedByMessageTime();


    @Insert
    public void insertGroupMember(GroupMember groupMember);

    @Update
    public void updateGroupMember(GroupMember groupMember);

    @Delete
    public void deleteGroupMember(GroupMember groupMember);

    @Query("SELECT * FROM group_members WHERE group_id = :groupId")
    public List<GroupMember> getGroupMembersByGroupId(Integer groupId);

    @Delete
    public void deleteGroupMembers(List<GroupMember> groupMembers);


    @Query("SELECT * FROM friends WHERE friend_id = :friendId")
    Friend getFriendByFriendId(Integer friendId);
}
