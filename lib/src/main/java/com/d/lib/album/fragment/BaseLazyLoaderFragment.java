package com.d.lib.album.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Auto-Loader - ViewPager Fragment
 * Created by D on 2017/8/23.
 */
abstract class BaseLazyLoaderFragment extends Fragment {
    protected Context mContext;
    protected Activity mActivity;
    protected View mRootView;
    protected boolean mIsVisibleToUser;
    protected boolean mIsLazyLoaded;
    protected boolean mIsPrepared;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            int layoutRes = getLayoutRes();
            mRootView = getActivity().getLayoutInflater().inflate(layoutRes, null);
            bindView(mRootView);
            mIsPrepared = true;
            init();
        } else {
            if (mRootView.getParent() != null) {
                ((ViewGroup) mRootView.getParent()).removeView(mRootView);
            }
            bindView(mRootView);
        }
        return mRootView;
    }

    protected void init() {

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        onVisible();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            this.mIsVisibleToUser = true;
            onVisible();
        } else {
            this.mIsVisibleToUser = false;
            onInvisible();
        }
    }

    protected void onVisible() {
        if (mIsLazyLoaded || !mIsPrepared || !mIsVisibleToUser) {
            return;
        }
        mIsLazyLoaded = true; // Just lazy loading once
        getData();
    }

    protected void onInvisible() {

    }

    /**
     * Return the layout resource like R.layout.my_layout
     *
     * @return the layout resource or zero ("0"), if you don't want to have an UI
     */
    protected abstract int getLayoutRes();

    protected void bindView(View rootView) {
    }

    protected abstract void initList();

    protected abstract void getData();
}
