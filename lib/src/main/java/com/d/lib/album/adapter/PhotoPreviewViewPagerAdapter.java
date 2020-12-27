package com.d.lib.album.adapter;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.d.lib.album.R;

import java.util.List;

/**
 * PhotoPreviewViewPagerAdapter
 * Created by D on 2020/10/10.
 */
public class PhotoPreviewViewPagerAdapter extends CommonPagerAdapter<String> {

    public PhotoPreviewViewPagerAdapter(@NonNull Context context, List<String> datas) {
        super(context, datas, R.layout.lib_album_adapter_preview);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public void convert(int position, CommonHolder holder, String item) {
        Glide.with(mContext).load(item)
                .apply(new RequestOptions()
                        .error(R.color.lib_album_color_black)
                        .dontAnimate())
                .into((ImageView) holder.getView(R.id.iv_image));
    }
}
