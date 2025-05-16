package com.free.testers;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MyAppsActivity extends AppCompatActivity {
    private RecyclerView myAppsRecyclerView;
    private EditText searchBar;
    private TextView emptyStateText;
    private MyAppsAdapter appsAdapter;
    private List<AppItem> appList = new ArrayList<>();
    private List<AppItem> filteredList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ShimmerFrameLayout shimmerContainer;
    private LinearLayout contentContainer;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_apps);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        myAppsRecyclerView = findViewById(R.id.myAppsRecyclerView);
        searchBar = findViewById(R.id.searchBar);
        emptyStateText = findViewById(R.id.emptyStateText);
        shimmerContainer = findViewById(R.id.shimmerContainer);
        contentContainer = findViewById(R.id.contentContainer);
        handler = new Handler();

        // Set up RecyclerView
        appsAdapter = new MyAppsAdapter(filteredList);
        myAppsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAppsRecyclerView.setAdapter(appsAdapter);

        // Set up navigation
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(MyAppsActivity.this, MainActivity.class));
            finish();
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(MyAppsActivity.this, ProfileActivity.class));
            finish();
        });
        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            startActivity(new Intent(MyAppsActivity.this, AddAppActivity.class));
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

        // Start shimmer animation
        shimmerContainer.startShimmer();

        // Simulate loading delay
        handler.postDelayed(() -> {
            shimmerContainer.stopShimmer();
            shimmerContainer.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
            loadUserApps();
        }, 2000); // 2 seconds delay
    }

    @Override
    protected void onPause() {
        super.onPause();
        shimmerContainer.stopShimmer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shimmerContainer.getVisibility() == View.VISIBLE) {
            shimmerContainer.startShimmer();
        }
    }

    private void loadUserApps() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("apps")
            .whereEqualTo("userId", currentUser.getUid())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                appList.clear();
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

                    AppItem app = new AppItem(name, developer, description, category,
                            packageName, playStoreLink, status);
                    app.setUserId(userId);
                    app.setIconUrl(iconUrl);
                    appList.add(app);
                }

                filteredList.clear();
                filteredList.addAll(appList);
                appsAdapter.notifyDataSetChanged();

                // Show/hide empty state
                if (appList.isEmpty()) {
                    emptyStateText.setVisibility(View.VISIBLE);
                    myAppsRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyStateText.setVisibility(View.GONE);
                    myAppsRecyclerView.setVisibility(View.VISIBLE);
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(MyAppsActivity.this, "Error loading apps: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
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

    // AppItem class
    public static class AppItem {
        public String name;
        public String developer;
        public String description;
        public String category;
        public String packageName;
        public String playStoreLink;
        public String status;
        private String userId;
        private String iconUrl;

        public AppItem(String name, String developer, String description, String category,
                      String packageName, String playStoreLink, String status) {
            this.name = name;
            this.developer = developer;
            this.description = description;
            this.category = category;
            this.packageName = packageName;
            this.playStoreLink = playStoreLink;
            this.status = status;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }

        public String getIconUrl() {
            return iconUrl;
        }
    }
} 