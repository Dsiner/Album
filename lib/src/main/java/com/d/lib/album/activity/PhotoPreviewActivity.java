package com.d.lib.album.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.d.lib.album.R;
import com.d.lib.album.adapter.PhotoPreviewViewPagerAdapter;
import com.d.lib.album.util.Utils;
import com.d.lib.album.widget.IndicatorLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * PhotoPreviewActivity
 * Created by D on 2017/4/26.
 */
public class PhotoPreviewActivity extends FragmentActivity
        implements ViewPager.OnPageChangeListener {
    public static final String EXTRA_URLS = "EXTRA_URLS";
    public static final String EXTRA_POSITION = "EXTRA_POSITION";

    protected IndicatorLayout indicator;
    protected ViewPager mViewPager;
    protected PagerAdapter mPagerAdapter;

    protected List<String> mUrls;
    protected int mPosition;

    public static void openActivity(Context context,
                                    @NonNull List<String> urls,
                                    int position) {
        if (context == null || urls == null || urls.size() <= 0) {
            return;
        }
        Intent intent = new Intent(context, PhotoPreviewActivity.class);
        intent.putStringArrayListExtra(EXTRA_URLS, new ArrayList<>(urls));
        intent.putExtra(EXTRA_POSITION, position);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lib_album_activity_photo_preview);
        bindView();
        init();
    }

    private void bindView() {
        mViewPager = findViewById(R.id.vp_pager);
        indicator = findViewById(R.id.indicator);
    }

    private void init() {
        mUrls = getIntent().getStringArrayListExtra(EXTRA_URLS);
        mPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
        onLoad();
    }

    private void onLoad() {
        mPagerAdapter = new PhotoPreviewViewPagerAdapter(this, mUrls);
        setAdapter(mPagerAdapter);
    }

    private void setAdapter(PagerAdapter adapter) {
        final int count = adapter.getCount();
        mPosition = Math.min(count - 1, mPosition);
        mPosition = Math.max(0, mPosition);
        mViewPager.setOffscreenPageLimit(Math.min(4, count - 1));
        mViewPager.removeOnPageChangeListener(this);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(mPosition, false);
        indicator.setIndicatorResources(new int[]{R.drawable.lib_album_indicator,
                R.drawable.lib_album_indicator_foucus})
                .setPointPadding(Utils.dp2px(this, 4))
                .setCount(count)
                .setCurrentItem(mPosition);
        indicator.setVisibility(count > 1 ? View.VISIBLE : View.INVISIBLE);

        onPageSelected(mPosition);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        indicator.setCurrentItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
