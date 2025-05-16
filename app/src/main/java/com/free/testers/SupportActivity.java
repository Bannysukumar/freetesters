package com.free.testers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SupportActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        // Back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Email support button
        Button emailSupportButton = findViewById(R.id.emailSupportButton);
        emailSupportButton.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@freetesters.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request");
            try {
                startActivity(emailIntent);
            } catch (Exception e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        // FAQs button
        Button faqsButton = findViewById(R.id.faqsButton);
        faqsButton.setOnClickListener(v -> {
            startActivity(new Intent(SupportActivity.this, FAQsActivity.class));
        });
    }
} 