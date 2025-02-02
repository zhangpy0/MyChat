# MyChat

**MyChat** 是一款仿微信的即时通讯应用，使用原生安卓开发。

**安卓客户端 Github 仓库**: [zhangpy0/MyChat](https://github.com/zhangpy0/MyChat)

**服务端 Github 仓库**: [zhangpy0/MyChat_backend](https://github.com/zhangpy0/MyChat_backend)

---

持续开发中。

## Languages
| language | files | code | comment | blank | total |
| :--- | ---: | ---: | ---: | ---: | ---: |
| Java | 113 | 8,810 | 1,145 | 1,982 | 11,937 |
| XML | 69 | 2,772 | 200 | 313 | 3,285 |
| Gradle | 3 | 83 | 31 | 24 | 138 |
| Markdown | 1 | 61 | 0 | 34 | 95 |
| Batch | 1 | 41 | 32 | 22 | 95 |
| Java Properties | 2 | 13 | 18 | 1 | 32 |

---

## 功能描述

### 核心功能

#### 1. 用户管理：

- **快速注册与安全登录**：
  - 支持账号、邮箱等多种注册方式。
  - 提供密码登录和验证码修改密码功能，保障账号安全。
  
- **个人信息管理**：
  - 用户可自由设置头像、昵称、性别等信息。

---

#### 2. 聊天功能：单聊与群聊

- **一对一聊天**：
  - 支持文字、图片和文件传输。
  
- **群聊**：
  - 支持多人实时互动，可管理群成员及创建群公告。
  
- **多媒体支持**：
  - 内置图片预览功能，可直接查看好友发送的图片和文件。

---

#### 3. 联系人功能：

- **好友管理**：
  - 添加好友功能支持通过账号搜索。
  
- **好友信息查看**：
  - 点击好友即可查看详细信息。

---

#### 4. 群组功能：高效团队协作 (正在开发)

- **创建与管理群组**：
  - 用户可创建或加入群组，与多人同时交流。
  - 群主可管理群成员、设置群公告。
  
- **群聊互动** ：
  - 支持群内文件发送。
  - 群组内支持共享信息，便于团队协作。

---

#### 5. 数据与存储：稳定可靠的技术保障

- **本地数据存储**：
  - 通过高效的数据库管理，保存聊天记录、好友列表等重要数据。
  - 离线状态下，所有消息均可本地查看。
  
- **云端同步**：
  - 支持历史数据云端同步，切换设备时聊天记录无缝迁移。
  
- **安全加密**(部分完成)：
  - 所有数据均采取加密存储与传输机制，确保用户隐私。

---

#### 6. 界面与交互设计：极致的用户体验

- **简洁直观的 UI**：
  - 采用现代化设计风格，界面清爽，操作流畅。
  
- **高效的导航与功能定位**：
  - 左侧侧边栏与底部导航栏设计，快速切换聊天、联系人、设置等模块。