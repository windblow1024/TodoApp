package com.example.todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 数据库帮助类
 * v2.0: 新增 priority, due_date, calendar_event_id 字段
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "todo.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_TASKS = "tasks";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_COMPLETED = "completed";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_PRIORITY = "priority";
    private static final String COL_DUE_DATE = "due_date";
    private static final String COL_CALENDAR_EVENT_ID = "calendar_event_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_TASKS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_COMPLETED + " INTEGER DEFAULT 0, "
                + COL_CREATED_AT + " INTEGER NOT NULL, "
                + COL_PRIORITY + " INTEGER DEFAULT 0, "
                + COL_DUE_DATE + " INTEGER DEFAULT 0, "
                + COL_CALENDAR_EVENT_ID + " INTEGER DEFAULT 0)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add new columns for v2.0
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COL_PRIORITY + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COL_DUE_DATE + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COL_CALENDAR_EVENT_ID + " INTEGER DEFAULT 0");
        }
    }

    // ========== CRUD ==========

    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = toContentValues(task);
        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = toContentValues(task);
        int rows = db.update(TABLE_TASKS, values, COL_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        return rows;
    }

    public int updateTaskStatus(int id, boolean completed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COMPLETED, completed ? 1 : 0);
        int rows = db.update(TABLE_TASKS, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public int deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_TASKS, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public Task getTask(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Task task = null;
        if (cursor != null && cursor.moveToFirst()) {
            task = cursorToTask(cursor);
            cursor.close();
        }
        db.close();
        return task;
    }

    // ========== 查询 ==========

    public List<Task> getAllTasks() {
        return queryTasks(null, null, COL_CREATED_AT + " DESC");
    }

    public List<Task> getActiveTasks() {
        return queryTasks(COL_COMPLETED + " = ?", new String[]{"0"}, COL_CREATED_AT + " DESC");
    }

    public List<Task> getCompletedTasks() {
        return queryTasks(COL_COMPLETED + " = ?", new String[]{"1"}, COL_CREATED_AT + " DESC");
    }

    public List<Task> searchTasks(String query) {
        return queryTasks(COL_TITLE + " LIKE ?", new String[]{"%" + query + "%"}, COL_CREATED_AT + " DESC");
    }

    public List<Task> getAllTasksSorted(String orderBy) {
        return queryTasks(null, null, orderBy);
    }

    public int getActiveCount() {
        return getCount(COL_COMPLETED + " = ?", new String[]{"0"});
    }

    public int getCompletedCount() {
        return getCount(COL_COMPLETED + " = ?", new String[]{"1"});
    }

    // ========== 批量操作 ==========

    public int deleteAllCompleted() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_TASKS, COL_COMPLETED + " = ?", new String[]{"1"});
        db.close();
        return rows;
    }

    // ========== 数据导入导出 ==========

    public List<Task> getAllTasksForExport() {
        return queryTasks(null, null, COL_ID + " ASC");
    }

    // ========== 内部方法 ==========

    private ContentValues toContentValues(Task task) {
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, task.getTitle());
        values.put(COL_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(COL_CREATED_AT, task.getCreatedAt());
        values.put(COL_PRIORITY, task.getPriority());
        values.put(COL_DUE_DATE, task.getDueDate());
        values.put(COL_CALENDAR_EVENT_ID, task.getCalendarEventId());
        return values;
    }

    private List<Task> queryTasks(String selection, String[] selectionArgs, String orderBy) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null, orderBy);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                taskList.add(cursorToTask(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return taskList;
    }

    private Task cursorToTask(Cursor cursor) {
        Task task = new Task();
        task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)));
        task.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMPLETED)) == 1);
        task.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));
        task.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRIORITY)));
        task.setDueDate(cursor.getLong(cursor.getColumnIndexOrThrow(COL_DUE_DATE)));
        task.setCalendarEventId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_CALENDAR_EVENT_ID)));
        return task;
    }

    private int getCount(String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS
                + (selection != null ? " WHERE " + selection : ""), selectionArgs);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }
}