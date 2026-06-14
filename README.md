# TodoApp — 待办清单

一个功能完整的 Android 待办事项管理应用，基于 Java + Room 数据库开发。支持任务管理、优先级（艾森豪威尔矩阵 / 高-中-低）、分类标签、开始/截止时间、系统日历同步、图片附件、数据统计仪表板。GitHub Actions 自动构建并发布签名的 Release APK。

应用名：**待办清单**

---

## 核心功能

### 📋 任务管理

- **任务列表** — RecyclerView + CardView 展示所有任务，支持三种排序：按开始时间 / 按截止时间 / 按优先级
- **添加/编辑任务** — 完整表单：标题、说明备注（多行文本）、优先级、状态、分类、开始/截止日期时间、附件
- **完成任务** — 点击复选框切换完成状态，带震动反馈
- **删除任务** — 点击删除按钮，Snackbar 撤销支持
- **搜索任务** — 顶部实时搜索框，按标题筛选
- **分类视图** — 全部 / 待办 / 已完成 三个标签页

### ⭐ 双优先级模式

可在设置中切换，默认为 **高/中/低**：

**模式 1 — 艾森豪威尔矩阵（4 级）：**

| 级别 | 标签 | 说明 |
|------|------|------|
| P0 🔴 | 重要且紧急 | `PRIORITY_P0_CRITICAL` |
| P1 🟠 | 重要不紧急 | `PRIORITY_P1_IMPORTANT` |
| P2 🟡 | 紧急不重要 | `PRIORITY_P2_URGENT` |
| P3 ⚪ | 低优先级 | `PRIORITY_P3_LOW` |

**模式 2 — 简单分级（3 级）：**

| 级别 | 标签 | 说明 |
|------|------|------|
| 🔴 高 | `PRIORITY_HIGH` | 高优先级 |
| 🟡 中 | `PRIORITY_MEDIUM` | 中优先级 |
| ⚪ 低 | `PRIORITY_LOW` | 低优先级 |

### 🏷️ 分类与标签

- **预设类别**：工作 / 个人 / 学习 / 健身 / 其他（Spinner 选择）
- **自定义标签**：选中「其他」后可输入自定义标签名称

### ⏰ 时间管理

- **开始日期与时间** — 可单独设置任务开始时间（DatePicker + TimePicker）
- **截止日期与时间** — 精确到分钟，支持校验（截止时间不能早于开始时间）
- **系统日历同步** — 同步到系统日历，到时系统通知提醒
  - 添加/编辑任务时可选同步到日历
  - 任务完成自动移除日历事件
  - 任务更新时同步更新日历事件
  - **MIUI 兼容**：优先使用已有系统日历，自动适配小米日历
  - **详细错误反馈**：同步失败时显示具体原因（权限不足 / 日历不存在 / 写入失败）

### 📎 附件支持

- 从图库选取图片（`MediaStore.ACTION_PICK_IMAGES`）
- 附件缩略图预览（ImageView，200dp 高度，centerCrop 缩放）
- **点击附件** — 调用系统图片查看器（FileProvider）
- **删除附件** — 附件下方红色删除按钮

### 📊 仪表板统计

- **统计卡片** — 总任务 / 待办 / 已完成 数量
- **完成率** — 百分比显示
- **环形图** — 自定义 Canvas 绘制（DonutChartView），已完成（绿色）+ 待办（蓝色）+ 背景（灰色）
- **今日任务** — 列出当天截止的任务

### 🎨 视觉与主题

- **深色模式** — 设置页切换，跟随 `AppCompatDelegate.MODE_NIGHT_YES/NO`
- **主题色自定义** — 蓝色 / 绿色 / 紫色 / 橙色 四色主题
- **亮色主题** — 白底深色文字，`Theme.MaterialComponents.Light.NoActionBar`
- **暗色主题** — 黑底白字，`Theme.MaterialComponents.NoActionBar`

### 💾 数据存储

- **Room 数据库** — `tasks` 表，支持 LiveData 响应式查询
- **数据导出/导入** — JSON 格式文件导入导出

---

## 任务状态

| 状态 | 常量 | 说明 |
|------|------|------|
| 待办 | `STATUS_TODO` | 待处理 |
| 进行中 | `STATUS_DOING` | 正在进行 |
| 计划中 | `STATUS_PLANNED` | 已计划 |
| 暂停 | `STATUS_PAUSED` | 暂停 |
| 已完成 | `STATUS_DONE` | 已完成 |

---

## 权限

| 权限 | 用途 | 备注 |
|------|------|------|
| `READ_CALENDAR` / `WRITE_CALENDAR` | 同步任务到系统日历 | 可选，未授予则跳过 |
| `VIBRATE` | 完成任务的震动反馈 | |
| `CAMERA` | 拍照添加附件（预留） | |
| `READ_EXTERNAL_STORAGE` | 读取图片附件 | `maxSdkVersion=32` |
| `READ_MEDIA_IMAGES` | Android 13+ 读取图片 | |

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 语言 | Java 11 | |
| 构建 | Gradle + AGP | 8.5 / 8.2.2 |
| 最小 SDK | Android 5.0 (API 21) | |
| 目标 SDK | Android 14 (API 34) | |
| UI | Material Components + ConstraintLayout + RecyclerView + CardView | 1.11.0 / 2.1.4 / 1.3.2 / 1.0.0 |
| 数据库 | Room | 2.6.1 |
| 日历 | CalendarContract（系统日历 API） | |
| 图表 | 自定义 Canvas 绘制（环形图） | |
| 图片选择 | `ActivityResultContracts.PickVisualMedia` | |
| 文件分享 | `FileProvider` | |
| 生命周期 | LiveData + ViewModel | 2.7.0 |
| CI/CD | GitHub Actions → 自动构建签名 Release APK + Release 发布 | |

