package com.example.todo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    private int currentTab = 0; // 0=全部, 1=待办, 2=已完成

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = TodoApplication.getInstance().getDatabaseHelper();

        initViews();
        setupTabs();
        setupFab();
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

        // Empty view
        emptyText.setText(getString(R.string.empty_all));
    }

    private void setupTabs() {
        tabAll.setOnClickListener(v -> selectTab(0));
        tabActive.setOnClickListener(v -> selectTab(1));
        tabCompleted.setOnClickListener(v -> selectTab(2));
        selectTab(0);
    }

    private void selectTab(int index) {
        currentTab = index;

        // Reset all tabs
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

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddDialog());
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
                dialog.dismiss();
            } else {
                Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void loadTasks() {
        switch (currentTab) {
            case 0:
                taskList = dbHelper.getAllTasks();
                break;
            case 1:
                taskList = dbHelper.getActiveTasks();
                break;
            case 2:
                taskList = dbHelper.getCompletedTasks();
                break;
        }

        if (taskList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            switch (currentTab) {
                case 0: emptyText.setText(R.string.empty_all); break;
                case 1: emptyText.setText(R.string.empty_active); break;
                case 2: emptyText.setText(R.string.empty_completed); break;
            }
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        if (adapter == null) {
            adapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskClickListener() {
                @Override
                public void onToggleComplete(Task task, int position) {
                    dbHelper.updateTaskStatus(task.getId(), !task.isCompleted());
                    loadTasks();
                }

                @Override
                public void onDelete(Task task, int position) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("确定删除「" + task.getTitle() + "」？")
                            .setPositiveButton("删除", (dialog, which) -> {
                                dbHelper.deleteTask(task.getId());
                                loadTasks();
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(taskList);
        }
    }
}
