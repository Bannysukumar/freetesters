package com.free.testers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PoliciesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policies);

        // Back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Privacy Policy button
        Button privacyPolicyButton = findViewById(R.id.privacyPolicyButton);
        privacyPolicyButton.setOnClickListener(v -> {
            // Open privacy policy in browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                Uri.parse("https://sites.google.com/view/free-testers/home"));
            try {
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Could not open privacy policy", Toast.LENGTH_SHORT).show();
            }
        });

        // Terms of Service button
        Button termsButton = findViewById(R.id.termsButton);
        termsButton.setOnClickListener(v -> {
            // Open terms of service in browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                Uri.parse("https://sites.google.com/view/free-testers/terms-and-conditions"));
            try {
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Could not open terms of service", Toast.LENGTH_SHORT).show();
            }
        });

        // Community Guidelines button
        Button guidelinesButton = findViewById(R.id.guidelinesButton);
        guidelinesButton.setOnClickListener(v -> {
            // Open community guidelines in browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                Uri.parse("https://sites.google.com/view/free-testers/terms-and-conditions"));
            try {
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Could not open community guidelines", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 