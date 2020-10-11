package com.d.lib.album.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.d.lib.album.R;
import com.d.lib.album.model.Album;

/**
 * AlbumAdapter
 * Created by D on 2017/4/26.
 */
public class AlbumAdapter extends CommonCursorAdapter {

    private OnClickListener mOnClickListener;

    public AlbumAdapter(Context context) {
        super(context, R.layout.lib_album_adapter_album);
    }

    @Override
    public void convert(int position, CommonHolder holder, Cursor cursor) {
        final Album item = Album.valueOf(cursor);
        holder.setText(R.id.tv_album_name, item.getDisplayName(mContext));
        holder.setText(R.id.tv_album_count, "(" + item.count + ")");
        Glide.with(mContext).load(item.coverUri)
                .apply(new RequestOptions()
                        .error(R.color.lib_album_color_black)
                        .dontAnimate())
                .into((ImageView) holder.getView(R.id.iv_album_thumbnail));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(item);
                }
            }
        });
    }

    public void setOnClickListener(OnClickListener l) {
        this.mOnClickListener = l;
    }

    public interface OnClickListener {
        void onClick(Album item);
    }
}
