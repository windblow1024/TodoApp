# TodoApp — 待办事项管理 Android App

一个轻量、美观的 Android 待办事项管理应用，基于 Java + SQLite 开发，GitHub Actions 自动构建 APK。

## 功能

### v1.0

- **任务列表** — 以列表展示所有待办事项，按创建时间倒序排列
- **添加任务** — 点击底部浮动按钮，弹出对话框输入标题
- **完成任务** — 点击复选框标记完成，文字变灰
- **删除任务** — 点击任务右侧删除按钮，确认后删除
- **分类视图** — 全部 / 待办 / 已完成 三个标签页切换
- **空状态提示** — 列表为空时显示友好的提示文案
- **本地持久化** — 使用 **SQLite** 数据库存储，重启 App 数据不丢失

### 未来规划

- 任务优先级（高/中/低）
- 任务截止日期
- 搜索功能
- 深色模式
- 数据导出备份

## 界面

iOS 风格，白色背景 + 蓝色主色调。

```
┌──────────────────────────┐
│  待办事项                  │  ← 标题栏
├──────────────────────────┤
│  全部  ·  待办  ·  已完成  │  ← 分类切换（蓝色指示线）
├──────────────────────────┤
│  ☐ 买 groceries           │  ← 未完成（黑色文字）
│     06/13 14:30           │  ← 创建时间
│         [🗑️]              │  ← 删除按钮
├──────────────────────────┤
│  ☑️ 写周报                 │  ← 已完成（灰色文字）
│     06/13 10:00           │
│         [🗑️]              │
├──────────────────────────┤
│                    [＋]   │  ← 浮动添加按钮
└──────────────────────────┘
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 语言 | Java 8 |
| 最小 SDK | 21 (Android 5.0) |
| 目标 SDK | 34 |
| 数据库 | **SQLite** (android.database.sqlite) |
| UI | ConstraintLayout + RecyclerView + CardView |
| 构建 | Gradle 8.5 + AGP 8.2.2 |
| CI/CD | GitHub Actions → 自动构建 APK |

## 项目结构

```
TodoApp/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/todo/
│       │   ├── TodoApplication.java   — Application，提供全局 DB 实例
│       │   ├── MainActivity.java      — 主界面，分类切换、增删改查
│       │   ├── Task.java              — 数据模型（id, title, completed, createdAt）
│       │   ├── TaskAdapter.java       — RecyclerView 适配器
│       │   └── DatabaseHelper.java    — SQLite 帮助类（增删改查）
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml   — 主界面布局
│           │   ├── item_task.xml       — 任务项布局
│           │   └── dialog_add_task.xml — 添加任务对话框
│           └── values/...
├── build.gradle.kts
├── settings.gradle.kts
├── .github/workflows/build-apk.yml
└── README.md
```

## 数据模型

```java
Task {
    int id            // 自增主键
    String title      // 任务标题
    boolean completed // 是否完成
    long createdAt    // 创建时间戳
}
```

数据库表 `tasks`，通过 `DatabaseHelper` 操作，支持 CRUD 和按状态筛选。

## 构建与下载

push 到 `main` 分支后 GitHub Actions 自动构建：

```bash
# 本地构建
./gradlew assembleDebug
```

APK 在 Actions 页面 Artifacts 中下载（`todo-debug-apk.zip`）。

## 版本

| 版本 | 内容 |
|------|------|
| v1.0 | 基础待办管理：增删改查、分类视图、SQLite 持久化 |

## License

MIT