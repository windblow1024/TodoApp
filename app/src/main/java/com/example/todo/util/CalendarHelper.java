package com.example.todo.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import com.example.todo.data.Task;

import java.util.TimeZone;

/**
 * 系统日历同步帮助类
 */
public class CalendarHelper {

    private static final String TAG = "CalendarHelper";
    private static final String CALENDAR_ACCOUNT = "TodoApp";
    private static final String CALENDAR_NAME = "待办事项";

    private static long cachedCalendarId = -1;

    public static long getOrCreateCalendar(Context context) {
        if (cachedCalendarId > 0) return cachedCalendarId;

        ContentResolver cr = context.getContentResolver();
        String[] projection = {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_NAME
        };
        String selection = CalendarContract.Calendars.ACCOUNT_NAME + " = ? AND "
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?";
        String[] selArgs = new String[]{CALENDAR_ACCOUNT, CalendarContract.ACCOUNT_TYPE_LOCAL};

        try (Cursor cursor = cr.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection, selection, selArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                cachedCalendarId = cursor.getLong(0);
                return cachedCalendarId;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying calendar", e);
        }

        // Create calendar
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
                return cachedCalendarId;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating calendar", e);
        }
        return -1;
    }

    public static long createEvent(Context context, String title, long dueDateMillis) {
        try {
            long calId = getOrCreateCalendar(context);
            if (calId < 0) return -1;

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
            if (uri == null) return -1;

            long eventId = ContentUris.parseId(uri);

            // Add reminder (15 min before)
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

    public static long syncTaskToCalendar(Context context, Task task) {
        if (!task.hasDueDate()) return 0;
        if (task.hasCalendarEvent()) {
            updateEvent(context, task.getCalendarEventId(), task.getTitle(), task.getDueDate());
            return task.getCalendarEventId();
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