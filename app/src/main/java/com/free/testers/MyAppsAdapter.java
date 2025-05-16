package com.free.testers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class MyAppsAdapter extends RecyclerView.Adapter<MyAppsAdapter.ViewHolder> {
    private List<MyAppsActivity.AppItem> apps;

    public MyAppsAdapter(List<MyAppsActivity.AppItem> apps) {
        this.apps = apps;
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
        MyAppsActivity.AppItem app = apps.get(position);
        holder.appName.setText(app.name);
        holder.developerName.setText(app.developer);
        holder.category.setText(app.category);
        holder.status.setText(app.status);

        // Load app icon using Glide
        if (app.getIconUrl() != null && !app.getIconUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(app.getIconUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.appIcon);
        } else {
            holder.appIcon.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView appName, developerName, category, status;
        ImageView appIcon, rightArrow;

        ViewHolder(View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            developerName = itemView.findViewById(R.id.appDev);
            category = itemView.findViewById(R.id.category);
            status = itemView.findViewById(R.id.status);
            appIcon = itemView.findViewById(R.id.appIcon);
            rightArrow = itemView.findViewById(R.id.rightArrow);
        }
    }
} 