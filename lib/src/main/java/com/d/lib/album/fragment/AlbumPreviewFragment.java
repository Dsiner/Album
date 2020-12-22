package com.d.lib.album.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.d.lib.album.R;
import com.d.lib.album.model.Media;

/**
 * AlbumPreviewFragment
 * Created by D on 2020/10/11.
 */
@Deprecated
public class AlbumPreviewFragment extends LazyLoaderFragment {

    public static final String EXTRA_MEDIA = "EXTRA_MEDIA";

    private ImageView iv_image;
    private Media mMedia;

    public static AlbumPreviewFragment create(Media media) {
        AlbumPreviewFragment fragment = new AlbumPreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_MEDIA, media);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.lib_album_adapter_preview;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mMedia = getArguments() != null
                ? (Media) getArguments().getParcelable(EXTRA_MEDIA)
                : null;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void bindView(View rootView) {
        super.bindView(rootView);
        iv_image = rootView.findViewById(R.id.iv_image);
    }

    @Override
    protected void init() {
        super.init();
        Log.d("Album", "getData: " + mMedia.uri);
        Glide.with(mContext).load(mMedia.uri)
                .apply(new RequestOptions().dontAnimate())
                .into(iv_image);
    }

    public Media getMedia() {
        return mMedia;
    }

    @Override
    protected void initList() {

    }

    @Override
    protected void getData() {

    }
}
