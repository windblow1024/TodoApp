# TodoApp — 待办事项管理 Android App

一个功能完整的 Android 待办事项管理应用，基于 Java + SQLite 开发，GitHub Actions 自动构建 APK。

## 功能

### 📋 任务管理

- **任务列表** — RecyclerView 展示所有待办事项，支持排序
- **添加任务** — 对话框输入标题 + 优先级 + 截止日期 + 日历同步
- **完成任务** — 点击复选框标记完成，文字变灰 + 删除线
- **编辑任务** — 点击任务进入详情页编辑标题、优先级、日期
- **删除任务** — 左滑删除或点击删除按钮，支持撤销
- **分类视图** — 全部 / 待办 / 已完成 三个标签页切换
- **搜索功能** — 顶部搜索框实时筛选任务
- **排序** — 按创建时间 / 截止日期 / 优先级排序

### 🔔 日历同步与提醒

- **截止日期** — 添加任务时可选择截止日期
- **系统日历同步** — 可选同步到系统日历 App
- **到时提醒** — 系统日历自动触发通知提醒
- **完成自动清除** — 任务完成后自动移除日历提醒

### ⭐ 优先级

- 高（红色标签）/ 中（橙色标签）/ 低（灰色标签）
- 优先级颜色标识，一目了然

### 🎨 交互体验

- **滑动删除** — 左滑任务项出现删除按钮
- **撤销删除** — 删除后底部弹出 Snackbar "已删除，撤销？"
- **回车添加** — 键盘回车直接确认添加
- **空状态插图** — 各分类空列表时有图标和友好文案
- **震动反馈** — 完成任务时轻微震动
- **加载动画** — 列表切换时的淡入动画

### 🎭 视觉设计

- **深色模式** — 跟随系统自动切换深色/浅色主题
- **主题色自定义** — 支持蓝/绿/紫/橙四色主题切换
- **完成动效** — 勾选时文字划掉动画
- **数据统计** — 顶部显示完成率进度条

### 💾 数据管理

- **SQLite 持久化** — 本地存储，重启不丢数据
- **数据导出** — 导出为 JSON 文件
- **数据导入** — 从 JSON 文件恢复数据
- **数据库升级** — 支持版本迁移

## 界面示意

```
浅色模式：                   深色模式：
┌──────────────────┐        ┌──────────────────┐
│  待办事项    🔍   │        │  待办事项    🔍   │
│  ████████░░ 60%  │        │  ████████░░ 60%  │
├──────────────────┤        ├──────────────────┤
│  全部·待办·已完成 │        │  全部·待办·已完成 │
├──────────────────┤        ├──────────────────┤
│ 🔴 买 groceries   │        │ 🔴 买 groceries   │
│    截止 06/15    │        │    截止 06/15    │
│ 🟡 写周报        │        │ 🟡 写周报        │
│    截止 06/14    │        │    截止 06/14    │
│ ⚪ 健身          │        │ ⚪ 健身          │
│                   │        │                   │
│              [＋] │        │              [＋] │
└──────────────────┘        └──────────────────┘
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 语言 | Java 8 |
| 最小 SDK | 21 (Android 5.0) |
| 目标 SDK | 34 |
| 数据库 | **SQLite** (android.database.sqlite) |
| 日历 | **CalendarContract** (系统日历 API) |
| UI | ConstraintLayout + RecyclerView + CardView + Material Components |
| 动画 | ViewPropertyAnimator + RecyclerView ItemAnimator |
| 构建 | Gradle 8.5 + AGP 8.2.2 |
| CI/CD | GitHub Actions → 自动构建 APK |

## 权限

```xml
<uses-permission android:name="android.permission.READ_CALENDAR" />
<uses-permission android:name="android.permission.WRITE_CALENDAR" />
<uses-permission android:name="android.permission.VIBRATE" />
```

## 项目结构

```
TodoApp/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/todo/
│       │   ├── TodoApplication.java       — Application，全局 DB 实例
│       │   ├── MainActivity.java          — 主界面，分类切换、搜索、排序
│       │   ├── TaskDetailActivity.java    — 任务详情/编辑界面
│       │   ├── Task.java                  — 数据模型
│       │   ├── TaskAdapter.java           — 列表适配器（含滑动删除）
│       │   ├── DatabaseHelper.java        — SQLite 增删改查
│       │   ├── CalendarHelper.java        — 系统日历同步
│       │   └── SettingsActivity.java      — 主题色设置
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml
│           │   ├── activity_task_detail.xml
│           │   ├── activity_settings.xml
│           │   ├── item_task.xml
│           │   └── dialog_add_task.xml
│           └── values/...
├── build.gradle.kts
├── settings.gradle.kts
├── .github/workflows/build-apk.yml
└── README.md
```

## 数据模型

```java
Task {
    int id                // 自增主键
    String title          // 任务标题
    boolean completed     // 是否完成
    long createdAt        // 创建时间戳
    int priority          // 优先级: 0=低, 1=中, 2=高
    long dueDate          // 截止日期时间戳 (0=无)
    long calendarEventId  // 系统日历事件ID (0=未同步)
}
```

## 版本

| 版本 | 内容 |
|------|------|
| v1.0 | 基础待办管理：增删改查、分类视图、SQLite 持久化 |
| v2.0 | 优先级、截止日期、日历同步提醒、搜索、滑动删除、深色模式、主题色、导出导入、统计、动画 |

## 构建与下载

push 到 `main` 后自动构建：

```bash
./gradlew assembleDebug
```

APK 在 Actions 页面 Artifacts 下载。

## License

MIT
