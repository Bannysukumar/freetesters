package com.free.testers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private ImageView profileImage;
    private ImageButton changePhotoButton;
    private TextInputEditText nameInput, emailInput, phoneInput, bioInput;
    private Button saveButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize views
        profileImage = findViewById(R.id.profileImage);
        changePhotoButton = findViewById(R.id.changePhotoButton);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        bioInput = findViewById(R.id.bioInput);
        saveButton = findViewById(R.id.saveButton);

        // Setup image picker
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Glide.with(this)
                        .load(selectedImageUri)
                        .circleCrop()
                        .into(profileImage);
                }
            }
        );

        // Load current user data
        loadUserData();

        // Setup click listeners
        changePhotoButton.setOnClickListener(v -> openImagePicker());
        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Load user data from Firestore
            db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        nameInput.setText(documentSnapshot.getString("name"));
                        emailInput.setText(currentUser.getEmail());
                        phoneInput.setText(documentSnapshot.getString("phone"));
                        bioInput.setText(documentSnapshot.getString("bio"));

                        // Load profile image
                        String imageUrl = documentSnapshot.getString("profileImage");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .circleCrop()
                                .into(profileImage);
                        }
                    }
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error loading profile data", Toast.LENGTH_SHORT).show()
                );
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void saveProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String bio = bioInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            return;
        }

        // Show loading state
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        // Create user data map
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("phone", phone);
        userData.put("bio", bio);

        // If new image is selected, upload it first
        if (selectedImageUri != null) {
            StorageReference imageRef = storage.getReference()
                .child("profile_images")
                .child(currentUser.getUid());

            imageRef.putFile(selectedImageUri)
                .continueWithTask(task -> imageRef.getDownloadUrl())
                .addOnSuccessListener(uri -> {
                    userData.put("profileImage", uri.toString());
                    updateUserData(userData);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show();
                    saveButton.setEnabled(true);
                    saveButton.setText("Save Changes");
                });
        } else {
            updateUserData(userData);
        }
    }

    private void updateUserData(Map<String, Object> userData) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid())
            .update(userData)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
                saveButton.setEnabled(true);
                saveButton.setText("Save Changes");
            });
    }
} 