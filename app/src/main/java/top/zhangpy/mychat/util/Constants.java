package top.zhangpy.mychat.util;


/*

```
1.chatMessage.setContent(userId + ":friend request get"); userId 对你发起了好友请求
2.chatMessage.setContent(userId + ":" + groupId + ":group request get"); userId 对你的groupId发起了入群请求
3.chatMessage.setContent(userId + ":friend request has been processed"); userId 已经处理了你的好友请求
4.chatMessage.setContent(groupId + ":group request has been processed"); groupId 已经处理了你的入群请求
5.chatMessage.setContent(groupId + ":remove you from group"); groupId 已经将你移出了群聊
```
 */
public class Constants {

    public static final String SERVER_IP = "192.168.17.134";

    public static final int MAIN_SERVER_PORT = 8080;

    public static final int WEB_SOCKET_PORT = 8081;




}
