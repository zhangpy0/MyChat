# MyChat-Android

## 功能介绍

## 概要设计

### 1. 项目结构

### 2. 数据流

#### 1. 注册

需要：userId, email, password, authCode

userId, email, password 由用户输入

authCode <- sendEmailForRegister

password -> passwordHash

userId, email, passwordHash, authCode -> register

result.code == 200 --> 注册成功 --> 跳转到登录界面

#### 2. 登录

UserDao -> User 存在跳过

需要：userId/email, password

userId/email, password 由用户输入

password -> passwordHash

userId/email, passwordHash -> login

login -> result -> User -> UserDao --> 跳转到主界面 聊天列表

#### 3. 聊天列表

(ContactDao -> getAllFriends) && (GroupDao -> getAllGroupsSortedByMessageTime)
-> List<Friend> && List<Group> -> List<ChatListItem>

开线程remoteUpdate:
List<FriendModel> getFriends -> 
List<Integer> getGroups

ChatListItem: userId, groupId, userNickname, groupName, 
lastMessageContent, lastMessageTime, lastMessageSender, avatarPath

(Friend -> UserProfile && Group -> GroupInfo && ChatMessage) -> ChatListItem

#### 4. websocket收到消息

##### 1. 服务器消息(sendId == 0, groupId == 0)

```
1.chatMessage.setContent(userId + ":friend request get"); userId 对你发起了好友请求
2.chatMessage.setContent(userId + ":" + groupId + ":group request get"); userId 对你的groupId发起了入群请求
3.chatMessage.setContent(userId + ":friend request has been processed"); userId 已经处理了你的好友请求
4.chatMessage.setContent(groupId + ":group request has been processed"); groupId 已经处理了你的入群请求
5.chatMessage.setContent(groupId + ":remove you from group"); groupId 已经将你移出了群聊
```

1.  getContactApplyFromOthers -> insertContactApply
2.  getContactApplyFromOthers -> insertContactApply
3.  getContactApplyFromMe -> updateContactApply
4.  getContactApplyFromMe -> updateContactApply
5.  getGroups -> (deleteGroup && deleteGroupInfo && (getGroupMembersByGroupId -> deleteGroupMembers))

#### 2. 聊天消息

## 软件架构