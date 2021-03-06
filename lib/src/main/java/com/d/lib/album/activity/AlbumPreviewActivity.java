package com.d.lib.album.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.d.lib.album.R;
import com.d.lib.album.adapter.AlbumPreviewCursorPagerAdapter;
import com.d.lib.album.adapter.AlbumPreviewPagerAdapter;
import com.d.lib.album.adapter.AlbumPreviewSelectedAdapter;
import com.d.lib.album.adapter.AlbumPreviewViewPagerAdapter;
import com.d.lib.album.model.Album;
import com.d.lib.album.model.Media;
import com.d.lib.album.model.SelectList;
import com.d.lib.album.mvp.AlbumLoaderPresenter;
import com.d.lib.album.mvp.IAlbumLoaderView;
import com.d.lib.album.widget.AlbumBottomBar;
import com.d.lib.album.widget.AlbumTitleBar;

import java.util.ArrayList;
import java.util.List;

/**
 * AlbumPreviewActivity
 * Created by D on 2017/4/26.
 */
public class AlbumPreviewActivity extends FragmentActivity
        implements View.OnClickListener, ViewPager.OnPageChangeListener, IAlbumLoaderView {

    public static final int REQUEST_CODE_EDIT = 1001;
    public static final int RESULT_CONFIRM = 9;

    public static final String EXTRA_ALBUM = "EXTRA_ALBUM";
    public static final String EXTRA_SELECTS = "EXTRA_SELECTS";
    public static final String EXTRA_BUNDLE = "EXTRA_BUNDLE";
    public static final String EXTRA_BUNDLE_ORIGIN_ENABLE = "EXTRA_BUNDLE_ORIGIN_ENABLE";
    public static final String EXTRA_BUNDLE_ORIGIN_CHECKED = "EXTRA_BUNDLE_ORIGIN_CHECKED";
    public static final String EXTRA_BUNDLE_MAX_SELECTABLE = "EXTRA_BUNDLE_MAX_SELECTABLE";
    public static final String EXTRA_BUNDLE_POSITION = "EXTRA_BUNDLE_POSITION";
    public static final String EXTRA_BUNDLE_ITEM = "EXTRA_BUNDLE_ITEM";

    private ViewPager mViewPager;
    private RecyclerView rv_preview_selected_list;
    private AlbumTitleBar album_title;
    private AlbumBottomBar album_bottom;
    private AlbumPreviewPagerAdapter mPagerAdapter;
    private AlbumPreviewSelectedAdapter mPreviewSelectedAdapter;

    private Album mAlbum;
    private List<Media> mSelects;
    private Bundle mBundle;
    private int mCurPosition;
    private Media mCurItem;
    private AlbumLoaderPresenter mPresenter;
    private boolean mIsOk;

    static void openActivityForResult(Activity activity,
                                      @Nullable Album album,
                                      @NonNull List<Media> selects,
                                      int requestCode,
                                      @Nullable Bundle... bundle) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, AlbumPreviewActivity.class);
        intent.putExtra(EXTRA_ALBUM, album);
        intent.putParcelableArrayListExtra(EXTRA_SELECTS, new ArrayList<>(selects));
        if (bundle != null && bundle.length > 0) {
            intent.putExtra(EXTRA_BUNDLE, bundle[0]);
        }
        if (!(activity instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivityForResult(intent, requestCode);
    }

    public static Bundle getBundle(boolean originEnable,
                                   boolean originChecked,
                                   int maxSelectable,
                                   int position,
                                   Media item) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_BUNDLE_ORIGIN_ENABLE, originEnable);
        bundle.putBoolean(EXTRA_BUNDLE_ORIGIN_CHECKED, originChecked);
        bundle.putInt(EXTRA_BUNDLE_MAX_SELECTABLE, maxSelectable);
        bundle.putInt(EXTRA_BUNDLE_POSITION, position);
        bundle.putParcelable(EXTRA_BUNDLE_ITEM, item);
        return bundle;
    }

    @NonNull
    public static List<Media> getSelectedExtra(Intent data) {
        if (data == null) {
            return new ArrayList<>();
        }
        List<Media> list = data.getParcelableArrayListExtra(EXTRA_SELECTS);
        return list != null ? list : new ArrayList<Media>();
    }

    @NonNull
    public static boolean getOriginExtra(Intent data) {
        if (data == null) {
            return false;
        }
        Bundle bundle = data.getBundleExtra(EXTRA_BUNDLE);
        bundle = bundle != null ? bundle : new Bundle();
        return bundle.getBoolean(EXTRA_BUNDLE_ORIGIN_CHECKED, false);
    }

    private Intent getResultIntent() {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(EXTRA_SELECTS,
                new ArrayList<>(mPreviewSelectedAdapter.getSelected()));
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_BUNDLE_ORIGIN_CHECKED, album_bottom.isOriginChecked());
        intent.putExtra(EXTRA_BUNDLE, bundle);
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

        } else if (R.id.tv_title_right == resId) {
            confirm();

        } else if (R.id.tv_bottom_edit == resId) {
            AlbumEditActivity.openActivityForResult(AlbumPreviewActivity.this,
                    album_bottom.getMedia().uri, REQUEST_CODE_EDIT);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lib_album_activity_album_preview);
        bindView();
        init();
    }

    private void bindView() {
        mViewPager = findViewById(R.id.vp_pager);
        rv_preview_selected_list = findViewById(R.id.rv_preview_selected_list);
        album_title = findViewById(R.id.album_title);
        album_bottom = findViewById(R.id.album_bottom);

        album_title.findViewById(R.id.iv_title_left).setOnClickListener(this);
        album_title.findViewById(R.id.tv_title_right).setOnClickListener(this);
        album_bottom.findViewById(R.id.tv_bottom_edit).setOnClickListener(this);
    }

    private void init() {
        mAlbum = getIntent().getParcelableExtra(EXTRA_ALBUM);
        mSelects = getIntent().getParcelableArrayListExtra(EXTRA_SELECTS);
        mBundle = getIntent().getBundleExtra(EXTRA_BUNDLE);
        mBundle = mBundle != null ? mBundle : new Bundle();
        mCurPosition = mBundle.getInt(EXTRA_BUNDLE_POSITION, 0);
        mCurItem = mBundle.getParcelable(EXTRA_BUNDLE_ITEM);
        final int maxSelectable = mBundle.getInt(EXTRA_BUNDLE_MAX_SELECTABLE,
                SelectList.MAX_COUNT);

        mPresenter = new AlbumLoaderPresenter(getApplicationContext());
        mPresenter.attachView(this);

        album_title.setType(AlbumTitleBar.TYPE_PREVIEW);
        album_title.setMaxSelectable(maxSelectable);
        album_bottom.setType(AlbumBottomBar.TYPE_PREVIEW);
        album_bottom.setOriginEnable(mBundle.getBoolean(EXTRA_BUNDLE_ORIGIN_ENABLE, false));
        album_bottom.setOriginChecked(mBundle.getBoolean(EXTRA_BUNDLE_ORIGIN_CHECKED, false));
        album_bottom.setOnBottomListener(new AlbumBottomBar.OnBottomListener() {
            @Override
            public void onOrigin(boolean check) {

            }

            @Override
            public boolean onCheck(Media item, boolean check) {
                boolean changed = mPreviewSelectedAdapter.setSelected(item, check);
                onSelectedChange();
                return changed;
            }
        });

        mPreviewSelectedAdapter = new AlbumPreviewSelectedAdapter(this,
                new ArrayList<Media>(), maxSelectable);
        mPreviewSelectedAdapter.setOnClickListener(new AlbumPreviewSelectedAdapter.OnClickListener() {
            @Override
            public void onClick(int position, Media item) {
                int index = mPagerAdapter.index(item);
                if (index != -1) {
                    mViewPager.setCurrentItem(index, false);
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv_preview_selected_list.setLayoutManager(layoutManager);
        rv_preview_selected_list.setAdapter(mPreviewSelectedAdapter);

        onLoad();
    }

    private void onLoad() {
        if (mAlbum == null) {
            mPreviewSelectedAdapter.setType(AlbumPreviewSelectedAdapter.TYPE_PREVIEW);
            mPagerAdapter = new AlbumPreviewViewPagerAdapter(this, mSelects);
            setAdapter(mPagerAdapter);

        } else {
            mPreviewSelectedAdapter.setType(AlbumPreviewSelectedAdapter.TYPE_ALBUM);
            mPresenter.loadAlbumMedia(mAlbum.id);
        }
    }

    private void setAdapter(final AlbumPreviewPagerAdapter adapter) {
        mCurPosition = Math.min(adapter.getCount() - 1, mCurPosition);
        mCurPosition = Math.max(0, mCurPosition);
        mViewPager.setOffscreenPageLimit(Math.min(4, adapter.getCount() - 1));
        mViewPager.removeOnPageChangeListener(this);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAdapter((PagerAdapter) adapter);

        if (!adapter.get(mCurPosition).equals(mCurItem)) {
            int index = mPagerAdapter.index(mCurItem);
            mCurPosition = index != -1 ? index : 0;
        }
        mViewPager.setCurrentItem(mCurPosition, false);

        mPreviewSelectedAdapter.setDatas(mSelects);
        mPreviewSelectedAdapter.notifyDataSetChanged();

        onPageSelected(mCurPosition);
    }

    private void onSelectedChange() {
        List<Media> list = mPreviewSelectedAdapter.getDatas();
        int count = list.size();
        int index = list.indexOf(album_bottom.getMedia());
        album_title.setCount(mPreviewSelectedAdapter.getSelectedItemCount());
        rv_preview_selected_list.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        if (index != -1) {
            rv_preview_selected_list.scrollToPosition(index);
        }
    }

    @Override
    public void onLoadAlbumSuccess(Cursor cursor) {

    }

    @Override
    public void onLoadAlbumError(Throwable e) {

    }

    @Override
    public void onLoadMediaSuccess(String id, Cursor cursor) {
        mPagerAdapter = new AlbumPreviewCursorPagerAdapter(this);
        ((AlbumPreviewCursorPagerAdapter) mPagerAdapter).setCursor(cursor, true);
        setAdapter(mPagerAdapter);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(final int position) {
        final Media media = mPagerAdapter.get(position);
        album_title.setTitle("" + (position + 1) + "/" + mPagerAdapter.getCount());
        album_bottom.setMedia(media);
        album_bottom.setChecked(mPreviewSelectedAdapter.isSelected(media));
        mPreviewSelectedAdapter.setCurrent(media);
        onSelectedChange();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mPagerAdapter != null) {
            mPagerAdapter.notifyDataSetChanged();
        }
        if (mPreviewSelectedAdapter != null) {
            mPreviewSelectedAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void finish() {
        setResult(mIsOk ? RESULT_OK : RESULT_CANCELED, getResultIntent());
        super.finish();
    }

    @Override
    protected void onDestroy() {
        if (mPagerAdapter instanceof AlbumPreviewCursorPagerAdapter) {
            ((AlbumPreviewCursorPagerAdapter) mPagerAdapter).setCursor(null, true);
        }
        mPresenter.detachView(false);
        super.onDestroy();
    }
}
