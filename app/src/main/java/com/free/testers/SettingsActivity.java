package com.free.testers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton backButton = findViewById(R.id.backButton);
        LinearLayout faqsRow = findViewById(R.id.faqsRow);
        LinearLayout supportRow = findViewById(R.id.supportRow);
        LinearLayout googleGroupRow = findViewById(R.id.googleGroupRow);
        LinearLayout policiesRow = findViewById(R.id.policiesRow);
        Switch switchLightMode = findViewById(R.id.switchLightMode);
        Button contactUsButton = findViewById(R.id.contactUsButton);

        backButton.setOnClickListener(v -> finish());

        faqsRow.setOnClickListener(v -> startActivity(new Intent(this, FAQsActivity.class)));
        supportRow.setOnClickListener(v -> startActivity(new Intent(this, SupportActivity.class)));
        googleGroupRow.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://groups.google.com/"));
            startActivity(browserIntent);
        });
        policiesRow.setOnClickListener(v -> startActivity(new Intent(this, PoliciesActivity.class)));

        // Load saved mode
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean(KEY_DARK_MODE, true);
        switchLightMode.setChecked(!darkMode);
        AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        switchLightMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // If checked, set light mode; else, set dark mode
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            // Save preference
            prefs.edit().putBoolean(KEY_DARK_MODE, !isChecked).apply();
        });

        contactUsButton.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@example.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact Support");
            startActivity(Intent.createChooser(emailIntent, "Contact Us"));
        });
    }
} 