package com.d.lib.album.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.util.List;

public class IntentUtils {
    public static int MIME_TYPE_ALL = 0;
    public static int MIME_TYPE_IMAGE = 1;
    public static int MIME_TYPE_VIDEO = 2;

    /**
     * Return whether the intent is available.
     *
     * @param intent The intent.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isIntentAvailable(final Context context, final Intent intent) {
        return queryIntentActivities(context, intent).size() > 0;
    }

    /**
     * Retrieve all activities that can be performed for the given intent.
     *
     * @param context Context
     * @param intent  The desired intent as per resolveActivity().
     * @return Returns a List of ResolveInfo objects containing one entry for
     * each matching activity, ordered from best to worst.
     * If there are no matching activities, an
     * empty list is returned.
     */
    @NonNull
    private static List<ResolveInfo> queryIntentActivities(final Context context,
                                                           final Intent intent) {
        return context.getApplicationContext()
                .getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    }

    /**
     * Return the intent of capture.
     *
     * @param outUri The uri of output.
     * @return the intent of capture
     */
    public static Intent getCaptureIntent(final Uri outUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    public static Intent getPickIntent(final Activity activity,
                                       final int mimeType,
                                       final boolean multiple) {
        final Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && isIntentAvailable(activity, getOpenDocPickIntent(mimeType, multiple))) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }

        setIntentExtra(intent, mimeType, multiple);

        if (queryIntentActivities(activity, intent).size() > 1) {
            // Create and start the chooser
            return Intent.createChooser(intent, "Pick an image");
        }
        return intent;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static Intent getOpenDocPickIntent(final int mimeType, final boolean multiple) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        setIntentExtra(intent, mimeType, multiple);
        return intent;
    }

    private static void setIntentExtra(final Intent intent,
                                       final int mimeType,
                                       final boolean multiple) {
        if (MIME_TYPE_IMAGE == mimeType) {
            intent.setType("image/*");
        } else if (MIME_TYPE_VIDEO == mimeType) {
            intent.setType("video/*");
        } else {
            intent.setType("*/*");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String[] mimeTypes = {"image/*", "video/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple);
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
    }
}
