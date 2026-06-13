package com.example.todo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todo.data.AppDatabase;
import com.example.todo.data.Task;
import com.example.todo.util.ThemeUtil;

import java.util.Calendar;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private TextView totalText, activeText, completedText, rateText;
    private LinearLayout chartContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtil.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        totalText = findViewById(R.id.totalText);
        activeText = findViewById(R.id.activeText);
        completedText = findViewById(R.id.completedText);
        rateText = findViewById(R.id.rateText);
        chartContainer = findViewById(R.id.chartContainer);

        loadStats();
    }

    private void loadStats() {
        var db = TodoApplication.getInstance().getDatabase();
        var dao = db.taskDao();

        dao.getTotalCount().observe(this, total -> {
            dao.getActiveCount().observe(this, active -> {
                dao.getCompletedCount().observe(this, completed -> {
                    totalText.setText(String.valueOf(total));
                    activeText.setText(String.valueOf(active));
                    completedText.setText(String.valueOf(completed));

                    int rate = total > 0 ? (completed * 100 / total) : 0;
                    rateText.setText(rate + "%");

                    // 绘制环形图
                    chartContainer.removeAllViews();
                    chartContainer.addView(new DonutChartView(this, total, completed, active));

                    // 统计文本
                    StringBuilder sb = new StringBuilder();
                    sb.append("📊 统计概要\n\n");
                    sb.append("总任务: ").append(total).append("\n");
                    sb.append("待办: ").append(active).append("\n");
                    sb.append("已完成: ").append(completed).append("\n");
                    sb.append("完成率: ").append(rate).append("%\n\n");

                    sb.append("📅 今日任务\n");
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    long startOfDay = cal.getTimeInMillis();
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    long endOfDay = cal.getTimeInMillis();

                    dao.getTasksForDate(startOfDay, endOfDay).observe(this, todayTasks -> {
                        sb.append("今日待办: ").append(todayTasks.size()).append("项\n");
                        for (Task t : todayTasks) {
                            sb.append("  ").append(t.isCompleted() ? "✅" : "☐").append(" ")
                              .append(t.getTitle()).append("\n");
                        }
                        ((TextView) findViewById(R.id.statsDetailText)).setText(sb.toString());
                    });
                });
            });
        });
    }

    // 自定义环形图 View
    static class DonutChartView extends android.view.View {
        private final Paint completedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint activePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rectF = new RectF();
        private final int total, completed, active;

        public DonutChartView(android.content.Context context, int total, int completed, int active) {
            super(context);
            this.total = total;
            this.completed = completed;
            this.active = active;

            completedPaint.setColor(0xFF34C759);
            completedPaint.setStyle(Paint.Style.STROKE);
            completedPaint.setStrokeWidth(40);

            activePaint.setColor(0xFF007AFF);
            activePaint.setStyle(Paint.Style.STROKE);
            activePaint.setStrokeWidth(40);

            bgPaint.setColor(0xFFE5E5EA);
            bgPaint.setStyle(Paint.Style.STROKE);
            bgPaint.setStrokeWidth(40);

            setLayoutParams(new LinearLayout.LayoutParams(400, 400));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            int size = Math.min(w, h);
            int padding = 40;
            rectF.set(padding, padding, size - padding, size - padding);

            if (total == 0) {
                canvas.drawArc(rectF, 0, 360, false, bgPaint);
                return;
            }

            float completedSweep = (float) completed / total * 360;
            float activeSweep = (float) active / total * 360;

            // Background
            canvas.drawArc(rectF, 0, 360, false, bgPaint);

            // Completed (green)
            canvas.drawArc(rectF, -90, completedSweep, false, completedPaint);

            // Active (blue)
            canvas.drawArc(rectF, -90 + completedSweep, activeSweep, false, activePaint);
        }
    }
}