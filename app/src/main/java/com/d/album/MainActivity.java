package com.d.album;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.d.lib.album.Album;
import com.d.lib.album.activity.AlbumActivity;
import com.d.lib.album.activity.CaptureActivity;
import com.d.lib.album.activity.PhotoPreviewActivity;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_CODE_CAPTURE = 1001;
    public static final int REQUEST_CODE_ALBUM = 1002;

    private TextView tv_list;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_photo_preview:
                PhotoPreviewActivity.openActivity(this,
                        Arrays.asList("",
                                "",
                                ""),
                        5);
                break;

            case R.id.btn_photo_capture:
                CaptureActivity.openActivityForResult(this, CaptureActivity.REQUEST_CODE_CAPTURE);
                break;

            case R.id.btn_open:
                Album.with(this)
                        .maxCount(9)
                        .originEnable(false)
                        .startActivityForResult(REQUEST_CODE_ALBUM);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindView();
    }

    private void bindView() {
        tv_list = findViewById(R.id.tv_list);

        findViewById(R.id.btn_photo_preview).setOnClickListener(this);
        findViewById(R.id.btn_photo_capture).setOnClickListener(this);
        findViewById(R.id.btn_open).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = CaptureActivity.getExtra(data);
                String content = uri != null ? uri.toString() : "";
                tv_list.setText("Capture result:\n\n" + content);
            } else {
                tv_list.setText("Capture result canceled: " + resultCode);
            }

        } else if (requestCode == REQUEST_CODE_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                List<Uri> list = AlbumActivity.getExtras(data);
                String content = list.toString().replaceAll(",", "\n\n");
                tv_list.setText("Album result:\n\n" + content);
            } else {
                tv_list.setText("Album result canceled: " + resultCode);
            }
        }
    }
}
