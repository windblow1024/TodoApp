package com.example.todo.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.todo.data.Task;

import java.util.TimeZone;

/**
 * 系统日历同步帮助类
 * v3.9: 增加详细错误反馈，支持 MIUI
 */
public class CalendarHelper {

    private static final String TAG = "CalendarHelper";
    private static final String CALENDAR_ACCOUNT = "TodoApp";
    private static final String CALENDAR_NAME = "待办事项";

    private static long cachedCalendarId = -1;

    /**
     * 同步结果封装
     */
    public static class SyncResult {
        public final boolean success;
        public final String message;
        public final long eventId;

        public SyncResult(boolean success, String message, long eventId) {
            this.success = success;
            this.message = message;
            this.eventId = eventId;
        }

        public static SyncResult ok(long eventId) {
            return new SyncResult(true, "同步成功", eventId);
        }

        public static SyncResult fail(String message) {
            return new SyncResult(false, message, 0);
        }
    }

    /**
     * 检查日历权限是否已授予（兼容 MIUI）
     */
    public static boolean hasCalendarPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_CALENDAR)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 获取或创建日历，优先使用已有日历（兼容 MIUI）
     */
    public static long getOrCreateCalendar(Context context) {
        if (cachedCalendarId > 0) return cachedCalendarId;

        ContentResolver cr = context.getContentResolver();

        // 1. 先查已有的 TodoApp 日历
        String[] projection = {CalendarContract.Calendars._ID};
        String selection = CalendarContract.Calendars.ACCOUNT_NAME + " = ?";
        String[] selArgs = new String[]{CALENDAR_ACCOUNT};

        try (Cursor cursor = cr.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection, selection, selArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                cachedCalendarId = cursor.getLong(0);
                Log.d(TAG, "Found existing calendar: " + cachedCalendarId);
                return cachedCalendarId;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying calendar", e);
        }

        // 2. 查询系统已有的可写日历（MIUI 兼容）
        String[] calProjection = {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE
        };
        try (Cursor cursor = cr.query(
                CalendarContract.Calendars.CONTENT_URI,
                calProjection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long calId = cursor.getLong(0);
                    String accountName = cursor.getString(1);
                    String accountType = cursor.getString(2);
                    // 优先用本地日历或小米日历
                    if (accountType != null && (accountType.contains("local") || accountType.contains("miui"))) {
                        cachedCalendarId = calId;
                        Log.d(TAG, "Using system calendar: " + accountName + " (" + accountType + ")");
                        return cachedCalendarId;
                    }
                } while (cursor.moveToNext());
                // 如果没找到 local/miui，用第一个可用日历
                cursor.moveToFirst();
                cachedCalendarId = cursor.getLong(0);
                Log.d(TAG, "Using first available calendar: " + cachedCalendarId);
                return cachedCalendarId;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying system calendars", e);
        }

        // 3. 创建新日历（最后手段）
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDAR_ACCOUNT);
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(CalendarContract.Calendars.NAME, CALENDAR_NAME);
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDAR_NAME);
        values.put(CalendarContract.Calendars.CALENDAR_COLOR, 0xFF007AFF);
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDAR_ACCOUNT);
        values.put(CalendarContract.Calendars.VISIBLE, 1);
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);

        try {
            Uri uri = cr.insert(
                    CalendarContract.Calendars.CONTENT_URI.buildUpon()
                            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDAR_ACCOUNT)
                            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                            .build(),
                    values);
            if (uri != null) {
                cachedCalendarId = ContentUris.parseId(uri);
                Log.d(TAG, "Created new calendar: " + cachedCalendarId);
                return cachedCalendarId;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating calendar", e);
        }
        return -1;
    }

    /**
     * 创建日历事件，返回详细结果
     */
    public static SyncResult createEvent(Context context, String title, long dueDateMillis) {
        try {
            // 权限检查
            if (!hasCalendarPermission(context)) {
                return SyncResult.fail("日历权限未授予，请在系统设置中开启「日历」权限");
            }

            long calId = getOrCreateCalendar(context);
            if (calId < 0) {
                return SyncResult.fail("无法获取或创建日历，请检查系统日历是否存在");
            }

            ContentResolver cr = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, dueDateMillis);
            values.put(CalendarContract.Events.DTEND, dueDateMillis + 3600000);
            values.put(CalendarContract.Events.TITLE, title);
            values.put(CalendarContract.Events.DESCRIPTION, "来自待办事项 App");
            values.put(CalendarContract.Events.CALENDAR_ID, calId);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
            values.put(CalendarContract.Events.ALL_DAY, 0);
            values.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED);

            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if (uri == null) {
                return SyncResult.fail("写入日历事件失败，请检查日历权限（MIUI 需在设置中单独开启）");
            }

            long eventId = ContentUris.parseId(uri);

            // 添加提醒
            try {
                ContentValues reminderValues = new ContentValues();
                reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId);
                reminderValues.put(CalendarContract.Reminders.MINUTES, 15);
                reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);
            } catch (Exception e) {
                Log.w(TAG, "Failed to add reminder", e);
            }

            Log.d(TAG, "Created event: " + eventId + " for: " + title);
            return SyncResult.ok(eventId);
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception", e);
            return SyncResult.fail("日历权限不足，请在「设置→应用→待办事项→权限」中开启日历权限");
        } catch (Exception e) {
            Log.e(TAG, "Error creating event", e);
            return SyncResult.fail("同步日历失败: " + e.getMessage());
        }
    }

    public static boolean updateEvent(Context context, long eventId, String title, long dueDateMillis) {
        if (eventId <= 0) return false;
        try {
            ContentResolver cr = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, dueDateMillis);
            values.put(CalendarContract.Events.DTEND, dueDateMillis + 3600000);
            values.put(CalendarContract.Events.TITLE, title);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

            Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
            int rows = cr.update(uri, values, null, null);
            return rows > 0;
        } catch (SecurityException e) {
            Log.e(TAG, "No calendar permission", e);
            return false;
        }
    }

    public static boolean deleteEvent(Context context, long eventId) {
        if (eventId <= 0) return false;
        try {
            ContentResolver cr = context.getContentResolver();
            Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
            int rows = cr.delete(uri, null, null);
            return rows > 0;
        } catch (SecurityException e) {
            Log.e(TAG, "No calendar permission", e);
            return false;
        }
    }

    /**
     * 同步任务到日历，返回详细结果
     */
    public static SyncResult syncTaskToCalendar(Context context, Task task) {
        if (!task.hasDueDate()) {
            return SyncResult.fail("没有设置截止日期，无法同步到日历");
        }

        if (task.hasCalendarEvent()) {
            boolean updated = updateEvent(context, task.getCalendarEventId(), task.getTitle(), task.getDueDate());
            if (updated) {
                return SyncResult.ok(task.getCalendarEventId());
            } else {
                return SyncResult.fail("更新日历事件失败");
            }
        } else {
            return createEvent(context, task.getTitle(), task.getDueDate());
        }
    }

    public static void removeTaskReminder(Context context, Task task) {
        if (task.hasCalendarEvent()) {
            deleteEvent(context, task.getCalendarEventId());
        }
    }
}