---

## 项目结构

```
TodoApp/
├── app/
│   ├── build.gradle.kts              # 应用构建配置（含 Release 签名）
│   └── src/main/
│       ├── AndroidManifest.xml       # 应用清单、权限、FileProvider
│       ├── java/com/example/todo/
│       │   ├── TodoApplication.java  — Application 单例（数据库初始化）
│       │   ├── MainActivity.java     — 主界面（列表、搜索、标签切换）
│       │   ├── TaskDetailActivity.java — 任务详情/编辑（表单、附件、日历同步）
│       │   ├── DashboardActivity.java — 仪表板（统计、环形图、今日任务）
│       │   ├── SettingsActivity.java — 设置（深色模式、优先级模式、主题色）
│       │   ├── data/
│       │   │   ├── Task.java         — Room Entity（任务数据模型）
│       │   │   ├── TaskDao.java      — Room DAO（查询、排序、统计）
│       │   │   └── AppDatabase.java  — Room Database
│       │   ├── ui/
│       │   │   └── TaskAdapter.java  — RecyclerView 适配器
│       │   └── util/
│       │       ├── CalendarHelper.java — 系统日历同步（MIUI 兼容）
│       │       └── ThemeUtil.java     — 主题工具
│       └── res/
│           ├── layout/               — 5 个布局文件
│           ├── drawable/             — 图标、输入框背景、徽章背景
│           ├── values/               — 颜色、字符串、亮色主题
│           ├── values-night/         — 暗色主题
│           └── xml/
│               └── file_paths.xml    — FileProvider 路径配置
├── .github/workflows/
│   └── build-apk.yml                 — GitHub Actions 自动构建
├── build.gradle.kts                  — 根构建配置
├── settings.gradle.kts               — 项目设置
└── README.md
```

---

## 构建

### 自动构建（GitHub Actions）

代码 push 到 `main` 分支后自动触发：

1. **Checkout + JDK 17**
2. **Setup Gradle**
3. **Decode keystore** — 从 `KEYSTORE_BASE64` Secret 解码签名文件
4. **Build Release APK** — 使用环境变量 `KEY_STORE_PASSWORD` 和 `KEY_PASSWORD` 签名
5. **Upload APK** — 上传 `todo-release-apk` Artifact
6. **Create Release** — 当 push tag (`v*`) 时自动创建 GitHub Release，附带签名的 APK

### 手动触发构建

1. 进入仓库 **Actions** 标签页
2. 选择 **Build APK** workflow
3. 点击 **Run workflow** → 选择分支 → **Run workflow**

### 本地构建

```bash
./gradlew assembleDebug   # Debug 版本（无需签名配置）
./gradlew assembleRelease # Release 版本（需要配置 release.keystore）
```

---

## 下载 APK

1. 进入仓库 **Actions** 标签页
2. 点击最新的成功 Workflow
3. 在 **Artifacts** 区域下载 `todo-release-apk`
4. 解压后安装 `.apk` 文件

Release 版本自动发布在 [Releases](https://github.com/windblow1024/TodoApp/releases) 页面。

---

## 数据模型 (Room Entity)

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `int` (PK, autoGenerate) | 任务 ID |
| `title` | `String` | 标题 |
| `description` | `String` | 说明备注 |
| `priority` | `int` | 0=P3, 1=P2, 2=P1, 3=P0, 4=高, 5=中, 6=低 |
| `category` | `String` | 类别: work/personal/study/fitness/other |
| `custom_tag` | `String` | 自定义标签名称 |
| `status` | `String` | 状态: todo/doing/planned/paused/done |
| `completed` | `boolean` | 是否完成 |
| `created_at` | `long` | 创建时间戳 |
| `start_date` | `long` | 开始日期时间戳（0=未设置） |
| `due_date` | `long` | 截止日期时间戳（0=未设置） |
| `calendar_event_id` | `long` | 系统日历事件 ID（0=未同步） |
| `attachment_path` | `String` | 附件路径 |
| `attachment_type` | `String` | 附件类型: image/pdf/note |

---

## 版本

| 版本 | 说明 |
|------|------|
| v1.0 | 基础待办管理 |
| v2.0 | 优先级、日历同步、搜索、深色模式 |
| v3.0 | Room 重构、艾森豪威尔矩阵、分类标签、附件、仪表板统计 |
| v3.1 | 修复点击任务项 Bug，升级 Java 11 |
| v3.2 | 修复详情页跳转，Release 自动发布 |
| v3.3 | 修复主线程数据库访问异常，状态 RadioGroup Bug |
| v3.4 | 说明备注多行、附件预览与删除 |
| v3.5 | FileProvider 替代 Uri.fromFile |
| v3.6 | 更新说明书 |
| v3.7 | 手动排序 |
| v3.8 | 说明备注默认 3 行 |
| v3.9 | 日历同步增强，MIUI 兼容，详细错误反馈 |
| v4.0 | 新增开始日期，去除重复提醒 |
| v4.1 | 开始时间/截止时间/优先级排序 |
| v4.2 | 排序方式更新：按开始时间/按截止时间/按优先级；截止时间校验 |
| v5.0 | 🆕 双优先级模式（艾森豪威尔矩阵 / 高-中-低），设置页切换，列表颜色适配 |

---

## License

MIT

---

*作者：风吹吹 · E-mail: windblow1024@gmail.com*
