package com.d.lib.album.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import com.d.lib.album.R;
import com.d.lib.album.adapter.AlbumMediaAdapter;
import com.d.lib.album.model.Album;
import com.d.lib.album.model.Media;
import com.d.lib.album.model.SelectList;
import com.d.lib.album.util.CachePool;
import com.d.lib.album.util.IntentUtils;
import com.d.lib.album.util.PermissionsChecker;
import com.d.lib.album.util.Utils;
import com.d.lib.album.widget.AlbumBottomBar;
import com.d.lib.album.widget.AlbumTitleBar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AlbumActivity
 * Created by D on 2017/4/26.
 */
public class AlbumActivity extends FragmentActivity implements View.OnClickListener {

    public static final int REQUEST_CODE_PREVIEW = 1001;
    public static final int REQUEST_CODE_PICK = 1002;
    public static final int REQUEST_CODE_PERMISSION = 2001;

    public static final String EXTRA_RESULT_SELECTS = "EXTRA_RESULT_SELECTS";
    public static final String EXTRA_RESULT_ORIGIN = "EXTRA_RESULT_ORIGIN";

    public static final String EXTRA_BUNDLE = "EXTRA_BUNDLE";
    public static final String EXTRA_BUNDLE_ORIGIN_ENABLE = "EXTRA_BUNDLE_ORIGIN_ENABLE";
    public static final String EXTRA_BUNDLE_MAX_COUNT = "EXTRA_BUNDLE_MAX_COUNT";

    private Bundle mBundle;
    private AlbumTitleBar album_title;
    private AlbumBottomBar album_bottom;
    private RecyclerView rv_list;
    private AlbumMediaAdapter mAdapter;

