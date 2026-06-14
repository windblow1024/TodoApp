package com.example.todo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todo.util.ThemeUtil;

public class SettingsActivity extends AppCompatActivity {

    private Switch darkModeSwitch;
    private RadioGroup themeGroup;
    private RadioGroup priorityModeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtil.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        themeGroup = findViewById(R.id.themeGroup);
        priorityModeGroup = findViewById(R.id.priorityModeGroup);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("todo_prefs", MODE_PRIVATE);
        darkModeSwitch.setChecked(prefs.getBoolean("dark_mode", false));

        int currentTheme = prefs.getInt("theme_color", ThemeUtil.THEME_BLUE);
        int priorityMode = prefs.getInt("priority_mode", ThemeUtil.PRIORITY_MODE_SIMPLE);
        if (priorityMode == ThemeUtil.PRIORITY_MODE_SIMPLE) findViewById(R.id.radioPrioritySimple);
        else findViewById(R.id.radioPriorityEisenhower);
        switch (currentTheme) {
            case ThemeUtil.THEME_GREEN: themeGroup.check(R.id.radioGreen); break;
            case ThemeUtil.THEME_PURPLE: themeGroup.check(R.id.radioPurple); break;
            case ThemeUtil.THEME_ORANGE: themeGroup.check(R.id.radioOrange); break;
            default: themeGroup.check(R.id.radioBlue); break;
        }

        // 优先级模式
        int currentMode = ThemeUtil.getPriorityMode(this);
        if (currentMode == ThemeUtil.PRIORITY_MODE_SIMPLE) {
            priorityModeGroup.check(R.id.radioPrioritySimple);
        } else {
            priorityModeGroup.check(R.id.radioPriorityEisenhower);
        }

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeUtil.setDarkMode(this, isChecked);
            recreate();
        });

        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int color;
            if (checkedId == R.id.radioGreen) color = ThemeUtil.THEME_GREEN;
            else if (checkedId == R.id.radioPurple) color = ThemeUtil.THEME_PURPLE;
            else if (checkedId == R.id.radioOrange) color = ThemeUtil.THEME_ORANGE;
            else color = ThemeUtil.THEME_BLUE;
            ThemeUtil.setThemeColor(this, color);
            recreate();
        });

        priorityModeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int mode = (checkedId == R.id.radioPrioritySimple)
                    ? ThemeUtil.PRIORITY_MODE_SIMPLE
                    : ThemeUtil.PRIORITY_MODE_EISENHOWER;
            ThemeUtil.setPriorityMode(this, mode);
            recreate();
        });
    }
}