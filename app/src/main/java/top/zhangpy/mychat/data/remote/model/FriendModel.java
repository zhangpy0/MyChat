package top.zhangpy.mychat.data.remote.model;


import lombok.Data;
import top.zhangpy.mychat.data.local.entity.Friend;
import top.zhangpy.mychat.util.Converter;

/*
List<Friend> friends = contactService.getFriends(Integer.valueOf(userId));
        for (Friend friend : friends) {
            res.add(Map.of(
                    "friendId", String.valueOf(friend.getFriendId()),
                    "status", String.valueOf(friend.getStatus())
            ));
        }
 */
@Data
public class FriendModel {
    private String friendId;
    private String status;

    public Friend mapToFriend() {
        Friend friend = new Friend();
        friend.setFriendId(Integer.valueOf(friendId));
        friend.setStatus(status);
        friend.setCreatedAt(Converter.fromUnixTimestamp(System.currentTimeMillis() / 1000));
        friend.setMessageTime(Converter.fromUnixTimestamp(System.currentTimeMillis() / 1000));
        return friend;
    }

    public Friend mapToFriend(Integer userId) {
        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setFriendId(Integer.valueOf(friendId));
        friend.setStatus(status);
        friend.setCreatedAt(Converter.fromUnixTimestamp(System.currentTimeMillis() / 1000));
        friend.setMessageTime(Converter.fromUnixTimestamp(System.currentTimeMillis() / 1000));
        return friend;
    }
}
