package com.d.lib.album.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.d.lib.album.R;
import com.d.lib.album.util.CachePool;
import com.d.lib.album.util.FileProviderCompat;
import com.d.lib.album.util.IntentUtils;
import com.d.lib.album.util.PermissionChecker;
import com.d.lib.album.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * CaptureActivity
 * Created by D on 2017/4/26.
 */
public class CaptureActivity extends FragmentActivity {

    public static final int REQUEST_CODE_CAPTURE = 1001;
    public static final int REQUEST_CODE_PERMISSION = 2001;

    public static final String EXTRA_RESULT_URI = "EXTRA_RESULT_URI";

    private String mCurrentTempFilePath = "";
    private boolean mIsOk;

    public static void openActivityForResult(Activity activity, int requestCode) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, CaptureActivity.class);
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
        Uri uri = getUri(mCurrentTempFilePath);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_URI, uri);
        return intent;
    }

    private void confirm() {
        mIsOk = true;
        File file = new File(mCurrentTempFilePath);
        if (file.exists() && file.isFile()) {
            MediaScannerConnection.scanFile(this,
                    new String[]{file.getAbsolutePath()}, null, null);
        }
        setResult(RESULT_OK, getResultIntent());
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        if (!Utils.isCameraAvailable(this)) {
            Toast.makeText(this, getString(R.string.lib_album_camera_not_available),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        PermissionChecker.permissionsCheck(this,
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
        try {
            final File file = createImageFile();
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentTempFilePath = file.getAbsolutePath();
            final Uri uri = FileProviderCompat.getUriForFile(this, file);
            final Intent intent = IntentUtils.getCaptureIntent(uri);
            if (intent.resolveActivity(getPackageManager()) == null) {
                throw new RuntimeException("Cannot launch camera");
            }
            startActivityForResult(intent, REQUEST_CODE_CAPTURE);
        } catch (Throwable e) {
            e.printStackTrace();
            // Cannot launch camera
            Toast.makeText(this, getString(R.string.lib_album_camera_error),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private File createImageFile() throws IOException {
        final String fileName = CachePool.FILE_CAMERA_PREFIX + System.currentTimeMillis();
        final File dir = new File(CachePool.getCameraDirectoryPath());
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        return File.createTempFile(fileName, ".jpg", dir);
    }

    private Uri getUri(String path) {
        return Uri.fromFile(new File(path));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (PermissionChecker.onRequestPermissionsResult(requestCode,
                    permissions, grantResults)) {
                nextInit();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                confirm();
            } else {
                finish();
            }
        }
    }

    @Override
    public void finish() {
        if (!mIsOk) {
            CachePool.deleteFile(new File(mCurrentTempFilePath));
        }
        super.finish();
    }
}
