package com.d.lib.album.adapter;

import com.d.lib.album.model.Media;

/**
 * AlbumPreviewPagerAdapter
 * Created by D on 2020/10/10.
 */
public interface AlbumPreviewPagerAdapter {
    int getCount();

    void notifyDataSetChanged();

    Media get(int position);

    int index(Media item);
}
