package com.free.testers;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.facebook.shimmer.ShimmerFrameLayout;

public class HomeActivity extends AppCompatActivity {
    private ShimmerFrameLayout shimmerContainer;
    private LinearLayout contentContainer;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        shimmerContainer = findViewById(R.id.shimmerContainer);
        contentContainer = findViewById(R.id.contentContainer);
        handler = new Handler();

        // Start shimmer animation
        shimmerContainer.startShimmer();

        // Simulate loading delay
        handler.postDelayed(() -> {
            shimmerContainer.stopShimmer();
            shimmerContainer.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
        }, 2000); // 2 seconds delay

        // Initialize other views and setup click listeners
        // ... existing code ...
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
} 