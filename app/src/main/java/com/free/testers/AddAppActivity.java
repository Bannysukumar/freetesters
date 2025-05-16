package com.free.testers;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddAppActivity extends AppCompatActivity {
    private EditText appNameInput, devNameInput, appLinkInput, imageUrlInput;
    private ImageView appIconPreview;
    private Button createListingButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_app);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        appNameInput = findViewById(R.id.appNameInput);
        devNameInput = findViewById(R.id.devNameInput);
        appLinkInput = findViewById(R.id.appLinkInput);
        imageUrlInput = findViewById(R.id.imageUrlInput);
        appIconPreview = findViewById(R.id.appIconPreview);
        createListingButton = findViewById(R.id.createListingButton);

        imageUrlInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String url = imageUrlInput.getText().toString().trim();
                if (!url.isEmpty()) {
                    previewImage(url);
                }
            }
        });

        createListingButton.setOnClickListener(v -> createAppListing());
    }

    private void createAppListing() {
        String appName = appNameInput.getText().toString().trim();
        String devName = devNameInput.getText().toString().trim();
        String appLink = appLinkInput.getText().toString().trim();
        String imageUrl = imageUrlInput.getText().toString().trim();

        // Validate inputs
        if (appName.isEmpty() || devName.isEmpty() || appLink.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate image URL
        if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
            Toast.makeText(this, "Please enter a valid image URL starting with http:// or https://", 
                         Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to add an app", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("AddAppActivity", "Creating app listing with icon URL: " + imageUrl);

        // Create app data
        Map<String, Object> appData = new HashMap<>();
        appData.put("name", appName);
        appData.put("developer", devName);
        appData.put("playStoreLink", appLink);
        appData.put("iconUrl", imageUrl);
        appData.put("userId", currentUser.getUid());
        appData.put("status", "Active");
        appData.put("category", "General"); // You can add a category selector later
        appData.put("description", ""); // You can add a description field later
        appData.put("packageName", extractPackageName(appLink));
        appData.put("createdAt", System.currentTimeMillis());

        // Save to Firestore
        db.collection("apps")
            .add(appData)
            .addOnSuccessListener(documentReference -> {
                android.util.Log.d("AddAppActivity", "App listing created successfully with ID: " + 
                                 documentReference.getId());
                
                // Create notification data
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("appId", documentReference.getId());
                notificationData.put("appName", appName);
                notificationData.put("developer", devName);
                notificationData.put("createdAt", System.currentTimeMillis());
                notificationData.put("type", "new_app");

                // Save notification data to trigger Cloud Function
                db.collection("notifications")
                    .add(notificationData)
                    .addOnSuccessListener(notifRef -> {
                        android.util.Log.d("AddAppActivity", "Notification trigger created successfully");
                        Toast.makeText(AddAppActivity.this, "App listing created successfully!", 
                                     Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("AddAppActivity", "Error creating notification trigger", e);
                        Toast.makeText(AddAppActivity.this, "App listing created but notification failed", 
                                     Toast.LENGTH_SHORT).show();
                        finish();
                    });
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AddAppActivity", "Error creating app listing", e);
                Toast.makeText(AddAppActivity.this, "Error creating app listing: " + e.getMessage(), 
                             Toast.LENGTH_SHORT).show();
            });
    }

    private void previewImage(String url) {
        if (url != null && !url.isEmpty()) {
            android.util.Log.d("AddAppActivity", "Previewing image from URL: " + url);
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .timeout(15000)
                .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model,
                            Target<android.graphics.drawable.Drawable> target,
                            boolean isFirstResource) {
                        android.util.Log.e("AddAppActivity", "Failed to load preview image: " + e.getMessage());
                        Toast.makeText(AddAppActivity.this, "Failed to load image preview", 
                                     Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                            Target<android.graphics.drawable.Drawable> target,
                            DataSource dataSource, boolean isFirstResource) {
                        android.util.Log.d("AddAppActivity", "Successfully loaded preview image");
                        return false;
                    }
                })
                .into(appIconPreview);
        }
    }

    private String extractPackageName(String playStoreLink) {
        // Extract package name from Play Store link
        // Example: https://play.google.com/store/apps/details?id=com.example.app
        try {
            String[] parts = playStoreLink.split("id=");
            if (parts.length > 1) {
                return parts[1];
            }
        } catch (Exception e) {
            // If extraction fails, return a default
            return "com.example.app";
        }
        return "com.example.app";
    }
} 