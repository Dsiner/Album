package com.d.lib.album.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.d.lib.album.R;
import com.d.lib.album.model.Media;
import com.d.lib.album.model.SelectList;
import com.d.lib.album.util.CachePool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * PreviewSelectedAdapter
 * Created by D on 2017/4/26.
 */
public class AlbumPreviewSelectedAdapter extends CommonCheckAdapter<Media> {
    public static final int TYPE_ALBUM = 0;
    public static final int TYPE_PREVIEW = 1;

    private int mType = TYPE_ALBUM;
    private Media mMedia;
    private OnClickListener mOnClickListener;

    public AlbumPreviewSelectedAdapter(Context context, List<Media> datas) {
        super(context, datas, R.layout.lib_album_adapter_preview_select);
    }

    public void setType(int type) {
        mType = type;
    }

    @Override
    public void setDatas(List<Media> datas) {
        super.setDatas(datas);
        selectAll();
    }

    public void setCurrent(Media item) {
        mMedia = item;
        notifyDataSetChanged();
    }

    public boolean isSelected(Media item) {
        if (TYPE_ALBUM == mType) {
            return mDatas.contains(item);

        } else {
            int index = mDatas.indexOf(item);
            return index != -1 && isSelected(index);
        }
    }

    public boolean setSelected(Media item, boolean check) {
        if (TYPE_ALBUM == mType) {
            if (check) {
                if (mDatas.size() >= SelectList.MAX_COUNT) {
                    Toast.makeText(mContext,
                            mContext.getString(R.string.lib_album_choose_limit_tips),
                            Toast.LENGTH_SHORT)
                            .show();
                    return false;
                }
                if (!mDatas.contains(item)) {
                    mDatas.add(item);
                }
            } else {
                mDatas.remove(item);
            }

        } else {
            int index = mDatas.indexOf(item);
            if (check) {
                addSelection(index);
            } else {
                removeSelection(index);
            }
        }
        notifyDataSetChanged();
        return true;
    }

    @NonNull
    @Override
    public List<Media> getSelected() {
        List<Media> list = new ArrayList<>();
        if (TYPE_ALBUM == mType) {
            list.addAll(getDatas());

        } else {
            for (int count = mDatas.size(), i = 0; i < count; i++) {
                if (isSelected(i)) {
                    list.add(mDatas.get(i));
                }
            }
        }
        return list;
    }

    @Override
    public int getSelectedItemCount() {
        if (TYPE_ALBUM == mType) {
            return getItemCount();

        } else {
            return super.getSelectedItemCount();
        }
    }

    @Override
    public void convert(final int position, CommonHolder holder, final Media item) {
        final boolean selected = isSelected(item);
        holder.setBackgroundResource(R.id.v_frame, selected
                ? (item.equals(mMedia)
                ? R.drawable.lib_album_corner_frame
                : R.color.lib_album_color_trans)
                : R.color.lib_album_color_translucent);

        final File cache = CachePool.getInstance(mContext).get(item.uri);
        holder.setVisibility(R.id.iv_label, cache != null ? View.VISIBLE : View.GONE);
        Glide.with(mContext).load(cache != null ? cache : item.uri)
                .apply(new RequestOptions()
                        .error(R.color.lib_album_color_black)
                        .dontAnimate())
                .into((ImageView) holder.getView(R.id.iv_thumbnail));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(position, item);
                }
            }
        });
    }

    public void setOnClickListener(OnClickListener l) {
        this.mOnClickListener = l;
    }

    public interface OnClickListener {
        void onClick(int position, Media item);
    }
}
