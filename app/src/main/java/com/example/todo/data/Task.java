package com.example.todo.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 任务数据模型 (Room Entity)
 * v3.0: 完整字段
 */
@Entity(tableName = "tasks")
public class Task {

    // 优先级常量 (艾森豪威尔矩阵)
    public static final int PRIORITY_P3_LOW = 0;           // 低优先级
    public static final int PRIORITY_P2_URGENT = 1;        // 紧急但不重要
    public static final int PRIORITY_P1_IMPORTANT = 2;     // 重要但不紧急
    public static final int PRIORITY_P0_CRITICAL = 3;      // 重要且紧急

    // 简单优先级（高/中/低）
    public static final int PRIORITY_HIGH = 4;
    public static final int PRIORITY_MEDIUM = 5;
    public static final int PRIORITY_LOW = 6;

    // 状态常量
    public static final String STATUS_TODO = "todo";
    public static final String STATUS_DOING = "doing";
    public static final String STATUS_PLANNED = "planned";
    public static final String STATUS_PAUSED = "paused";
    public static final String STATUS_DONE = "done";

    // 重复类型
    public static final String REPEAT_NONE = "none";
    public static final String REPEAT_DAILY = "daily";
    public static final String REPEAT_WEEKLY = "weekly";
    public static final String REPEAT_MONTHLY = "monthly";
    public static final String REPEAT_CUSTOM = "custom";

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "priority")
    private int priority;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "custom_tag")
    private String customTag;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "completed")
    private boolean completed;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "start_date")
    private long startDate;

    @ColumnInfo(name = "due_date")
    private long dueDate;

    @ColumnInfo(name = "calendar_event_id")
    private long calendarEventId;

    @ColumnInfo(name = "attachment_path")
    private String attachmentPath;

    @ColumnInfo(name = "attachment_type")
    private String attachmentType;

    public Task() {}

    public Task(String title) {
        this.title = title;
        this.description = "";
        this.priority = PRIORITY_P3_LOW;
        this.category = "other";
        this.customTag = "";
        this.status = STATUS_TODO;
        this.completed = false;
        this.createdAt = System.currentTimeMillis();
        this.startDate = 0;
        this.dueDate = 0;
        this.calendarEventId = 0;
        this.attachmentPath = "";
        this.attachmentType = "";
    }

    // ===== Getters & Setters =====

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCustomTag() { return customTag; }
    public void setCustomTag(String customTag) { this.customTag = customTag; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }

    public long getCalendarEventId() { return calendarEventId; }
    public void setCalendarEventId(long calendarEventId) { this.calendarEventId = calendarEventId; }

    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }

    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }

    // ===== 辅助方法 =====

    public boolean hasStartDate() { return startDate > 0; }
    public boolean hasDueDate() { return dueDate > 0; }
    public boolean hasCalendarEvent() { return calendarEventId > 0; }
    public boolean hasAttachment() { return attachmentPath != null && !attachmentPath.isEmpty(); }
    public boolean hasDescription() { return description != null && !description.isEmpty(); }

    public String getPriorityLabel() {
        switch (priority) {
            case PRIORITY_P0_CRITICAL: return "重要且紧急";
            case PRIORITY_P1_IMPORTANT: return "重要不紧急";
            case PRIORITY_P2_URGENT: return "紧急不重要";
            case PRIORITY_HIGH: return "高优先级";
            case PRIORITY_MEDIUM: return "中优先级";
            case PRIORITY_LOW: return "低优先级";
            default: return "低优先级";
        }
    }

    public String getCategoryLabel() {
        switch (category != null ? category : "") {
            case "work": return "工作";
            case "personal": return "个人";
            case "study": return "学习";
            case "fitness": return "健身";
            default:
                if (customTag != null && !customTag.isEmpty()) return customTag;
                return "其他";
        }
    }

    public String getStatusLabel() {
        switch (status != null ? status : "") {
            case STATUS_TODO: return "待办";
            case STATUS_DOING: return "进行中";
            case STATUS_PLANNED: return "计划中";
            case STATUS_PAUSED: return "暂停";
            case STATUS_DONE: return "已完成";
            default: return "待办";
        }
    }
}