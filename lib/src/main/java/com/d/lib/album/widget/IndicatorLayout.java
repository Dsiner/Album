package com.d.lib.album.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.d.lib.album.util.Utils;

/**
 * IndicatorLayout
 * Created by D on 2020/10/11.
 **/
public class IndicatorLayout extends LinearLayout {
    private int[] mIndicatorIds;
    private int mPointPadding;
    private int mCurPosition;

    public IndicatorLayout(Context context) {
        this(context, null);
    }

    public IndicatorLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mPointPadding = Utils.dp2px(context, 2.5f);
    }

    public IndicatorLayout setIndicatorResources(@DrawableRes int[] indicatorIds) {
        this.mIndicatorIds = indicatorIds;
        return this;
    }

    public IndicatorLayout setPointPadding(int pointPadding) {
        this.mPointPadding = pointPadding;
        return this;
    }

    public IndicatorLayout setCount(int count) {
        removeAllViews();
        if (count <= 0) {
            return this;
        }
        for (int i = 0; i < count; i++) {
            ImageView pointView = new ImageView(getContext());
            pointView.setPadding(mPointPadding, 0, mPointPadding, 0);
            pointView.setImageResource(i == mCurPosition ? mIndicatorIds[1] : mIndicatorIds[0]);
            addView(pointView);
        }
        return this;
    }

    public void setCurrentItem(int position) {
        if (mCurPosition == position) {
            return;
        }
        mCurPosition = position;
        final int count = getChildCount();
        if (getChildCount() <= 0 || position < 0 || position > count - 1) {
            return;
        }
        for (int i = 0; i < count; i++) {
            ImageView pointView = (ImageView) getChildAt(i);
            pointView.setImageResource(i == position ? mIndicatorIds[1] : mIndicatorIds[0]);
        }
    }
}
