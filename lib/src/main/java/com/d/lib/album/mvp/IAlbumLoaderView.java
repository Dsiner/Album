package com.d.lib.album.mvp;

import android.database.Cursor;

/**
 * IAlbumLoaderView
 * Created by D on 2020/10/10.
 */
public interface IAlbumLoaderView extends MvpBaseView {
    void onLoadAlbumSuccess(Cursor cursor);

    void onLoadAlbumError(Throwable e);

    void onLoadMediaSuccess(String id, Cursor cursor);
}
