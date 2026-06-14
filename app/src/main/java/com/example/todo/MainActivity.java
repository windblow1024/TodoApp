package com.example.todo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.todo.data.AppDatabase;
import com.example.todo.data.TaskDao;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.data.Task;
import com.example.todo.ui.TaskAdapter;
import com.example.todo.util.ThemeUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    private TextView tabAll, tabActive, tabCompleted;
    private View indicatorAll, indicatorActive, indicatorCompleted;
    private TextView emptyText;
    private View emptyView;
    private EditText searchInput;
    private View searchClearBtn;
    private FloatingActionButton fab;

    private int currentTab = 0;
    private String searchQuery = "";
    private String currentSort = "created";
    private Task pendingUndoTask = null;

    private final ActivityResultLauncher<String> requestCalendarPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtil.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED) {
                requestCalendarPermission.launch(Manifest.permission.WRITE_CALENDAR);
            }
        }

        initViews();
        setupTabs();
        setupSearch();
        setupFab();
        setupMenuButtons();
        setupObservers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
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
        searchClearBtn.setOnClickListener(v -> searchInput.setText(""));
    }

    private void setupFab() {
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskDetailActivity.class);
            startActivity(intent);
        });
    }

    private void setupMenuButtons() {
        findViewById(R.id.sortButton).setOnClickListener(v -> showSortDialog());
        findViewById(R.id.dashboardButton).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void showSortDialog() {
        String[] options = {"按开始时间", "按截止时间", "按优先级"};
        int checkedItem = currentSort.equals("due") ? 1 : currentSort.equals("priority") ? 2 : 0;
        new AlertDialog.Builder(this)
                .setTitle("排序方式")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    switch (which) {
                        case 0: currentSort = "start"; break;
                        case 1: currentSort = "due"; break;
                        case 2: currentSort = "priority"; break;
                    }
                    dialog.dismiss();
                    loadTasks();
                })
                .show();
    }

    private void setupObservers() {
        loadTasks();
    }

    private void loadTasks() {
        AppDatabase db = TodoApplication.getInstance().getDatabase();
        TaskDao dao = db.taskDao();

        LiveData<List<Task>> liveData;
        if (!searchQuery.isEmpty()) {
            liveData = dao.searchTasks("%" + searchQuery + "%");
        } else {
            switch (currentSort) {
                case "start":
                    liveData = currentTab == 0 ? dao.getAllTasksSortedByStartDate()
                            : currentTab == 1 ? dao.getActiveTasksSortedByStartDate() : dao.getCompletedTasksSortedByStartDate();
                    break;
                case "due":
                    liveData = currentTab == 0 ? dao.getAllTasksSortedByDueDate()
                            : currentTab == 1 ? dao.getActiveTasksSortedByDueDate() : dao.getCompletedTasksSortedByDueDate();
                    break;
                case "priority":
                    liveData = currentTab == 0 ? dao.getAllTasksSortedByPriority()
                            : currentTab == 1 ? dao.getActiveTasks() : dao.getCompletedTasksSortedByPriority();
                    break;
                default:
                    liveData = currentTab == 0 ? dao.getAllTasks()
                            : currentTab == 1 ? dao.getActiveTasks() : dao.getCompletedTasks();
                    break;
            }
        }

        liveData.observe(this, tasks -> {
            taskList = tasks;
            updateUI();
        });
    }

    private void updateUI() {
        if (taskList == null || taskList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            emptyText.setText(searchQuery.isEmpty()
                    ? (currentTab == 0 ? "还没有待办事项\n点击 + 添加"
                        : currentTab == 1 ? "没有待办事项 🎉" : "还没有已完成的任务")
                    : "没有找到「" + searchQuery + "」");
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        if (adapter == null) {
            adapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskClickListener() {
                @Override
                public void onToggleComplete(Task task, int position) {
                    AppDatabase db = TodoApplication.getInstance().getDatabase();
                    new Thread(() -> {
                        task.setCompleted(!task.isCompleted());
                        if (task.isCompleted()) {
                            task.setStatus(Task.STATUS_DONE);
                            com.example.todo.util.CalendarHelper.removeTaskReminder(MainActivity.this, task);
                        }
                        db.taskDao().update(task);
                    }).start();
                }

                @Override
                public void onDelete(Task task, int position) {
                    pendingUndoTask = task;
                    AppDatabase db = TodoApplication.getInstance().getDatabase();
                    new Thread(() -> {
                        if (task.hasCalendarEvent()) {
                            com.example.todo.util.CalendarHelper.deleteEvent(MainActivity.this, task.getCalendarEventId());
                        }
                        db.taskDao().delete(task);
                    }).start();

                    Snackbar.make(recyclerView, "已删除", Snackbar.LENGTH_LONG)
                            .setAction("撤销", v -> {
                                if (pendingUndoTask != null) {
                                    new Thread(() -> {
                                        db.taskDao().insert(pendingUndoTask);
                                    }).start();
                                    pendingUndoTask = null;
                                }
                            })
                            .show();
                }

                @Override
                public void onItemClick(Task task, int position) {
                    try {
                        Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
                        intent.putExtra("task_id", task.getId());
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "打开详情失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(taskList);
        }
    }
}