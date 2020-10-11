package com.d.lib.album.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.support.annotation.NonNull;

/**
 * Utils
 * Created by D on 2020/10/11.
 **/
public class Utils {

    /**
     * Value of dp to value of px.
     *
     * @param dpValue The value of dp.
     * @return value of px
     */
    public static int dp2px(@NonNull final Context context, final float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static boolean isCameraAvailable(Context context) {
        PackageManager packageManager = context.getPackageManager();
        boolean hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
        boolean hasCameraAny = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            hasCameraAny = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        }
        return hasCamera || hasCameraAny;
    }

    /**
     * Closes {@code cursor}, ignoring any checked exceptions. Does nothing if {@code cursor} is
     * null.
     */
    public static void closeQuietly(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }
}
