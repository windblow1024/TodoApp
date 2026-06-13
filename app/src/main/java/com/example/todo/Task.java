package com.example.todo;

/**
 * 待办事项数据模型
 */
public class Task {
    private int id;
    private String title;
    private boolean completed;
    private long createdAt;

    public Task(String title) {
        this.title = title;
        this.completed = false;
        this.createdAt = System.currentTimeMillis();
    }

    public Task(int id, String title, boolean completed, long createdAt) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
