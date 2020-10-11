package com.d.lib.album.mvp;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.lang.ref.WeakReference;

public class MvpBasePresenter<V extends MvpBaseView> {
    protected Context mContext;
    private WeakReference<V> mViewRef;

    public MvpBasePresenter(Context context) {
        if (context instanceof Application) {
            this.mContext = context;
        } else {
            throw new IllegalArgumentException("Context must be ApplicationContext");
        }
    }

    @UiThread
    public void attachView(V view) {
        mViewRef = new WeakReference<V>(view);
    }

    /**
     * Get the attached view. You should always call {@link #isViewAttached()} to check if the view
     * is
     * attached to avoid NullPointerExceptions.
     *
     * @return <code>null</code>, if view is not attached, otherwise the concrete view instance
     */
    @UiThread
    @Nullable
    public V getView() {
        return mViewRef == null ? null : mViewRef.get();
    }

    /**
     * Checks if a view is attached to this presenter. You should always call this method before
     * calling {@link #getView()} to get the view instance.
     */
    @UiThread
    public boolean isViewAttached() {
        return mViewRef != null && mViewRef.get() != null;
    }

    @UiThread
    public void detachView(boolean retainInstance) {
        if (mViewRef != null) {
            mViewRef.clear();
            mViewRef = null;
        }
    }
}
