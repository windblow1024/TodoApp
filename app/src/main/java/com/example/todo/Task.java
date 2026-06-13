package com.example.todo;

/**
 * 待办事项数据模型
 * v2.0: 新增 priority, dueDate, calendarEventId
 */
public class Task {
    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_MEDIUM = 1;
    public static final int PRIORITY_HIGH = 2;

    private int id;
    private String title;
    private boolean completed;
    private long createdAt;
    private int priority;
    private long dueDate;
    private long calendarEventId;

    public Task() {}

    public Task(String title) {
        this.title = title;
        this.completed = false;
        this.createdAt = System.currentTimeMillis();
        this.priority = PRIORITY_LOW;
        this.dueDate = 0;
        this.calendarEventId = 0;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }

    public long getCalendarEventId() { return calendarEventId; }
    public void setCalendarEventId(long calendarEventId) { this.calendarEventId = calendarEventId; }

    public boolean hasDueDate() { return dueDate > 0; }
    public boolean hasCalendarEvent() { return calendarEventId > 0; }
}