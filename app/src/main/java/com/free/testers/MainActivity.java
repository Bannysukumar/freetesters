package com.free.testers;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import android.util.Log;
import java.util.HashMap;
import com.free.testers.utils.PermissionUtils;
import android.Manifest;
import android.content.pm.PackageManager;

public class MainActivity extends AppCompatActivity implements AppsAdapter.OnItemClickListener {

    private RecyclerView appsRecyclerView;
    private EditText searchBar;
    private ProgressBar loadingProgressBar;
    private AppsAdapter appsAdapter;
    private List<AppItem> appList = new ArrayList<>();
    private List<AppItem> filteredList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Initialize views
        appsRecyclerView = findViewById(R.id.appsRecyclerView);
        searchBar = findViewById(R.id.searchBar);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        
        // Set up RecyclerView
        appsAdapter = new AppsAdapter(filteredList, this);
        appsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        appsRecyclerView.setAdapter(appsAdapter);

        // Check and request permissions
        checkAndRequestPermissions();

        // Set up navigation
        findViewById(R.id.navHome).setOnClickListener(v -> {});
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });
        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            if (PermissionUtils.hasStoragePermission(this)) {
                startActivity(new Intent(MainActivity.this, AddAppActivity.class));
            } else {
                showPermissionExplanationDialog();
            }
        });

        // Set up search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load apps from Firestore
        loadApps();
    }

    private void checkAndRequestPermissions() {
        // Check storage permission
        if (!PermissionUtils.hasStoragePermission(this)) {
            if (PermissionUtils.shouldShowStoragePermissionRationale(this)) {
                showPermissionExplanationDialog();
            } else {
                PermissionUtils.requestStoragePermission(this);
            }
        }

        // Check notification permission
        if (!PermissionUtils.hasNotificationPermission(this)) {
            if (PermissionUtils.shouldShowNotificationPermissionRationale(this)) {
                showNotificationPermissionExplanationDialog();
            } else {
                PermissionUtils.requestNotificationPermission(this);
            }
        }
    }

    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Storage Permission Required")
            .setMessage("This app needs storage permission to upload app icons and screenshots. Please grant the permission to continue.")
            .setPositiveButton("Grant Permission", (dialog, which) -> {
                PermissionUtils.requestStoragePermission(this);
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            })
            .show();
    }

    private void showNotificationPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Notification Permission Required")
            .setMessage("This app needs notification permission to send you updates about your apps and testing opportunities.")
            .setPositiveButton("Grant Permission", (dialog, which) -> {
                PermissionUtils.requestNotificationPermission(this);
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            })
            .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the operation
                if (permissions[0].equals(Manifest.permission.READ_MEDIA_IMAGES) ||
                    permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Storage permission granted
                    Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
                } else if (permissions[0].equals(Manifest.permission.POST_NOTIFICATIONS)) {
                    // Notification permission granted
                    registerFCMToken();
                }
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied. Some features may not work properly.", 
                             Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerFCMToken() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w("MainActivity", "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();
                Log.d("MainActivity", "FCM Token: " + token);

                // Save token to Firestore
                db.collection("fcm_tokens")
                    .document(currentUser.getUid())
                    .set(new HashMap<String, Object>() {{
                        put("token", token);
                        put("userId", currentUser.getUid());
                        put("updatedAt", System.currentTimeMillis());
                    }})
                    .addOnSuccessListener(aVoid -> Log.d("MainActivity", "FCM token saved successfully"))
                    .addOnFailureListener(e -> Log.e("MainActivity", "Error saving FCM token", e));
            });
    }

    private void loadApps() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        loadingProgressBar.setVisibility(ProgressBar.VISIBLE);
        appsRecyclerView.setVisibility(RecyclerView.GONE);

        Log.d("MainActivity", "Loading apps for user: " + currentUser.getUid());
        Toast.makeText(this, "Loading all apps...", Toast.LENGTH_SHORT).show();

        db.collection("apps")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                appList.clear();
                Log.d("MainActivity", "Found " + queryDocumentSnapshots.size() + " apps");
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String name = document.getString("name");
                    String developer = document.getString("developer");
                    String description = document.getString("description");
                    String category = document.getString("category");
                    String packageName = document.getString("packageName");
                    String playStoreLink = document.getString("playStoreLink");
                    String status = document.getString("status");
                    String userId = document.getString("userId");
                    String iconUrl = document.getString("iconUrl");
                    
                    Log.d("MainActivity", "Loading app: " + name + " by " + developer);
                    
                    AppItem app = new AppItem(name, developer, description, category, 
                                            packageName, playStoreLink, status);
                    app.setUserId(userId);
                    app.setIconUrl(iconUrl);
                    appList.add(app);
                }
                filteredList.clear();
                filteredList.addAll(appList);
                appsAdapter.notifyDataSetChanged();
                
                // Hide loading indicator and show RecyclerView
                loadingProgressBar.setVisibility(ProgressBar.GONE);
                appsRecyclerView.setVisibility(RecyclerView.VISIBLE);
                
                if (appList.isEmpty()) {
                    Log.d("MainActivity", "No apps found in database");
                    Toast.makeText(MainActivity.this, "No apps found. Add some apps!", 
                                 Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                // Hide loading indicator and show RecyclerView
                loadingProgressBar.setVisibility(ProgressBar.GONE);
                appsRecyclerView.setVisibility(RecyclerView.VISIBLE);
                
                Log.e("MainActivity", "Error loading apps", e);
                Toast.makeText(MainActivity.this, "Error loading apps: " + e.getMessage(), 
                             Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not signed in, go to LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            // Reload apps when activity starts
            loadApps();
        }
    }

    private void filterApps(String query) {
        filteredList.clear();
        for (AppItem app : appList) {
            if (app.name.toLowerCase().contains(query.toLowerCase()) ||
                app.developer.toLowerCase().contains(query.toLowerCase()) ||
                app.category.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(app);
            }
        }
        appsAdapter.notifyDataSetChanged();
    }

    // Add this method to implement OnItemClickListener
    @Override
    public void onItemClick(AppItem app) {
        // Show the open app dialog
        OpenAppDialog dialog = new OpenAppDialog(this, app.packageName);
        dialog.show();
    }

    // AppItem class
    static class AppItem {
        String name, developer, description, category, packageName, playStoreLink, status, iconUrl;
        String userId;
        
        AppItem(String name, String developer, String description, String category,
                String packageName, String playStoreLink, String status) {
            this.name = name;
            this.developer = developer;
            this.description = description;
            this.category = category;
            this.packageName = packageName;
            this.playStoreLink = playStoreLink;
            this.status = status;
        }

        void setUserId(String userId) {
            this.userId = userId;
        }

        void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }
    }
}