/**
 * MainActivity.java - Main activity for Tasbih Minimalis app
 */
package com.lhawi.minimalistasbih;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {

    private TextView counterTextView;
    private TextView subtleLabel;
    private ImageButton resetButton;
    private ImageButton modeToggleButton;
    private ConstraintLayout mainLayout;

    private SharedPreferences preferences;
    private int count = 0;
    private boolean isDarkMode = false;
    private boolean isVibrationEnabled = true;
    private boolean isWakelockEnabled = false;
    private Vibrator vibrator;

    private static final String PREF_NAME = "TasbihPrefs";
    private static final String KEY_COUNT = "counter";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_VIBRATION = "vibration";
    private static final String KEY_WAKELOCK = "wakelock";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences first to set the theme correctly before inflating layout
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        isDarkMode = preferences.getBoolean(KEY_DARK_MODE, false);

        // Apply theme based on saved preference
        applyDarkMode();

        // Set up status bar properly
        setupStatusBar();

        // Now inflate layout with the correct theme applied
        setContentView(R.layout.activity_main);

        // Initialize views
        counterTextView = findViewById(R.id.counterTextView);
        subtleLabel = findViewById(R.id.subtleLabel);
        resetButton = findViewById(R.id.resetButton);
        modeToggleButton = findViewById(R.id.modeToggleButton);
        mainLayout = findViewById(R.id.mainLayout);

        // Initialize vibrator service
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Load remaining preferences
        count = preferences.getInt(KEY_COUNT, 0);
        isVibrationEnabled = preferences.getBoolean(KEY_VIBRATION, true);
        isWakelockEnabled = preferences.getBoolean(KEY_WAKELOCK, false);

        // Update UI based on saved preferences
        updateCounterDisplay();
        applyWakelock();

        // Set up click listener for the main screen to increment counter
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementCounter();
            }
        });

        // Set up click listener for the reset button
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCounter();
            }
        });

        // Set up click listener for the mode toggle button
        modeToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDarkMode();
            }
        });

        // Long press on reset button to toggle vibration
        resetButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggleVibration();
                return true;
            }
        });
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();

            if (isDarkMode) {
                // Dark mode - light text in status bar
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                // Light mode - dark text in status bar
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }

            decorView.setSystemUiVisibility(flags);
        }
    }

    private void incrementCounter() {
        count++;
        updateCounterDisplay();

        // Provide vibration feedback if enabled
        if (isVibrationEnabled && vibrator != null) {
            vibrator.vibrate(20); // 20ms vibration
        }
    }

    private void resetCounter() {
        count = 0;
        updateCounterDisplay();
        Toast.makeText(this, "Counter reset", Toast.LENGTH_SHORT).show();
    }

    private void updateCounterDisplay() {
        counterTextView.setText(String.valueOf(count));
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;

        // Save preference immediately
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_DARK_MODE, isDarkMode);
        editor.apply();

        // Apply the theme change
        applyDarkMode();

        // Show confirmation
        String message = isDarkMode ? "Dark mode enabled" : "Light mode enabled";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Recreate the activity to apply theme changes completely
        recreate();
    }

    private void applyDarkMode() {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void toggleVibration() {
        isVibrationEnabled = !isVibrationEnabled;

        // Save preference immediately
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_VIBRATION, isVibrationEnabled);
        editor.apply();

        // Show confirmation
        String message = isVibrationEnabled ? "Vibration enabled" : "Vibration disabled";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void toggleWakelock() {
        isWakelockEnabled = !isWakelockEnabled;

        // Save preference immediately
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_WAKELOCK, isWakelockEnabled);
        editor.apply();

        applyWakelock();

        // Show confirmation
        String message = isWakelockEnabled ? "Screen will stay on" : "Screen can turn off";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void applyWakelock() {
        if (isWakelockEnabled) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save current counter state
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_COUNT, count);
        editor.apply();
    }

    // Handle night mode properly
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Update UI elements that need to change based on dark/light mode
        int currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            isDarkMode = true;
        } else {
            isDarkMode = false;
        }

        // Update status bar UI
        setupStatusBar();
    }
}