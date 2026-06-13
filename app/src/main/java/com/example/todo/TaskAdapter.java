package com.example.todo;

import android.animation.ObjectAnimator;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 待办事项列表适配器
 * v2.0: 滑动删除、优先级颜色、完成动效、震动反馈
 */
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
        return taskList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        CheckBox checkBox;
        TextView titleText;
        TextView timeText;
        TextView priorityBadge;
        TextView dueDateText;
        ImageButton deleteButton;
        View swipeDeleteContainer;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            checkBox = itemView.findViewById(R.id.checkBox);
            titleText = itemView.findViewById(R.id.titleText);
            timeText = itemView.findViewById(R.id.timeText);
            priorityBadge = itemView.findViewById(R.id.priorityBadge);
            dueDateText = itemView.findViewById(R.id.dueDateText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            swipeDeleteContainer = itemView.findViewById(R.id.swipeDeleteContainer);
        }

        void bind(final Task task, final int position) {
            Context ctx = itemView.getContext();

            checkBox.setChecked(task.isCompleted());
            titleText.setText(task.getTitle());
            timeText.setText(dateFormat.format(new Date(task.getCreatedAt())));

            // 优先级标签
            switch (task.getPriority()) {
                case Task.PRIORITY_HIGH:
                    priorityBadge.setText("高");
                    priorityBadge.setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.holo_red_light));
                    priorityBadge.setVisibility(View.VISIBLE);
                    break;
                case Task.PRIORITY_MEDIUM:
                    priorityBadge.setText("中");
                    priorityBadge.setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.holo_orange_light));
                    priorityBadge.setVisibility(View.VISIBLE);
                    break;
                default:
                    priorityBadge.setText("低");
                    priorityBadge.setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.darker_gray));
                    priorityBadge.setVisibility(View.VISIBLE);
                    break;
            }

            // 截止日期
            if (task.hasDueDate()) {
                dueDateText.setVisibility(View.VISIBLE);
                dueDateText.setText("截止 " + dateFormat.format(new Date(task.getDueDate())));

                // 过期提醒（未完成且已过期）
                if (!task.isCompleted() && task.getDueDate() < System.currentTimeMillis()) {
                    dueDateText.setTextColor(ContextCompat.getColor(ctx, android.R.color.holo_red_light));
                } else {
                    dueDateText.setTextColor(ContextCompat.getColor(ctx, android.R.color.darker_gray));
                }
            } else {
                dueDateText.setVisibility(View.GONE);
            }

            // 完成状态
            if (task.isCompleted()) {
                titleText.setAlpha(0.4f);
                timeText.setAlpha(0.3f);
                if (priorityBadge != null) priorityBadge.setAlpha(0.4f);
            } else {
                titleText.setAlpha(1.0f);
                timeText.setAlpha(0.6f);
                if (priorityBadge != null) priorityBadge.setAlpha(1.0f);
            }

            // 复选框点击 → 切换完成状态 + 动效 + 震动
            checkBox.setOnClickListener(v -> {
                if (listener != null) {
                    // 震动反馈
                    vibrate(ctx);

                    // 文字淡化动画
                    if (!task.isCompleted()) {
                        titleText.animate().alpha(0.4f).setDuration(300).start();
                    } else {
                        titleText.animate().alpha(1.0f).setDuration(300).start();
                    }

                    listener.onToggleComplete(task, position);
                }
            });

            // 删除按钮
            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(task, position);
                }
            });

            // 点击整行 → 进入详情
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(task, position);
                }
            });
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