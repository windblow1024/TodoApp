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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtil.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        themeGroup = findViewById(R.id.themeGroup);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("todo_prefs", MODE_PRIVATE);
        darkModeSwitch.setChecked(prefs.getBoolean("dark_mode", false));

        int currentTheme = prefs.getInt("theme_color", ThemeUtil.THEME_BLUE);
        switch (currentTheme) {
            case ThemeUtil.THEME_GREEN: themeGroup.check(R.id.radioGreen); break;
            case ThemeUtil.THEME_PURPLE: themeGroup.check(R.id.radioPurple); break;
            case ThemeUtil.THEME_ORANGE: themeGroup.check(R.id.radioOrange); break;
            default: themeGroup.check(R.id.radioBlue); break;
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
    }
}