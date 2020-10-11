package com.d.lib.album.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContentResolverCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v4.os.OperationCanceledException;

/**
 * CursorLoaderCompat
 * Created by D on 2020/10/11.
 **/
public class CursorLoaderCompat {
    protected final Context mContext;
    protected final Uri mUri;
    protected final String[] mProjection;
    protected final String mSelection;
    protected final String[] mSelectionArgs;
    protected final String mSortOrder;

    protected volatile Cursor mCursor;
    protected volatile CancellationSignal mCancellationSignal;

    public CursorLoaderCompat(@NonNull Context context, @NonNull Uri uri, @Nullable String[] projection,
                              @Nullable String selection, @Nullable String[] selectionArgs,
                              @Nullable String sortOrder) {
        mContext = context.getApplicationContext();
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
    }

    public Cursor loadInBackground() {
        synchronized (this) {
            if (mCancellationSignal != null) {
                throw new OperationCanceledException();
            }
            mCancellationSignal = new CancellationSignal();
        }
        try {
            Cursor cursor = ContentResolverCompat.query(mContext.getContentResolver(),
                    mUri, mProjection, mSelection, mSelectionArgs, mSortOrder,
                    mCancellationSignal);
            if (cursor != null) {
                try {
                    // Ensure the cursor window is filled.
                    cursor.getCount();
                } catch (RuntimeException ex) {
                    cursor.close();
                    throw ex;
                }
            }
            mCursor = cursor;
            return cursor;
        } finally {
            synchronized (this) {
                mCancellationSignal = null;
            }
        }
    }

    public void cancelLoadInBackground() {
        synchronized (this) {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
        }
    }

    public void onCanceled() {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
    }

    public interface Callback {
        void onSuccess(Cursor cursor);

        void onError(Throwable e);
    }
}
