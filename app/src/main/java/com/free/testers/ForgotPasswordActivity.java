package com.free.testers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText emailInput;
    private Button sendLinkButton;
    private ImageButton backButton;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailInput = findViewById(R.id.emailInput);
        sendLinkButton = findViewById(R.id.sendLinkButton);
        backButton = findViewById(R.id.backButton);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        sendLinkButton.setOnClickListener(v -> sendResetLink());
        backButton.setOnClickListener(v -> finish());
    }

    private void sendResetLink() {
        String email = emailInput.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }
        progressDialog.setMessage("Sending reset link...");
        progressDialog.show();
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Reset link sent to your email.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Failed to send reset link: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }
} 