package com.d.lib.album;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.d.lib.album.activity.AlbumActivity;

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

    public static Album with(Activity activity) {
        return new Album(activity);
    }

    public static Album with(Fragment fragment) {
        return new Album(fragment);
    }

    public Album originEnable(boolean enable) {
        mBundle.putBoolean(AlbumActivity.EXTRA_BUNDLE_ORIGIN_ENABLE, enable);
        return this;
    }

    public Album maxCount(int count) {
        mBundle.putInt(AlbumActivity.EXTRA_BUNDLE_MAX_COUNT, count);
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
