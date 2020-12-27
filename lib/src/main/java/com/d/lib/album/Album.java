package com.d.lib.album;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.d.lib.album.activity.AlbumActivity;
import com.d.lib.album.util.CachePool;

/**
 * Album
 * Created by D on 2020/9/26.
 */
public class Album {
    private final Object mFrom;
    private final Bundle mBundle = new Bundle();

    private Album(Object from) {
        this.mFrom = from;
    }

    public static void setDiskCache(String diskCacheName) {
        CachePool.setDiskCache(diskCacheName);
    }

    /**
     * Start from an Activity.
     * <p>
     * This Activity's {@link Activity#onActivityResult(int, int, Intent)} will be called when user
     * finishes selecting.
     *
     * @param activity Activity instance.
     * @return Instance.
     */
    public static Album with(Activity activity) {
        return new Album(activity);
    }

    /**
     * Start from a Fragment.
     * <p>
     * This Fragment's {@link Fragment#onActivityResult(int, int, Intent)} will be called when user
     * finishes selecting.
     *
     * @param fragment Fragment instance.
     * @return Instance.
     */
    public static Album with(Fragment fragment) {
        return new Album(fragment);
    }

    /**
     * Sets the number of spans to be laid out.
     *
     * @param spanCount The total number of spans in the grid
     * @return {@link Album} for fluent API.
     */
    public Album spanCount(int spanCount) {
        if (spanCount < 1) {
            throw new IllegalArgumentException("Span count should be at least 1. Provided "
                    + spanCount);
        }
        mBundle.putInt(AlbumActivity.EXTRA_BUNDLE_SPAN_COUNT, spanCount);
        return this;
    }

    /**
     * Determines whether the photo capturing is enabled or not on the media grid view.
     * <p>
     * If this value is set true, photo capturing entry will appear only on All Media's page.
     *
     * @param enable Whether to enable capturing or not. Default value is false;
     * @return {@link Album} for fluent API.
     */
    public Album capture(boolean enable) {
        mBundle.putBoolean(AlbumActivity.EXTRA_BUNDLE_CAPTURE_ENABLE, enable);
        return this;
    }

    /**
     * Show a original photo check options.Let users decide whether use original photo after select
     *
     * @param enable Whether to enable original photo or not
     * @return {@link Album} for fluent API.
     */
    public Album originEnable(boolean enable) {
        mBundle.putBoolean(AlbumActivity.EXTRA_BUNDLE_ORIGIN_ENABLE, enable);
        return this;
    }

    /**
     * Maximum selectable count.
     *
     * @param count Maximum selectable count. Default value is 9.
     * @return {@link Album} for fluent API.
     */
    public Album maxSelectable(int count) {
        mBundle.putInt(AlbumActivity.EXTRA_BUNDLE_MAX_SELECTABLE, count);
        return this;
    }

    public void startActivityForResult(int requestCode) {
        if (mFrom instanceof Activity) {
            AlbumActivity.openActivityForResult((Activity) mFrom, requestCode, mBundle);
        } else if (mFrom instanceof Fragment) {
            AlbumActivity.openActivityForResult((Fragment) mFrom, requestCode, mBundle);
        }
    }
}
