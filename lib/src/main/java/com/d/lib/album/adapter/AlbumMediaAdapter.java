package com.d.lib.album.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.d.lib.album.R;
import com.d.lib.album.activity.AlbumActivity;
import com.d.lib.album.activity.CaptureActivity;
import com.d.lib.album.model.Media;
import com.d.lib.album.model.SelectList;
import com.d.lib.album.util.CachePool;

import java.io.File;
import java.util.List;

/**
 * AlbumMediaAdapter
 * Created by D on 2017/4/26.
 */
public class AlbumMediaAdapter extends CommonCursorAdapter {
    private final SelectList<Media> mSelectList;
    private OnClickListener mOnClickListener;

    public AlbumMediaAdapter(Context context, final int maxSelectable, final boolean captureEnable) {
        super(context, new MultiItemTypeSupport<Cursor>() {
            final int ITEM_ID_CAPTURE = (int) Media.ITEM_ID_CAPTURE;

            @Override
            public int getItemViewType(int position, Cursor item) {
                return captureEnable && position == 0 ? ITEM_ID_CAPTURE : 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return viewType == ITEM_ID_CAPTURE
                        ? R.layout.lib_album_adapter_capture
                        : R.layout.lib_album_adapter_image;
            }
        });
        mSelectList = new SelectList<>(maxSelectable);
    }

    public SelectList<Media> getSelected() {
        return mSelectList;
    }

    public void setSelected(List<Media> list) {
        if (list != null) {
            mSelectList.clear();
            mSelectList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @Override
    public void convert(int position, CommonHolder holder, Cursor item) {
        final int resId = holder.layoutId;
        if (R.layout.lib_album_adapter_capture == resId) {
            convertCapture(position, holder, item);

        } else if (R.layout.lib_album_adapter_image == resId) {
            convertImage(position, holder, item);
        }
    }

    private void convertCapture(final int position, final CommonHolder holder, final Cursor item) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CaptureActivity.openActivityForResult((Activity) mContext,
                        AlbumActivity.REQUEST_CODE_CAPTURE);
            }
        });
    }

    private void convertImage(final int position, final CommonHolder holder, final Cursor cursor) {
        final Media media = Media.valueOf(cursor);
        final boolean selected = mSelectList.contains(media);
        holder.setBackgroundResource(R.id.v_frame, selected
                ? R.color.lib_album_color_translucent
                : R.color.lib_album_color_trans);
        selected(holder, media, selected);

        final File cache = CachePool.getInstance(mContext).get(media.uri);
        holder.setVisibility(R.id.iv_label, cache != null ? View.VISIBLE : View.GONE);
        Glide.with(mContext).load(cache != null ? cache : media.uri)
                .apply(new RequestOptions()
                        .error(R.color.lib_album_color_black)
                        .dontAnimate())
                .into((ImageView) holder.getView(R.id.iv_thumbnail));

        holder.setOnClickListener(R.id.layout_label, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected) {
                    mSelectList.remove(media);
                } else {
                    if (!mSelectList.add(media)) {
                        Toast.makeText(mContext,
                                mContext.getString(R.string.lib_album_choose_limit_tips,
                                        mSelectList.getMaxCount()),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }
                notifyDataSetChanged();
                if (mOnClickListener != null) {
                    mOnClickListener.onCount(position, media, mSelectList.size());
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(position, media);
                }
            }
        });
    }

    private void selected(CommonHolder holder, Media item, boolean selected) {
        if (selected) {
            holder.setBackgroundResource(R.id.tv_label, R.drawable.lib_album_corner_circle_checked);
            holder.setText(R.id.tv_label, "" + (mSelectList.indexOf(item) + 1));

        } else {
            holder.setBackgroundResource(R.id.tv_label, R.drawable.lib_album_corner_circle_unchecked);
            holder.setText(R.id.tv_label, "");
        }
    }

    public void setOnClickListener(OnClickListener l) {
        this.mOnClickListener = l;
    }

    public interface OnClickListener {
        void onCount(int position, Media item, int count);

        void onClick(int position, Media item);
    }
}
