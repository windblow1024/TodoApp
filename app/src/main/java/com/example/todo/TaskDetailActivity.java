package com.example.todo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * 任务详情/编辑界面
 */
public class TaskDetailActivity extends AppCompatActivity {

    private EditText titleInput;
    private RadioGroup priorityGroup;
    private TextView dueDateText;
    private CheckBox syncCalendarCheck;
    private Button saveButton, deleteButton;
    private View dueDateContainer;

    private DatabaseHelper dbHelper;
    private Task task;
    private long selectedDueDate = 0;
    private boolean syncToCalendar = false;

    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 应用主题色和深色模式
        ThemeUtil.applyTheme(this);
        setContentView(R.layout.activity_task_detail);

        dbHelper = TodoApplication.getInstance().getDatabaseHelper();

        int taskId = getIntent().getIntExtra("task_id", -1);
        if (taskId <= 0) {
            finish();
            return;
        }

        task = dbHelper.getTask(taskId);
        if (task == null) {
            finish();
            return;
        }

        initViews();
        loadTaskData();
    }

    private void initViews() {
        titleInput = findViewById(R.id.titleInput);
        priorityGroup = findViewById(R.id.priorityGroup);
        dueDateText = findViewById(R.id.dueDateText);
        dueDateContainer = findViewById(R.id.dueDateContainer);
        syncCalendarCheck = findViewById(R.id.syncCalendarCheck);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        dueDateContainer.setOnClickListener(v -> showDateTimePicker());

        saveButton.setOnClickListener(v -> saveTask());
        deleteButton.setOnClickListener(v -> deleteTask());

        syncCalendarCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            syncToCalendar = isChecked;
        });
    }

    private void loadTaskData() {
        titleInput.setText(task.getTitle());

        // 优先级
        switch (task.getPriority()) {
            case Task.PRIORITY_HIGH: priorityGroup.check(R.id.radioHigh); break;
            case Task.PRIORITY_MEDIUM: priorityGroup.check(R.id.radioMedium); break;
            default: priorityGroup.check(R.id.radioLow); break;
        }

        // 截止日期
        if (task.hasDueDate()) {
            selectedDueDate = task.getDueDate();
            dueDateText.setText(dateTimeFormat.format(selectedDueDate));
        } else {
            dueDateText.setText("无");
        }

        // 日历同步
        syncToCalendar = task.hasCalendarEvent();
        syncCalendarCheck.setChecked(syncToCalendar);
    }

    private void showDateTimePicker() {
        Calendar cal = Calendar.getInstance();
        if (selectedDueDate > 0) {
            cal.setTimeInMillis(selectedDueDate);
        }

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                selectedDueDate = cal.getTimeInMillis();
                dueDateText.setText(dateTimeFormat.format(selectedDueDate));
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();

        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTask() {
        String title = titleInput.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
            return;
        }

        task.setTitle(title);

        int checkedId = priorityGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radioHigh) task.setPriority(Task.PRIORITY_HIGH);
        else if (checkedId == R.id.radioMedium) task.setPriority(Task.PRIORITY_MEDIUM);
        else task.setPriority(Task.PRIORITY_LOW);

        task.setDueDate(selectedDueDate);

        // 处理日历同步
        if (syncToCalendar && selectedDueDate > 0) {
            long eventId = CalendarHelper.syncTaskToCalendar(this, task);
            if (eventId > 0) {
                task.setCalendarEventId(eventId);
            }
        } else if (!syncToCalendar && task.hasCalendarEvent()) {
            CalendarHelper.deleteEvent(this, task.getCalendarEventId());
            task.setCalendarEventId(0);
        } else if (syncToCalendar && !task.hasCalendarEvent()) {
            // 无截止日期但有同步开关 → 忽略
        }

        dbHelper.updateTask(task);
        Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void deleteTask() {
        if (task.hasCalendarEvent()) {
            CalendarHelper.deleteEvent(this, task.getCalendarEventId());
        }
        dbHelper.deleteTask(task.getId());
        Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
        finish();
    }
}