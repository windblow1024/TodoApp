package com.example.todo;

import android.app.Application;

import com.example.todo.data.AppDatabase;

/**
 * Application 类
 */
public class TodoApplication extends Application {

    private static TodoApplication instance;
    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = AppDatabase.getInstance(this);
    }

    public static synchronized TodoApplication getInstance() {
        return instance;
    }

    public AppDatabase getDatabase() {
        return database;
    }
}