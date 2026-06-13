package com.example.todo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 待办事项列表适配器
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());

    public interface OnTaskClickListener {
        void onToggleComplete(Task task, int position);
        void onDelete(Task task, int position);
    }

    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    public void updateData(List<Task> newList) {
        this.taskList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task, position);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView titleText;
        TextView timeText;
        ImageButton deleteButton;
        View divider;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            titleText = itemView.findViewById(R.id.titleText);
            timeText = itemView.findViewById(R.id.timeText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            divider = itemView.findViewById(R.id.divider);
        }

        void bind(final Task task, final int position) {
            checkBox.setChecked(task.isCompleted());
            titleText.setText(task.getTitle());
            timeText.setText(dateFormat.format(new Date(task.getCreatedAt())));

            if (task.isCompleted()) {
                titleText.setAlpha(0.4f);
                timeText.setAlpha(0.3f);
            } else {
                titleText.setAlpha(1.0f);
                timeText.setAlpha(0.6f);
            }

            // 最后一项不显示分割线
            divider.setVisibility(position == taskList.size() - 1 ? View.GONE : View.VISIBLE);

            checkBox.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleComplete(task, position);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(task, position);
                }
            });
        }
    }
}