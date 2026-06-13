package com.example.todo;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.TimeZone;

/**
 * 系统日历同步帮助类
 * 用于在系统日历中创建事件并设置提醒
 */
public class CalendarHelper {

    private static final String TAG = "CalendarHelper";
    private static final String CALENDAR_ACCOUNT = "TodoApp";
    private static final String CALENDAR_NAME = "待办事项";

    /**
     * 获取或创建 TodoApp 专用的日历
     */
    public static long getOrCreateCalendar(Context context) {
        ContentResolver cr = context.getContentResolver();

        // 查询是否已有 TodoApp 日历
        String[] projection = {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_NAME
        };
        String selection = CalendarContract.Calendars.ACCOUNT_NAME + " = ? AND "
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?";
        String[] selArgs = new String[]{CALENDAR_ACCOUNT, CalendarContract.ACCOUNT_TYPE_LOCAL};

        Cursor cursor = cr.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection, selection, selArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            long calId = cursor.getLong(0);
            cursor.close();
            return calId;
        }
        if (cursor != null) cursor.close();

        // 创建新的本地日历
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

        Uri uri = cr.insert(CalendarContract.Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDAR_ACCOUNT)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                .build(), values);

        if (uri != null) {
            return ContentUris.parseId(uri);
        }
        return -1;
    }

    /**
     * 创建日历事件（待办事项）
     * @return 事件ID
     */
    public static long createEvent(Context context, String title, long dueDateMillis) {
        try {
            long calId = getOrCreateCalendar(context);
            if (calId < 0) return -1;

            ContentResolver cr = context.getContentResolver();
            ContentValues values = new ContentValues();

            values.put(CalendarContract.Events.DTSTART, dueDateMillis);
            values.put(CalendarContract.Events.DTEND, dueDateMillis + 3600000); // 1 hour
            values.put(CalendarContract.Events.TITLE, title);
            values.put(CalendarContract.Events.DESCRIPTION, "来自待办事项 App");
            values.put(CalendarContract.Events.CALENDAR_ID, calId);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
            values.put(CalendarContract.Events.ALL_DAY, 0);
            values.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED);

            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if (uri == null) return -1;

            long eventId = ContentUris.parseId(uri);

            // 添加提醒（默认提前15分钟）
            ContentValues reminderValues = new ContentValues();
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId);
            reminderValues.put(CalendarContract.Reminders.MINUTES, 15);
            reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);

            Log.d(TAG, "Created event: " + eventId + " for: " + title);
            return eventId;
        } catch (SecurityException e) {
            Log.e(TAG, "No calendar permission", e);
            return -1;
        }
    }

    /**
     * 更新日历事件
     */
    public static boolean updateEvent(Context context, long eventId, String title, long dueDateMillis) {
        try {
            if (eventId <= 0) return false;

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

    /**
     * 删除日历事件
     */
    public static boolean deleteEvent(Context context, long eventId) {
        try {
            if (eventId <= 0) return false;

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
     * 将任务同步到日历（创建或更新）
     */
    public static long syncTaskToCalendar(Context context, Task task) {
        if (!task.hasDueDate()) return 0;

        if (task.hasCalendarEvent()) {
            // 更新已有事件
            updateEvent(context, task.getCalendarEventId(), task.getTitle(), task.getDueDate());
            return task.getCalendarEventId();
        } else {
            // 创建新事件
            return createEvent(context, task.getTitle(), task.getDueDate());
        }
    }

    /**
     * 任务完成时移除日历提醒
     */
    public static void removeTaskReminder(Context context, Task task) {
        if (task.hasCalendarEvent()) {
            deleteEvent(context, task.getCalendarEventId());
        }
    }
}