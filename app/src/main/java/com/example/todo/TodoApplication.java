package com.example.todo;

import android.app.Application;

/**
 * Application 类，提供全局数据库实例和应用主题
 */
public class TodoApplication extends Application {

    private static TodoApplication instance;
    private DatabaseHelper databaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        databaseHelper = new DatabaseHelper(this);
    }

    public static synchronized TodoApplication getInstance() {
        return instance;
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }
}