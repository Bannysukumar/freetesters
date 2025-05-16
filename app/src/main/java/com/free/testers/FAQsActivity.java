package com.free.testers;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.facebook.shimmer.ShimmerFrameLayout;

public class FAQsActivity extends AppCompatActivity {
    private ShimmerFrameLayout shimmerContainer;
    private LinearLayout contentContainer;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqs);

        // Initialize views
        shimmerContainer = findViewById(R.id.shimmerContainer);
        contentContainer = findViewById(R.id.contentContainer);
        handler = new Handler();

        // Back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Start shimmer animation
        shimmerContainer.startShimmer();

        // Simulate loading delay
        handler.postDelayed(() -> {
            shimmerContainer.stopShimmer();
            shimmerContainer.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
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
} 