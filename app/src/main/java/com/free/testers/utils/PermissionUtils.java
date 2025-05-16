package com.free.testers.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {
    public static final int PERMISSION_REQUEST_CODE = 100;
    
    // Permission arrays for different Android versions
    private static final String[] STORAGE_PERMISSIONS_33_AND_ABOVE = {
        Manifest.permission.READ_MEDIA_IMAGES
    };
    
    private static final String[] STORAGE_PERMISSIONS_BELOW_33 = {
        Manifest.permission.READ_EXTERNAL_STORAGE
    };
    
    private static final String[] NOTIFICATION_PERMISSIONS = {
        Manifest.permission.POST_NOTIFICATIONS
    };

    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, 
                Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, 
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, 
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permission not required for Android 12 and below
    }

    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity, 
                STORAGE_PERMISSIONS_33_AND_ABOVE, PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(activity, 
                STORAGE_PERMISSIONS_BELOW_33, PERMISSION_REQUEST_CODE);
        }
    }

    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity, 
                NOTIFICATION_PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }

    public static boolean shouldShowStoragePermissionRationale(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, 
                Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, 
                Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    public static boolean shouldShowNotificationPermissionRationale(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, 
                Manifest.permission.POST_NOTIFICATIONS);
        }
        return false;
    }
} 