package com.d.lib.album.mvp;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.d.lib.album.loader.AlbumLoader;
import com.d.lib.album.loader.AlbumMediaLoader;
import com.d.lib.album.loader.CursorLoaderCompat;
import com.d.lib.album.util.Utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AlbumLoaderPresenter
 * Created by D on 2017/4/26.
 */
public class AlbumLoaderPresenter extends MvpBasePresenter<IAlbumLoaderView> {
    @Deprecated
    private final LinkedHashMap<String, Cursor> mHashMap = new LinkedHashMap<>();

    public AlbumLoaderPresenter(Context context) {
        super(context);
    }

    public void loadAlbum() {
        AlbumLoader.load(mContext, new CursorLoaderCompat.Callback() {
            @Override
            public void onSuccess(Cursor cursor) {
                Log.d("Album", "loadAlbum onSuccess");
                if (getView() == null) {
                    Utils.closeQuietly(cursor);
                    return;
                }
                getView().onLoadAlbumSuccess(cursor);
            }

            @Override
            public void onError(Throwable e) {
                Log.d("Album", "loadAlbum onError" + e);
                if (getView() == null) {
                    return;
                }
                getView().onLoadAlbumError(e);
            }
        });
    }

    public void loadAlbumMedia(final String albumId) {
        if (getView() == null) {
            return;
        }
        Log.d("Album", "onLoad: " + albumId);
        AlbumMediaLoader.load(mContext, albumId, new CursorLoaderCompat.Callback() {
            @Override
            public void onSuccess(Cursor cursor) {
                Log.d("Album", "loadAlbumMedia onSuccess");
                if (getView() == null) {
                    Utils.closeQuietly(cursor);
                    return;
                }
                getView().onLoadMediaSuccess(albumId, cursor);
            }

            @Override
            public void onError(Throwable e) {
                Log.d("Album", "loadAlbumMedia onError" + e);
                if (getView() == null) {
                    return;
                }
                getView().onLoadMediaSuccess(albumId, null);
            }
        });
    }

    @Deprecated
    private boolean cached(String albumId) {
        final Cursor cache = mHashMap.get(albumId);
        if (cache != null) {
            mHashMap.put(albumId, cache);
            getView().onLoadMediaSuccess(albumId, cache);
            return true;
        }
        if (mHashMap.containsKey(albumId)) {
            // Loading
            return true;
        }
        mHashMap.put(albumId, null);
        return false;
    }

    @Deprecated
    private void close(String albumId) {
        final Cursor cache = mHashMap.get(albumId);
        if (cache != null) {
            Utils.closeQuietly(cache);
        }
    }

    @Deprecated
    private void closeAll() {
        for (Map.Entry<String, Cursor> entry : mHashMap.entrySet()) {
            Utils.closeQuietly(entry.getValue());
        }
        mHashMap.clear();
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
    }
}
