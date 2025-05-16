package com.free.testers;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import java.util.List;

public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder> {
    private List<MainActivity.AppItem> apps;
    private OnItemClickListener listener;

    public AppsAdapter(List<MainActivity.AppItem> apps, OnItemClickListener listener) {
        this.apps = apps;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainActivity.AppItem app = apps.get(position);
        holder.appName.setText(app.name);
        holder.developerName.setText(app.developer);
        holder.category.setText(app.category);
        holder.status.setText(app.status);
        
        // Load app icon from URL using Glide
        if (app.iconUrl != null && !app.iconUrl.isEmpty()) {
            android.util.Log.d("AppsAdapter", "Loading icon from URL: " + app.iconUrl);
            Glide.with(holder.itemView.getContext())
                .load(app.iconUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .timeout(15000) // 15 seconds timeout
                .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model,
                            Target<android.graphics.drawable.Drawable> target,
                            boolean isFirstResource) {
                        android.util.Log.e("AppsAdapter", "Failed to load icon: " + e.getMessage());
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                            Target<android.graphics.drawable.Drawable> target,
                            DataSource dataSource, boolean isFirstResource) {
                        android.util.Log.d("AppsAdapter", "Successfully loaded icon");
                        return false;
                    }
                })
                .into(holder.appIcon);
        } else {
            android.util.Log.d("AppsAdapter", "No icon URL provided for app: " + app.name);
            holder.appIcon.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView appName, developerName, category, status;
        ImageView appIcon, rightArrow;
        Button testNowButton;

        ViewHolder(View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            developerName = itemView.findViewById(R.id.appDev);
            category = itemView.findViewById(R.id.category);
            status = itemView.findViewById(R.id.status);
            appIcon = itemView.findViewById(R.id.appIcon);
            rightArrow = itemView.findViewById(R.id.rightArrow);
            testNowButton = itemView.findViewById(R.id.testNowButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(apps.get(position));
                }
            });

            testNowButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    MainActivity.AppItem app = apps.get(position);
                    // Launch the app using package name
                    Intent launchIntent = itemView.getContext().getPackageManager()
                            .getLaunchIntentForPackage(app.packageName);
                    if (launchIntent != null) {
                        itemView.getContext().startActivity(launchIntent);
                    } else {
                        // If app is not installed, open Play Store
                        Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
                        playStoreIntent.setData(Uri.parse(app.playStoreLink));
                        itemView.getContext().startActivity(playStoreIntent);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(MainActivity.AppItem app);
    }
} 