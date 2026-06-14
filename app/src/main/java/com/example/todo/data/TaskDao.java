package com.example.todo.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Room 数据访问对象
 */
@Dao
public interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY created_at DESC")
    LiveData<List<Task>> getActiveTasks();

    @Query("SELECT * FROM tasks WHERE completed = 1 ORDER BY created_at DESC")
    LiveData<List<Task>> getCompletedTasks();

    @Query("SELECT * FROM tasks WHERE title LIKE :query ORDER BY created_at DESC")
    LiveData<List<Task>> searchTasks(String query);

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY created_at DESC")
    LiveData<List<Task>> getTasksByCategory(String category);

    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY created_at DESC")
    LiveData<List<Task>> getTasksByPriority(int priority);

    @Query("SELECT * FROM tasks WHERE id = :id")
    LiveData<Task> getTaskById(int id);

    @Query("SELECT * FROM tasks WHERE id = :id")
    Task getTaskSync(int id);

    // 排序查询
    @Query("SELECT * FROM tasks ORDER BY priority DESC, created_at DESC")
    LiveData<List<Task>> getAllTasksSortedByPriority();

    @Query("SELECT * FROM tasks ORDER BY start_date ASC")
    LiveData<List<Task>> getAllTasksSortedByStartDate();

    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY start_date ASC")
    LiveData<List<Task>> getActiveTasksSortedByStartDate();

    @Query("SELECT * FROM tasks ORDER BY due_date ASC")
    LiveData<List<Task>> getAllTasksSortedByDueDate();

    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY due_date ASC")
    LiveData<List<Task>> getActiveTasksSortedByDueDate();

    // 统计查询
    @Query("SELECT COUNT(*) FROM tasks")
    LiveData<Integer> getTotalCount();

    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 0")
    LiveData<Integer> getActiveCount();

    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 1")
    LiveData<Integer> getCompletedCount();

    @Query("SELECT COUNT(*) FROM tasks WHERE priority = :priority")
    LiveData<Integer> getCountByPriority(int priority);

    @Query("SELECT COUNT(*) FROM tasks WHERE category = :category")
    LiveData<Integer> getCountByCategory(String category);

    // 日历视图查询
    @Query("SELECT * FROM tasks WHERE due_date >= :startOfDay AND due_date < :endOfDay ORDER BY due_date ASC")
    LiveData<List<Task>> getTasksForDate(long startOfDay, long endOfDay);

    // CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("DELETE FROM tasks WHERE id = :id")
    void deleteById(int id);

    @Query("UPDATE tasks SET completed = :completed WHERE id = :id")
    void updateCompletion(int id, boolean completed);
}