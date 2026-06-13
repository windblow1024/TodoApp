package com.example.todo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todo.data.AppDatabase;
import com.example.todo.data.Task;
import com.example.todo.util.CalendarHelper;
import com.example.todo.util.ThemeUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    private EditText titleInput, descInput, customTagInput;
    private RadioGroup priorityGroup, statusGroup;
    private TextView dueDateText;
    private CheckBox syncCalendarCheck;
    private Spinner categorySpinner, repeatSpinner;
    private View dueDateContainer, repeatDaysContainer;
    private LinearLayout repeatDaysLayout;
    private ImageView attachmentPreview;
    private Button saveButton, deleteButton, attachButton;
    private View attachmentContainer;

    private Task task;
    private boolean isNewTask = true;
    private long selectedDueDate = 0;
    private boolean syncToCalendar = false;
    private String repeatType = Task.REPEAT_NONE;
    private String attachmentPath = "";
    private String attachmentType = "";

    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    handleAttachment(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            ThemeUtil.applyTheme(this);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_task_detail);

            int taskId = getIntent().getIntExtra("task_id", -1);
            isNewTask = taskId <= 0;

            initViews();
            setupCategorySpinner();
            setupRepeatSpinner();

            if (isNewTask) {
                task = new Task();
            } else {
                AppDatabase db = TodoApplication.getInstance().getDatabase();
                task = db.taskDao().getTaskSync(taskId);
                if (task == null) {
                    Toast.makeText(this, "任务不存在 (ID=" + taskId + ")", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                loadTaskData();
            }

            setupListeners();
        } catch (Exception e) {
            Toast.makeText(this, "打开详情失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        titleInput = findViewById(R.id.titleInput);
        descInput = findViewById(R.id.descInput);
        customTagInput = findViewById(R.id.customTagInput);
        priorityGroup = findViewById(R.id.priorityGroup);
        statusGroup = findViewById(R.id.statusGroup);
        dueDateText = findViewById(R.id.dueDateText);
        dueDateContainer = findViewById(R.id.dueDateContainer);
        syncCalendarCheck = findViewById(R.id.syncCalendarCheck);
        categorySpinner = findViewById(R.id.categorySpinner);
        repeatSpinner = findViewById(R.id.repeatSpinner);
        repeatDaysContainer = findViewById(R.id.repeatDaysContainer);
        repeatDaysLayout = findViewById(R.id.repeatDaysLayout);
        attachmentPreview = findViewById(R.id.attachmentPreview);
        attachmentContainer = findViewById(R.id.attachmentContainer);
        attachButton = findViewById(R.id.attachButton);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupCategorySpinner() {
        String[] categories = {"工作", "个人", "学习", "健身", "其他", "自定义"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner.setAdapter(adapter);
    }

    private void setupRepeatSpinner() {
        String[] repeats = {"不重复", "每天", "每周", "每月", "自定义"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, repeats);
        repeatSpinner.setAdapter(adapter);
    }

    private void loadTaskData() {
        titleInput.setText(task.getTitle());
        descInput.setText(task.getDescription());

        // Priority
        switch (task.getPriority()) {
            case Task.PRIORITY_P0_CRITICAL: priorityGroup.check(R.id.radioP0); break;
            case Task.PRIORITY_P1_IMPORTANT: priorityGroup.check(R.id.radioP1); break;
            case Task.PRIORITY_P2_URGENT: priorityGroup.check(R.id.radioP2); break;
            default: priorityGroup.check(R.id.radioP3); break;
        }

        // Status
        switch (task.getStatus() != null ? task.getStatus() : "") {
            case Task.STATUS_DOING: statusGroup.check(R.id.statusDoing); break;
            case Task.STATUS_PLANNED: statusGroup.check(R.id.statusPlanned); break;
            case Task.STATUS_BLOCKED: statusGroup.check(R.id.statusBlocked); break;
            case Task.STATUS_DONE: statusGroup.check(R.id.statusDone); break;
            default: statusGroup.check(R.id.statusTodo); break;
        }

        // Category
        String[] catValues = {"work", "personal", "study", "fitness", "other", "custom"};
        for (int i = 0; i < catValues.length; i++) {
            if (catValues[i].equals(task.getCategory())) {
                categorySpinner.setSelection(i);
                break;
            }
        }

        // Custom tag
        if ("custom".equals(task.getCategory()) && task.getCustomTag() != null) {
            customTagInput.setText(task.getCustomTag());
            customTagInput.setVisibility(View.VISIBLE);
        }

        // Due date
        if (task.hasDueDate()) {
            selectedDueDate = task.getDueDate();
            dueDateText.setText(dateTimeFormat.format(selectedDueDate));
        }

        // Repeat
        String[] repeatValues = {Task.REPEAT_NONE, Task.REPEAT_DAILY, Task.REPEAT_WEEKLY, Task.REPEAT_MONTHLY, Task.REPEAT_CUSTOM};
        for (int i = 0; i < repeatValues.length; i++) {
            if (repeatValues[i].equals(task.getRepeatType())) {
                repeatSpinner.setSelection(i);
                break;
            }
        }

        syncToCalendar = task.hasCalendarEvent();
        syncCalendarCheck.setChecked(syncToCalendar);

        // Attachment
        if (task.hasAttachment()) {
            attachmentPath = task.getAttachmentPath();
            attachmentType = task.getAttachmentType();
            showAttachmentPreview();
        }

        deleteButton.setVisibility(View.VISIBLE);
    }

    private void setupListeners() {
        dueDateContainer.setOnClickListener(v -> showDateTimePicker());

        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                customTagInput.setVisibility(position == 5 ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        repeatSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                repeatDaysContainer.setVisibility(position == 4 ? View.VISIBLE : View.GONE);
                String[] repeatValues = {Task.REPEAT_NONE, Task.REPEAT_DAILY, Task.REPEAT_WEEKLY, Task.REPEAT_MONTHLY, Task.REPEAT_CUSTOM};
                repeatType = repeatValues[position];
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        attachButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        saveButton.setOnClickListener(v -> saveTask());
        deleteButton.setOnClickListener(v -> deleteTask());
    }

    private void showDateTimePicker() {
        Calendar cal = Calendar.getInstance();
        if (selectedDueDate > 0) cal.setTimeInMillis(selectedDueDate);

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.SECOND, 0);
                selectedDueDate = cal.getTimeInMillis();
                dueDateText.setText(dateTimeFormat.format(selectedDueDate));
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void handleAttachment(Uri uri) {
        try {
            String fileName = "attachment_" + System.currentTimeMillis() + ".jpg";
            File outputDir = new File(getCacheDir(), "attachments");
            outputDir.mkdirs();
            File outputFile = new File(outputDir, fileName);

            try (InputStream is = getContentResolver().openInputStream(uri);
                 FileOutputStream os = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            attachmentPath = outputFile.getAbsolutePath();
            attachmentType = "image";
            showAttachmentPreview();
        } catch (Exception e) {
            Toast.makeText(this, "附件添加失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAttachmentPreview() {
        if ("image".equals(attachmentType)) {
            attachmentPreview.setImageURI(Uri.fromFile(new File(attachmentPath)));
            attachmentContainer.setVisibility(View.VISIBLE);
        }
    }

    private void saveTask() {
        String title = titleInput.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
            return;
        }

        task.setTitle(title);
        task.setDescription(descInput.getText().toString().trim());

        // Priority
        int checkedPriority = priorityGroup.getCheckedRadioButtonId();
        if (checkedPriority == R.id.radioP0) task.setPriority(Task.PRIORITY_P0_CRITICAL);
        else if (checkedPriority == R.id.radioP1) task.setPriority(Task.PRIORITY_P1_IMPORTANT);
        else if (checkedPriority == R.id.radioP2) task.setPriority(Task.PRIORITY_P2_URGENT);
        else task.setPriority(Task.PRIORITY_P3_LOW);

        // Status
        int checkedStatus = statusGroup.getCheckedRadioButtonId();
        if (checkedStatus == R.id.statusDoing) task.setStatus(Task.STATUS_DOING);
        else if (checkedStatus == R.id.statusPlanned) task.setStatus(Task.STATUS_PLANNED);
        else if (checkedStatus == R.id.statusBlocked) task.setStatus(Task.STATUS_BLOCKED);
        else if (checkedStatus == R.id.statusDone) { task.setStatus(Task.STATUS_DONE); task.setCompleted(true); }
        else task.setStatus(Task.STATUS_TODO);

        // Category
        int catPos = categorySpinner.getSelectedItemPosition();
        String[] catValues = {"work", "personal", "study", "fitness", "other", "custom"};
        task.setCategory(catValues[catPos]);
        task.setCustomTag(catPos == 5 ? customTagInput.getText().toString().trim() : "");

        // Due date
        task.setDueDate(selectedDueDate);

        // Repeat
        task.setRepeatType(repeatType);

        // Attachment
        task.setAttachmentPath(attachmentPath);
        task.setAttachmentType(attachmentType);

        // Calendar sync
        if (syncToCalendar && selectedDueDate > 0) {
            long eventId = CalendarHelper.syncTaskToCalendar(this, task);
            if (eventId > 0) task.setCalendarEventId(eventId);
        } else if (!syncToCalendar && task.hasCalendarEvent()) {
            CalendarHelper.deleteEvent(this, task.getCalendarEventId());
            task.setCalendarEventId(0);
        }

        AppDatabase db = TodoApplication.getInstance().getDatabase();
        new Thread(() -> {
            if (isNewTask) {
                db.taskDao().insert(task);
            } else {
                db.taskDao().update(task);
            }
        }).start();

        Toast.makeText(this, isNewTask ? "已创建" : "已保存", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void deleteTask() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("确定删除「" + task.getTitle() + "」？")
                .setPositiveButton("删除", (dialog, which) -> {
                    AppDatabase db = TodoApplication.getInstance().getDatabase();
                    new Thread(() -> {
                        if (task.hasCalendarEvent()) {
                            CalendarHelper.deleteEvent(this, task.getCalendarEventId());
                        }
                        db.taskDao().delete(task);
                    }).start();
                    Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}