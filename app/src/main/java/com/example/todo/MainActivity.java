package com.example.todo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Task> taskList;
    private TextView tabAll, tabActive, tabCompleted;
    private View indicatorAll, indicatorActive, indicatorCompleted;
    private TextView emptyText;
    private View emptyView;
    private EditText searchInput;
    private ProgressBar statsBar;
    private TextView statsText;
    private View statsContainer;
    private View searchClearBtn;
    private FloatingActionButton fab;

    private int currentTab = 0; // 0=全部, 1=待办, 2=已完成
    private String currentSort = "created";
    private String searchQuery = "";
    private boolean searchMode = false;
    private Task pendingUndoTask = null;
    private int pendingUndoPosition = -1;
    private Handler handler = new Handler();

    private final ActivityResultLauncher<String> requestCalendarPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtil.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = TodoApplication.getInstance().getDatabaseHelper();

        // 请求日历权限（Android 6.0+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED) {
                requestCalendarPermission.launch(Manifest.permission.WRITE_CALENDAR);
            }
        }

        initViews();
        setupTabs();
        setupSearch();
        setupSortMenu();
        setupSettingsButton();
        setupFab();
        applyThemeColor();

        // 加载动画
        recyclerView.setAnimation(createFadeInAnimation());

        loadTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从详情页返回时刷新
        loadTasks();
        updateStats();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        tabAll = findViewById(R.id.tabAll);
        tabActive = findViewById(R.id.tabActive);
        tabCompleted = findViewById(R.id.tabCompleted);
        indicatorAll = findViewById(R.id.indicatorAll);
        indicatorActive = findViewById(R.id.indicatorActive);
        indicatorCompleted = findViewById(R.id.indicatorCompleted);
        emptyText = findViewById(R.id.emptyText);
        emptyView = findViewById(R.id.emptyView);
        searchInput = findViewById(R.id.searchInput);
        searchClearBtn = findViewById(R.id.searchClearBtn);
        statsBar = findViewById(R.id.statsBar);
        statsText = findViewById(R.id.statsText);
        statsContainer = findViewById(R.id.statsContainer);
        fab = findViewById(R.id.fab);
    }

    private void setupTabs() {
        tabAll.setOnClickListener(v -> selectTab(0));
        tabActive.setOnClickListener(v -> selectTab(1));
        tabCompleted.setOnClickListener(v -> selectTab(2));
        selectTab(0);
    }

    private void selectTab(int index) {
        currentTab = index;

        tabAll.setTextColor(getColor(android.R.color.darker_gray));
        tabActive.setTextColor(getColor(android.R.color.darker_gray));
        tabCompleted.setTextColor(getColor(android.R.color.darker_gray));
        indicatorAll.setVisibility(View.INVISIBLE);
        indicatorActive.setVisibility(View.INVISIBLE);
        indicatorCompleted.setVisibility(View.INVISIBLE);

        switch (index) {
            case 0:
                tabAll.setTextColor(getColor(R.color.colorPrimary));
                indicatorAll.setVisibility(View.VISIBLE);
                break;
            case 1:
                tabActive.setTextColor(getColor(R.color.colorPrimary));
                indicatorActive.setVisibility(View.VISIBLE);
                break;
            case 2:
                tabCompleted.setTextColor(getColor(R.color.colorPrimary));
                indicatorCompleted.setVisibility(View.VISIBLE);
                break;
        }

        loadTasks();
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                searchClearBtn.setVisibility(searchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                loadTasks();
            }
        });

        searchClearBtn.setOnClickListener(v -> {
            searchInput.setText("");
        });
    }

    private void setupSortMenu() {
        findViewById(R.id.sortButton).setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add(0, 1, 0, "按创建时间");
            popup.getMenu().add(0, 2, 0, "按截止日期");
            popup.getMenu().add(0, 3, 0, "按优先级");
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1: currentSort = "created"; break;
                    case 2: currentSort = "due"; break;
                    case 3: currentSort = "priority"; break;
                }
                loadTasks();
                return true;
            });
            popup.show();
        });
    }

    private void setupSettingsButton() {
        findViewById(R.id.settingsButton).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    private void setupFab() {
        fab.setOnClickListener(v -> showAddDialog());
    }

    private void applyThemeColor() {
        int colorIndex = ThemeUtil.getThemeColor(this);
        String colorStr = ThemeUtil.THEME_COLORS[colorIndex];
        int color = android.graphics.Color.parseColor(colorStr);
        fab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
    }

    private Animation createFadeInAnimation() {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500);
        return anim;
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText input = dialogView.findViewById(R.id.taskInput);
        input.requestFocus();

        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            String title = input.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "请输入待办事项", Toast.LENGTH_SHORT).show();
                return;
            }

            Task task = new Task(title);
            long id = dbHelper.addTask(task);
            if (id > 0) {
                task.setId((int) id);
                loadTasks();
                updateStats();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void loadTasks() {
        String orderBy;
        switch (currentSort) {
            case "due": orderBy = "due_date ASC, " + "created_at DESC"; break;
            case "priority": orderBy = "priority DESC, " + "created_at DESC"; break;
            default: orderBy = "created_at DESC"; break;
        }

        // 搜索模式
        if (!searchQuery.isEmpty()) {
            taskList = dbHelper.searchTasks(searchQuery);
        } else {
            switch (currentTab) {
                case 0: taskList = dbHelper.getAllTasksSorted(orderBy); break;
                case 1: taskList = dbHelper.getActiveTasks(); break;
                case 2: taskList = dbHelper.getCompletedTasks(); break;
            }
        }

        updateEmptyView();
        updateStats();

        if (adapter == null) {
            adapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskClickListener() {
                @Override
                public void onToggleComplete(Task task, int position) {
                    dbHelper.updateTaskStatus(task.getId(), !task.isCompleted());

                    // 任务完成时移除日历提醒
                    if (!task.isCompleted()) {
                        task.setCompleted(true);
                        CalendarHelper.removeTaskReminder(MainActivity.this, task);
                        task.setCalendarEventId(0);
                        dbHelper.updateTask(task);
                    }

                    loadTasks();
                }

                @Override
                public void onDelete(Task task, int position) {
                    // 保存以便撤销
                    pendingUndoTask = task;
                    pendingUndoPosition = position;

                    dbHelper.deleteTask(task.getId());

                    // 如果有日历事件也删除
                    if (task.hasCalendarEvent()) {
                        CalendarHelper.deleteEvent(MainActivity.this, task.getCalendarEventId());
                    }

                    loadTasks();
                    updateStats();

                    // 显示撤销 Snackbar
                    Snackbar.make(recyclerView, "已删除", Snackbar.LENGTH_LONG)
                            .setAction("撤销", v -> {
                                if (pendingUndoTask != null) {
                                    long newId = dbHelper.addTask(pendingUndoTask);
                                    pendingUndoTask.setId((int) newId);
                                    loadTasks();
                                    updateStats();
                                    pendingUndoTask = null;
                                }
                            })
                            .show();
                }

                @Override
                public void onItemClick(Task task, int position) {
                    Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
                    intent.putExtra("task_id", task.getId());
                    startActivity(intent);
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(taskList);
        }
    }

    private void updateEmptyView() {
        if (taskList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            if (!searchQuery.isEmpty()) {
                emptyText.setText("没有找到「" + searchQuery + "」");
            } else {
                switch (currentTab) {
                    case 0: emptyText.setText(R.string.empty_all); break;
                    case 1: emptyText.setText(R.string.empty_active); break;
                    case 2: emptyText.setText(R.string.empty_completed); break;
                }
            }
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateStats() {
        int total = dbHelper.getActiveCount() + dbHelper.getCompletedCount();
        int completed = dbHelper.getCompletedCount();
        int active = dbHelper.getActiveCount();

        if (total > 0) {
            statsContainer.setVisibility(View.VISIBLE);
            int progress = (completed * 100) / total;
            statsBar.setProgress(progress);
            statsText.setText(completed + "/" + total + " (" + progress + "%)");
        } else {
            statsContainer.setVisibility(View.GONE);
        }
    }
}