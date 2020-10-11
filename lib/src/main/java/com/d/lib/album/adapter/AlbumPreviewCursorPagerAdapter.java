package com.d.lib.album.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.d.lib.album.R;
import com.d.lib.album.model.Media;
import com.d.lib.album.util.CachePool;

import java.io.File;

/**
 * AlbumPreviewCursorPagerAdapter
 * Created by D on 2020/10/10.
 */
public class AlbumPreviewCursorPagerAdapter extends CommonCursorPagerAdapter
        implements AlbumPreviewPagerAdapter {

    public AlbumPreviewCursorPagerAdapter(@NonNull Context context) {
        super(context, R.layout.lib_album_adapter_preview);
    }

    @Override
    public Media get(int position) {
        if (!isDataValid(mCursor)) {
            throw new IllegalStateException("Cannot bind view holder when cursor is in invalid state.");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Could not move cursor to position " + position
                    + " when trying to bind view holder");
        }
        final Media media = Media.valueOf(mCursor);
        return media;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int index(Media o) {
        if (!isDataValid(mCursor)) {
            throw new IllegalStateException("Cannot index when cursor is in invalid state.");
        }
        if (mCursor.moveToFirst()) {
            int index = 0;
            do {
                final Media media = Media.valueOf(mCursor);
                if (media.equals(o)) {
                    return index;
                }
                index++;
            } while (mCursor.moveToNext());
        }
        return -1;
    }

    @Override
    public void convert(int position, CommonHolder holder, Cursor cursor) {
        final Media media = Media.valueOf(cursor);

        final File cache = CachePool.getInstance(mContext).get(media.uri);
        Glide.with(mContext).load(cache != null ? cache : media.uri)
                .apply(new RequestOptions()
                        .error(R.color.lib_album_color_black)
                        .dontAnimate())
                .into((ImageView) holder.getView(R.id.iv_image));
    }
}
