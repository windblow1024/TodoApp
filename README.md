# TodoApp — 全能待办事项管理 Android App

一个功能完整的 Android 待办事项管理应用，基于 Java + Room 数据库开发，支持任务优先级、分类标签、时间追踪、日历同步、可视化统计等高级功能。GitHub Actions 自动构建 APK。

## 核心功能

### 📋 任务管理

- **任务列表** — RecyclerView 展示所有任务，支持排序（时间/优先级/截止日期）
- **添加/编辑任务** — 完整的任务创建表单，包含标题、说明备注、优先级、分类、截止日期等
- **完成任务** — 点击复选框切换完成状态，带删除线动画和震动反馈
- **删除任务** — 左滑删除或点击删除按钮，支持撤销
- **搜索任务** — 顶部实时搜索框，按标题筛选
- **分类视图** — 全部 / 待办 / 已完成 三个标签页

### ⭐ 任务优先级与分类

- **优先级分级（艾森豪威尔矩阵）**
  - 🔴 重要且紧急（P0）
  - 🟠 重要但不紧急（P1）
  - 🟡 紧急但不重要（P2）
  - ⚪ 低优先级（P3）
- **类别/标签系统**
  - 预设类别：工作、个人、学习、健身、其他
  - 支持自定义标签
  - 按类别筛选视图

### ⏰ 时间与进度追踪

- **截止日期与时间** — 日期+时间选择器，精确到分钟
- **状态跟踪** — 自定义状态：待办、进行中、计划中、阻塞、已完成
- **重复提醒设置**
  - 不重复
  - 每天
  - 每周
  - 每月
  - 自定义（支持选择具体星期几）
- **系统日历同步** — 同步到系统日历，到时系统通知提醒
  - 添加任务时可选同步到日历
  - 任务完成自动移除日历事件
  - 任务更新时同步更新日历事件

### 📎 任务详情与附件

- **说明备注** — 富文本备注，支持多行文本、URL 链接
- **附件支持**
  - 拍照添加照片
  - 从图库选取图片
  - 扫描文稿
  - 附件预览（缩略图展示）
- **任务详情页** — 点击任务进入完整详情编辑界面

### 📊 可视化与统计

- **可视化仪表板**
  - 日历视图 — 以日历形式展示各天的任务分布
  - 完成率环形图 — 直观展示已完成 vs 未完成比例
  - 优先级分布柱状图 — 按优先级分组的任务数量
  - 类别分布饼图 — 按类别的任务数量
- **完成状态追踪**
  - 每日完成进度条
  - 本周趋势（每日完成数）
  - 总任务数 / 已完成 / 待办统计

### 🎨 视觉设计

- **深色模式** — 跟随系统或手动切换
- **主题色自定义** — 蓝/绿/紫/橙 四色主题
- **iOS 风格** — 圆角卡片、清晰层次

### 💾 数据管理

- **Room 数据库** — 类型安全的 SQLite 封装，支持数据库迁移
- **数据导出** — 导出为 JSON 文件
- **数据导入** — 从 JSON 文件恢复

## 技术栈

| 层级 | 技术 |
|------|------|
| 语言 | Java 8 |
| 最小 SDK | 21 (Android 5.0) |
| 目标 SDK | 34 |
| 数据库 | **Room** (androidx.room) |
| 日历 | CalendarContract (系统日历 API) |
| UI | ConstraintLayout + RecyclerView + CardView + Material Components |
| 图表 | 自定义 Canvas 绘制（环形图、柱状图、饼图） |
| 动画 | ViewPropertyAnimator + RecyclerView ItemAnimator |
| 构建 | Gradle 8.5 + AGP 8.2.2 |
| CI/CD | GitHub Actions → 自动构建 APK |

## 权限

```xml
<uses-permission android:name="android.permission.READ_CALENDAR" />
<uses-permission android:name="android.permission.WRITE_CALENDAR" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

## 数据模型 (Room Entity)

```java
@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    int id;
    String title;            // 标题
    String description;      // 说明备注
    int priority;            // 优先级: 0=P3低, 1=P2紧急不重要, 2=P1重要不紧急, 3=P0重要紧急
    String category;         // 类别: work/personal/study/fitness/other/custom
    String customTag;        // 自定义标签
    String status;           // 状态: todo/doing/planned/blocked/done
    boolean completed;       // 是否完成
    long createdAt;          // 创建时间
    long dueDate;            // 截止日期 (0=无)
    String repeatType;       // 重复: none/daily/weekly/monthly/custom
    String repeatDays;       // 自定义重复日 (JSON数组: [1,3,5] 表示周一三五)
    long calendarEventId;    // 系统日历事件ID (0=未同步)
    String attachmentPath;   // 附件路径
    String attachmentType;   // 附件类型: image/pdf/note
}
```

## 项目结构

```
TodoApp/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/todo/
│       │   ├── TodoApplication.java         — Application
│       │   ├── MainActivity.java            — 主界面
│       │   ├── TaskDetailActivity.java      — 任务详情/编辑
│       │   ├── DashboardActivity.java       — 仪表板统计
│       │   ├── SettingsActivity.java        — 设置
│       │   ├── data/
│       │   │   ├── Task.java                — Room Entity
│       │   │   ├── TaskDao.java             — Room DAO
│       │   │   └── AppDatabase.java         — Room Database
│       │   ├── ui/
│       │   │   ├── TaskAdapter.java         — 列表适配器
│       │   │   ├── CalendarView.java        — 日历视图
│       │   │   └── charts/                  — 图表组件
│       │   └── util/
│       │       ├── CalendarHelper.java      — 系统日历同步
│       │       ├── RepeatHelper.java        — 重复提醒
│       │       └── ThemeUtil.java           — 主题工具
│       └── res/
├── build.gradle.kts
├── settings.gradle.kts
├── .github/workflows/build-apk.yml
└── README.md
```

## 版本

| 版本 | 内容 |
|------|------|
| v1.0 | 基础待办管理 |
| v2.0 | 优先级、日历同步、搜索、深色模式 |
| v3.0 | Room 重构、艾森豪威尔矩阵、分类标签、附件、重复提醒、仪表板统计 |

## 构建与下载

```bash
./gradlew assembleDebug
```

APK 在 Actions 页面 Artifacts 下载。

## License

MIT