    public static void openActivityForResult(Activity activity,
                                             int requestCode,
                                             @Nullable Bundle... bundle) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, AlbumActivity.class);
        if (bundle != null) {
            intent.putExtra(EXTRA_BUNDLE, bundle[0]);
        }
        if (!(activity instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivityForResult(intent, requestCode);
    }

    public static void openActivityForResult(Fragment fragment,
                                             int requestCode,
                                             @Nullable Bundle... bundle) {
        if (fragment == null || fragment.getActivity() == null) {
            return;
        }
        Intent intent = new Intent(fragment.getActivity(), AlbumActivity.class);
        if (bundle != null) {
            intent.putExtra(EXTRA_BUNDLE, bundle[0]);
        }
        fragment.startActivityForResult(intent, requestCode);
    }

    @NonNull
    public static List<Uri> getExtras(Intent data) {
        List<Uri> list = data.getParcelableArrayListExtra(EXTRA_RESULT_SELECTS);
        return list != null ? list : new ArrayList<Uri>();
    }

    private Intent getResultIntent() {
        final List<Media> selected = mAdapter.getSelected().asList();
        final List<Uri> list = new ArrayList<>();
        for (Media media : selected) {
            Uri uri;
            final File cache = CachePool.getInstance(this).get(media.uri);
            if (cache != null && cache.exists()) {
                uri = Uri.fromFile(cache);
            } else {
                uri = media.uri;
            }
            list.add(uri);
        }
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(EXTRA_RESULT_SELECTS, new ArrayList<>(list));
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_RESULT_ORIGIN, album_bottom.isOrigin());
        return intent;
    }

    private void confirm() {
        setResult(RESULT_OK, getResultIntent());
        finish();
    }

    @Override
    public void onClick(View v) {
        int resId = v.getId();
        if (R.id.iv_title_left == resId) {
            finish();

        } else if (R.id.tv_title_right == resId) {
            confirm();

        } else if (R.id.tv_bottom_edit == resId) {
            List<Media> selected = mAdapter.getSelected().asList();
            if (selected.size() <= 0) {
                return;
            }
            preview(null, 0, selected.get(0));
        }
    }

    private void preview(Album album, int position, Media item) {
        Bundle bundle = AlbumPreviewActivity.getBundle(
                mBundle.getBoolean(EXTRA_BUNDLE_ORIGIN_ENABLE, false),
                album_bottom.isOrigin(),
                mBundle.getInt(EXTRA_BUNDLE_MAX_COUNT, SelectList.MAX_COUNT),
                position,
                item);
        AlbumPreviewActivity.openActivityForResult(AlbumActivity.this,
                album, mAdapter.getSelected().asList(),
                REQUEST_CODE_PREVIEW,
                bundle);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lib_album_activity_album);
        bindView();
        init();
    }

    private void bindView() {
        album_title = findViewById(R.id.album_title);
        album_bottom = findViewById(R.id.album_bottom);
        rv_list = findViewById(R.id.rv_list);

        album_title.findViewById(R.id.iv_title_left).setOnClickListener(this);
        album_title.findViewById(R.id.tv_title_right).setOnClickListener(this);

        album_bottom.findViewById(R.id.tv_bottom_edit).setOnClickListener(this);
    }

    private void init() {
        mBundle = getIntent().getBundleExtra(EXTRA_BUNDLE);
        mBundle = mBundle != null ? mBundle : new Bundle();
        album_title.setType(AlbumTitleBar.TYPE_ALBUM);
        album_bottom.setType(AlbumBottomBar.TYPE_ALBUM);
        album_bottom.setOriginEnable(mBundle.getBoolean(EXTRA_BUNDLE_ORIGIN_ENABLE, false));
        album_title.setCount(0);
        album_bottom.setCount(0);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        rv_list.setLayoutManager(layoutManager);

        mAdapter = new AlbumMediaAdapter(this,
                mBundle.getInt(EXTRA_BUNDLE_MAX_COUNT, SelectList.MAX_COUNT));
        mAdapter.setOnClickListener(new AlbumMediaAdapter.OnClickListener() {
            @Override
            public void onCount(int position, Media item, int count) {
                album_title.setCount(count);
                album_bottom.setCount(count);
            }

            @Override
            public void onClick(int position, Media item) {
                preview(album_title.getAlbum(), position, item);
            }
        });
        rv_list.setAdapter(mAdapter);

        album_title.setOnLoadListener(new AlbumTitleBar.OnLoadListener() {
            @Override
            public void onLoad(String id, Cursor cursor) {
                if (pick(id, cursor)) {
                    return;
                }
                mAdapter.setCursor(cursor, true);
            }
        });

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
        album_title.onLoad();
    }

    private boolean pick(String id, Cursor cursor) {
        if (TextUtils.equals(Album.ALBUM_ID_ALL, id)
                && (cursor == null || cursor.getCount() <= 0)) {
            Utils.closeQuietly(cursor);
            startActivityForResult(IntentUtils.getPickIntent(AlbumActivity.this,
                    IntentUtils.MIME_TYPE_IMAGE,
                    true), REQUEST_CODE_PICK);
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (album_title.onBackPressed()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PREVIEW) {
            final List<Media> list = AlbumPreviewActivity.getSelectedExtra(data);
            final boolean isOrigin = AlbumPreviewActivity.getOriginExtra(data);
            onSelectedResult(list, isOrigin);
            if (resultCode == RESULT_OK) {
                confirm();
            }

        } else if (requestCode == REQUEST_CODE_PICK) {
            final List<Media> list = new ArrayList<>();
            if (resultCode == RESULT_OK) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN
                        && data != null && data.getClipData() != null
                        && data.getClipData().getItemCount() > 0) {
                    final ClipData clipData = data.getClipData();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        list.add(Media.valueOf(clipData.getItemAt(i).getUri()));
                    }
                } else if (data != null && data.getData() != null) {
                    final Uri uri = data.getData();
                    list.add(Media.valueOf(uri));
                }
            }
            onSelectedResult(list, false);
            if (resultCode == RESULT_OK) {
                confirm();
            } else {
                finish();
            }
        }
    }

    private void onSelectedResult(List<Media> list, boolean isOrigin) {
        mAdapter.setSelected(list);
        album_title.setCount(list.size());
        album_bottom.setCount(list.size());
        album_bottom.setOrigin(isOrigin);
    }

    @Override
    public void finish() {
        CachePool.getInstance(getApplicationContext()).clear();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        album_title.onDestroy();
        mAdapter.setCursor(null, true);
        CachePool.getInstance(getApplicationContext()).clear();
        super.onDestroy();
    }
}
