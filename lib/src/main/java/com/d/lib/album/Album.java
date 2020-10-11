package com.d.lib.album;

import android.app.Activity;
import android.os.Bundle;

import com.d.lib.album.activity.AlbumActivity;

/**
 * Album
 * Created by D on 2020/9/26.
 */
public class Album {
    private final Activity mActivity;
    private final Bundle mBundle = new Bundle();

    private Album(Activity activity) {
        this.mActivity = activity;
    }

    public static Album with(Activity activity) {
        return new Album(activity);
    }

    public Album originEnable(boolean enable) {
        mBundle.putBoolean(AlbumActivity.EXTRA_BUNDLE_ORIGIN_ENABLE, enable);
        return this;
    }

    public void startActivityForResult(int requestCode) {
        AlbumActivity.openActivityForResult(mActivity, requestCode, mBundle);
    }
}
