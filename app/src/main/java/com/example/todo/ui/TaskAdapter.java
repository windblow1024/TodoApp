package com.example.todo.ui;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.R;
import com.example.todo.data.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());

    public interface OnTaskClickListener {
        void onToggleComplete(Task task, int position);
        void onDelete(Task task, int position);
        void onItemClick(Task task, int position);
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
        return taskList != null ? taskList.size() : 0;
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        CheckBox checkBox;
        TextView titleText;
        TextView timeText;
        TextView priorityBadge;
        TextView categoryBadge;
        TextView dueDateText;
        TextView statusText;
        ImageButton deleteButton;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);

            // 整行点击进入详情
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(taskList.get(pos), pos);
                }
            });
            checkBox = itemView.findViewById(R.id.checkBox);
            titleText = itemView.findViewById(R.id.titleText);
            timeText = itemView.findViewById(R.id.timeText);
            priorityBadge = itemView.findViewById(R.id.priorityBadge);
            categoryBadge = itemView.findViewById(R.id.categoryBadge);
            dueDateText = itemView.findViewById(R.id.dueDateText);
            statusText = itemView.findViewById(R.id.statusText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(final Task task, final int position) {
            Context ctx = itemView.getContext();
            checkBox.setChecked(task.isCompleted());
            titleText.setText(task.getTitle());
            timeText.setText(dateFormat.format(new Date(task.getCreatedAt())));

            // Priority badge
            switch (task.getPriority()) {
                case Task.PRIORITY_P0_CRITICAL:
                    priorityBadge.setText("P0 紧急");
                    priorityBadge.setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.holo_red_dark));
                    break;
                case Task.PRIORITY_P1_IMPORTANT:
                    priorityBadge.setText("P1 重要");
                    priorityBadge.setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.holo_orange_dark));
                    break;
                case Task.PRIORITY_P2_URGENT:
                    priorityBadge.setText("P2 紧急");
                    priorityBadge.setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.holo_blue_dark));
                    break;
                default:
                    priorityBadge.setText("P3 普通");
                    priorityBadge.setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.darker_gray));
                    break;
            }

            // Category badge
            categoryBadge.setText(task.getCategoryLabel());
            categoryBadge.setVisibility(View.VISIBLE);

            // Status
            statusText.setText(task.getStatusLabel());
            statusText.setVisibility(View.VISIBLE);

            // Due date
            if (task.hasDueDate()) {
                dueDateText.setVisibility(View.VISIBLE);
                dueDateText.setText("截止 " + dateFormat.format(new Date(task.getDueDate())));
                if (!task.isCompleted() && task.getDueDate() < System.currentTimeMillis()) {
                    dueDateText.setTextColor(ContextCompat.getColor(ctx, android.R.color.holo_red_light));
                } else {
                    dueDateText.setTextColor(ContextCompat.getColor(ctx, android.R.color.darker_gray));
                }
            } else {
                dueDateText.setVisibility(View.GONE);
            }

            // Completed style
            float alpha = task.isCompleted() ? 0.4f : 1.0f;
            titleText.setAlpha(alpha);
            timeText.setAlpha(alpha * 0.6f);

            // Checkbox click
            checkBox.setOnClickListener(v -> {
                vibrate(ctx);
                if (listener != null) listener.onToggleComplete(task, position);
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(task, position);
            });

            // 点击事件已移至 itemView
        }

        private void vibrate(Context ctx) {
            try {
                Vibrator vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(50);
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}