package com.example.todo;

import android.app.Activity;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * 主题工具类
 * 管理深色模式和主题色
 */
public class ThemeUtil {

    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_THEME_COLOR = "theme_color";

    // 主题色常量
    public static final int THEME_BLUE = 0;
    public static final int THEME_GREEN = 1;
    public static final int THEME_PURPLE = 2;
    public static final int THEME_ORANGE = 3;

    public static final String[] THEME_COLORS = {"#007AFF", "#34C759", "#AF52DE", "#FF9500"};
    public static final String[] THEME_NAMES = {"蓝色", "绿色", "紫色", "橙色"};

    /**
     * 应用主题设置（在 setContentView 之前调用）
     */
    private static SharedPreferences getPrefs(Activity activity) {
        return activity.getSharedPreferences("todo_prefs", Activity.MODE_PRIVATE);
    }

    public static void applyTheme(Activity activity) {
        SharedPreferences prefs = getPrefs(activity);

        // 深色模式
        boolean darkMode = prefs.getBoolean(KEY_DARK_MODE, false);
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static boolean isDarkMode(Activity activity) {
        SharedPreferences prefs = getPrefs(activity);
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public static void setDarkMode(Activity activity, boolean dark) {
        SharedPreferences prefs = getPrefs(activity);
        prefs.edit().putBoolean(KEY_DARK_MODE, dark).apply();
    }

    public static int getThemeColor(Activity activity) {
        SharedPreferences prefs = getPrefs(activity);
        return prefs.getInt(KEY_THEME_COLOR, THEME_BLUE);
    }

    public static void setThemeColor(Activity activity, int colorIndex) {
        SharedPreferences prefs = getPrefs(activity);
        prefs.edit().putInt(KEY_THEME_COLOR, colorIndex).apply();
    }
}