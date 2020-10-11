package com.d.lib.album.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionsChecker {

    public static void permissionsCheck(final Activity activity,
                                        final List<String> requiredPermissions,
                                        final int requestCode,
                                        final Runnable runnable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (runnable != null) {
                runnable.run();
            }
            return;
        }
        final List<String> missingPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (!isGranted(activity, permission)) {
                missingPermissions.add(permission);
            }
        }
        if (missingPermissions.size() > 0) {
            activity.requestPermissions(missingPermissions.toArray(new String[missingPermissions.size()]),
                    requestCode);
            return;
        }
        // All permissions granted
        if (runnable != null) {
            runnable.run();
        }
    }

    public static boolean onRequestPermissionsResult(int requestCode,
                                                     @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                // Required permission missing
                return false;
            }
        }
        return true;
    }

    public static boolean isGranted(Context context, String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        int status = ContextCompat.checkSelfPermission(context, permission);
        return status == PackageManager.PERMISSION_GRANTED;
    }
}
