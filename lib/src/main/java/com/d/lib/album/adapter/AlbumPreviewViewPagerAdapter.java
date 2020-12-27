package com.d.lib.album.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.d.lib.album.R;
import com.d.lib.album.model.Media;
import com.d.lib.album.util.CachePool;

import java.io.File;
import java.util.List;

/**
 * AlbumPreviewViewPagerAdapter
 * Created by D on 2020/10/10.
 */
public class AlbumPreviewViewPagerAdapter extends CommonPagerAdapter<Media>
        implements AlbumPreviewPagerAdapter {

    public AlbumPreviewViewPagerAdapter(@NonNull Context context, List<Media> datas) {
        super(context, datas, R.layout.lib_album_adapter_preview);
    }

    @Override
    public Media get(int position) {
        return mDatas.get(position);
    }

    @Override
    public int index(Media item) {
        return mDatas.indexOf(item);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public void convert(int position, CommonHolder holder, Media item) {
        final File cache = CachePool.getInstance(mContext).get(item.uri);
        Glide.with(mContext).load(cache != null ? cache : item.uri)
                .apply(new RequestOptions()
                        .error(R.color.lib_album_color_black)
                        .dontAnimate())
                .into((ImageView) holder.getView(R.id.iv_image));
    }
}
