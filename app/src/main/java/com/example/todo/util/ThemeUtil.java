package com.example.todo.util;

import android.app.Activity;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * 主题工具类
 */
public class ThemeUtil {

    private static final String PREFS_NAME = "todo_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_THEME_COLOR = "theme_color";
    private static final String KEY_PRIORITY_MODE = "priority_mode";

    public static final int PRIORITY_MODE_EISENHOWER = 0;
    public static final int PRIORITY_MODE_SIMPLE = 1;

    public static final int THEME_BLUE = 0;
    public static final int THEME_GREEN = 1;
    public static final int THEME_PURPLE = 2;
    public static final int THEME_ORANGE = 3;

    private static SharedPreferences getPrefs(Activity activity) {
        return activity.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
    }

    public static void applyTheme(Activity activity) {
        SharedPreferences prefs = getPrefs(activity);
        boolean darkMode = prefs.getBoolean(KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static boolean isDarkMode(Activity activity) {
        return getPrefs(activity).getBoolean(KEY_DARK_MODE, false);
    }

    public static void setDarkMode(Activity activity, boolean dark) {
        getPrefs(activity).edit().putBoolean(KEY_DARK_MODE, dark).apply();
    }

    public static int getThemeColor(Activity activity) {
        return getPrefs(activity).getInt(KEY_THEME_COLOR, THEME_BLUE);
    }

    public static void setThemeColor(Activity activity, int colorIndex) {
        getPrefs(activity).edit().putInt(KEY_THEME_COLOR, colorIndex).apply();
    }

    public static int getPriorityMode(Activity activity) {
        return getPrefs(activity).getInt(KEY_PRIORITY_MODE, PRIORITY_MODE_EISENHOWER);
    }

    public static void setPriorityMode(Activity activity, int mode) {
        getPrefs(activity).edit().putInt(KEY_PRIORITY_MODE, mode).apply();
    }

    public static int parseColor(int colorIndex) {
        switch (colorIndex) {
            case THEME_GREEN: return 0xFF34C759;
            case THEME_PURPLE: return 0xFFAF52DE;
            case THEME_ORANGE: return 0xFFFF9500;
            default: return 0xFF007AFF;
        }
    }
}