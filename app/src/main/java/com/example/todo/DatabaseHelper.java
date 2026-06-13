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
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "todo.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_TASKS = "tasks";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_COMPLETED = "completed";
    private static final String COL_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_TASKS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_COMPLETED + " INTEGER DEFAULT 0, "
                + COL_CREATED_AT + " INTEGER NOT NULL)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    // 添加任务
    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, task.getTitle());
        values.put(COL_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(COL_CREATED_AT, task.getCreatedAt());
        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    // 获取所有任务
    public List<Task> getAllTasks() {
        return getTasks(null, null);
    }

    // 获取未完成任务
    public List<Task> getActiveTasks() {
        return getTasks(COL_COMPLETED + " = ?", new String[]{"0"});
    }

    // 获取已完成任务
    public List<Task> getCompletedTasks() {
        return getTasks(COL_COMPLETED + " = ?", new String[]{"1"});
    }

    private List<Task> getTasks(String selection, String[] selectionArgs) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_TASKS, null, selection, selectionArgs,
                null, null, COL_CREATED_AT + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMPLETED)) == 1,
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
                );
                taskList.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return taskList;
    }

    // 更新任务完成状态
    public int updateTaskStatus(int id, boolean completed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COMPLETED, completed ? 1 : 0);
        int rows = db.update(TABLE_TASKS, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    // 删除任务
    public int deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_TASKS, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    // 获取任务总数
    public int getTaskCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }
}
