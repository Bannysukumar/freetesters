package com.free.testers;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class OpenAppDialog extends Dialog {
    private static final String TAG = "OpenAppDialog";
    private final String packageName;
    private TextView notInstalledMessage;
    private MaterialButton openAppButton;

    public OpenAppDialog(@NonNull Context context, String packageName) {
        super(context);
        this.packageName = packageName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_open_app);

        // Initialize views
        ImageButton closeButton = findViewById(R.id.closeButton);
        openAppButton = findViewById(R.id.openAppButton);
        notInstalledMessage = findViewById(R.id.notInstalledMessage);

        // Set up close button
        closeButton.setOnClickListener(v -> dismiss());

        // Check if app is installed
        boolean isAppInstalled = isAppInstalled(packageName);
        updateUI(isAppInstalled);

        // Set up open app button
        openAppButton.setOnClickListener(v -> {
            if (isAppInstalled) {
                launchApp();
            } else {
                showNotInstalledMessage();
            }
        });
    }

    private boolean isAppInstalled(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            Log.e(TAG, "Package name is null or empty");
            return false;
        }

        try {
            // First try to get the launch intent
            Intent launchIntent = getContext().getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                Log.d(TAG, "App is installed: " + packageName);
                return true;
            }

            // For early access apps, try to find by package name in all installed apps
            PackageManager pm = getContext().getPackageManager();
            List<ApplicationInfo> installedApps;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13 and above
                installedApps = pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0));
            } else {
                // For Android 12 and below
                installedApps = pm.getInstalledApplications(0);
            }

            for (ApplicationInfo appInfo : installedApps) {
                if (packageName.equals(appInfo.packageName)) {
                    // Check if the app is enabled
                    if (appInfo.enabled) {
                        Log.d(TAG, "Early access app found and enabled: " + packageName);
                        return true;
                    } else {
                        Log.d(TAG, "Early access app found but disabled: " + packageName);
                    }
                }
            }

            // If we still haven't found it, try to get package info directly
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PackageManager.PackageInfoFlags flags = PackageManager.PackageInfoFlags.of(0);
                    PackageInfo packageInfo = pm.getPackageInfo(packageName, flags);
                    if (packageInfo != null && packageInfo.applicationInfo.enabled) {
                        Log.d(TAG, "App found through package info: " + packageName);
                        return true;
                    }
                } else {
                    PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
                    if (packageInfo != null && packageInfo.applicationInfo.enabled) {
                        Log.d(TAG, "App found through package info: " + packageName);
                        return true;
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.d(TAG, "App not found through package info: " + packageName);
            }

            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if app is installed: " + e.getMessage());
            return false;
        }
    }

    private void launchApp() {
        try {
            Intent launchIntent = getContext().getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(launchIntent);
                dismiss();
            } else {
                // Try to find the app in the list of installed apps
                PackageManager pm = getContext().getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                    dismiss();
                } else {
                    // If still not found, open Play Store
                    openPlayStore();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error launching app: " + e.getMessage());
            Toast.makeText(getContext(), "Error launching app", Toast.LENGTH_SHORT).show();
            openPlayStore();
        }
    }

    private void openPlayStore() {
        try {
            // First try to open Play Store app
            Intent intent = new Intent(Intent.ACTION_VIEW);
            
            // Clean the package name from any additional parameters
            String cleanPackageName = packageName;
            if (packageName.contains("&")) {
                cleanPackageName = packageName.substring(0, packageName.indexOf("&"));
            }
            
            // Create the Play Store URL
            String playStoreUrl = "market://details?id=" + cleanPackageName;
            intent.setData(Uri.parse(playStoreUrl));
            intent.setPackage("com.android.vending"); // Explicitly set Play Store package
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            // Check if Play Store is installed
            PackageManager pm = getContext().getPackageManager();
            if (intent.resolveActivity(pm) != null) {
                getContext().startActivity(intent);
            } else {
                // If Play Store is not installed, open in browser
                openInBrowser(cleanPackageName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening Play Store: " + e.getMessage());
            openInBrowser(packageName);
        }
    }

    private void openInBrowser(String packageName) {
        try {
            // Clean the package name from any additional parameters
            String cleanPackageName = packageName;
            if (packageName.contains("&")) {
                cleanPackageName = packageName.substring(0, packageName.indexOf("&"));
            }
            
            // Create the Play Store web URL
            String webUrl = "https://play.google.com/store/apps/details?id=" + cleanPackageName;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(webUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening browser: " + e.getMessage());
            Toast.makeText(getContext(), "Could not open Play Store or browser", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNotInstalledMessage() {
        notInstalledMessage.setVisibility(TextView.VISIBLE);
        Toast.makeText(getContext(), "App is not installed, please install it.", Toast.LENGTH_SHORT).show();
        openPlayStore();
    }

    private void updateUI(boolean isAppInstalled) {
        if (!isAppInstalled) {
            notInstalledMessage.setVisibility(TextView.VISIBLE);
            openAppButton.setText("Install App");
        } else {
            notInstalledMessage.setVisibility(TextView.GONE);
            openAppButton.setText("Open App");
        }
    }
} 