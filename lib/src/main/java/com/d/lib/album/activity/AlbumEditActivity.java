package com.d.lib.album.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.d.lib.album.R;
import com.d.lib.album.compress.Engine;
import com.d.lib.album.util.CachePool;
import com.d.lib.album.util.PermissionsChecker;
import com.d.lib.album.widget.PhotoEditView;

import java.io.File;
import java.util.Collections;

/**
 * AlbumEditActivity
 * Created by D on 2017/4/26.
 */
public class AlbumEditActivity extends FragmentActivity implements View.OnClickListener {

    public static final int REQUEST_CODE_PERMISSION = 2001;

    public static final String EXTRA_RESULT_URI = "EXTRA_RESULT_URI";

    private PhotoEditView iv_photo;
    private Uri mUri;
    private boolean mIsOk;

    public static void openActivityForResult(Activity activity, Uri uri, int requestCode) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, AlbumEditActivity.class);
        intent.putExtra(EXTRA_RESULT_URI, uri);
        if (!(activity instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivityForResult(intent, requestCode);
    }

    @Nullable
    public static Uri getExtra(Intent data) {
        if (data == null) {
            return null;
        }
        return data.getParcelableExtra(EXTRA_RESULT_URI);
    }

    private Intent getResultIntent() {
        Uri uri = getUri(mUri);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_URI, uri);
        return intent;
    }

    private void confirm() {
        mIsOk = true;
        finish();
    }

    @Override
    public void onClick(View v) {
        int resId = v.getId();
        if (R.id.iv_title_left == resId) {
            finish();

        } else if (R.id.iv_undo == resId) {
            iv_photo.getDrawAttacher().undo();

        } else if (R.id.tv_bottom_edit_finish == resId) {
            Bitmap bitmap = iv_photo.save();
            CachePool.getInstance(this).put(mUri, bitmap,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            confirm();
                        }
                    });
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lib_album_activity_album_edit);
        mUri = getIntent().getParcelableExtra(EXTRA_RESULT_URI);
        bindView();
        init();
    }

    private void bindView() {
        iv_photo = findViewById(R.id.iv_photo);

        findViewById(R.id.iv_title_left).setOnClickListener(this);
        findViewById(R.id.iv_undo).setOnClickListener(this);
        findViewById(R.id.tv_bottom_edit_finish).setOnClickListener(this);
    }

    private void init() {
        PermissionsChecker.permissionsCheck(this,
                Collections.singletonList(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_PERMISSION,
                new Runnable() {
                    @Override
                    public void run() {
                        nextInit();
                    }
                });
    }

    private void nextInit() {
        Uri uri = getUri(mUri);
        Engine.load(this, uri, iv_photo);
    }

    private Uri getUri(Uri from) {
        final Uri to;
        final File cache = CachePool.getInstance(this).get(from);
        if (cache != null && cache.exists()) {
            to = Uri.fromFile(cache);
        } else {
            to = from;
        }
        return to;
    }

    @Override
    public void finish() {
        setResult(mIsOk ? RESULT_OK : RESULT_CANCELED, getResultIntent());
        super.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (PermissionsChecker.onRequestPermissionsResult(requestCode,
                    permissions, grantResults)) {
                nextInit();
            } else {
                finish();
            }
        }
    }
}
