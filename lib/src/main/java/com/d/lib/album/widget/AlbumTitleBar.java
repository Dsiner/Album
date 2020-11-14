package com.d.lib.album.widget;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.d.lib.album.R;
import com.d.lib.album.adapter.AlbumAdapter;
import com.d.lib.album.model.Album;
import com.d.lib.album.model.SelectList;
import com.d.lib.album.mvp.ILoadView;
import com.d.lib.album.mvp.LoadPresenter;
import com.d.lib.album.util.Utils;

/**
 * AlbumTitleBar
 * Created by D on 2020/10/11.
 **/
public class AlbumTitleBar extends LinearLayout implements ILoadView, View.OnClickListener {
    public static final int TYPE_ALBUM = 0;
    public static final int TYPE_PREVIEW = 1;

    private Context mContext;
    private View mRootView;
    private int mType;
    private int mMaxCount = SelectList.MAX_COUNT;
    private TextView tv_title_title, tv_title_right;
    private ImageView iv_title_left, iv_title_title;
    private View llyt_title_title_root, flyt_album_root;
    private RecyclerView rv_album_list;

    private AlbumAdapter mAdapter;
    private Album mAlbum = Album.createAll();
    private LoadPresenter mPresenter;
    private OnLoadListener mOnLoadListener;

    public AlbumTitleBar(Context context) {
        this(context, null);
    }

    public AlbumTitleBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlbumTitleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void onClick(View v) {
        int resId = v.getId();
        if (R.id.llyt_title_title_root == resId) {
            toggle();

        } else if (R.id.flyt_album_root == resId) {
            toggle();
        }
    }

    private void bindView() {
        llyt_title_title_root = findViewById(R.id.llyt_title_title_root);
        iv_title_left = findViewById(R.id.iv_title_left);
        tv_title_title = findViewById(R.id.tv_title_title);
        iv_title_title = findViewById(R.id.iv_title_title);
        tv_title_right = findViewById(R.id.tv_title_right);
        flyt_album_root = findViewById(R.id.flyt_album_root);
        rv_album_list = findViewById(R.id.rv_album_list);

        llyt_title_title_root.setOnClickListener(this);
        flyt_album_root.setOnClickListener(this);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        mContext = context;
        mRootView = LayoutInflater.from(context).inflate(R.layout.lib_album_layout_title, this);
        bindView();
        mPresenter = new LoadPresenter(context.getApplicationContext());
        mPresenter.attachView(this);

        mAdapter = new AlbumAdapter(context);
        mAdapter.setOnClickListener(new AlbumAdapter.OnClickListener() {

            @Override
            public void onClick(Album item) {
                toggle();
                if (TextUtils.equals(item.id, mAlbum.id)) {
                    return;
                }
                onMediaLoad(item);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv_album_list.setLayoutManager(layoutManager);
        rv_album_list.setAdapter(mAdapter);
    }

    @NonNull
    public Album getAlbum() {
        return mAlbum;
    }

    public void setType(int type) {
        if (TYPE_ALBUM == type) {
            iv_title_left.setImageResource(R.drawable.lib_album_ic_title_close);
            llyt_title_title_root.setBackgroundDrawable(ContextCompat.getDrawable(mContext,
                    R.drawable.lib_album_corner_choose));
            tv_title_title.setPadding(Utils.dp2px(mContext, 12),
                    Utils.dp2px(mContext, 4),
                    Utils.dp2px(mContext, 0),
                    Utils.dp2px(mContext, 4));
            iv_title_title.setVisibility(VISIBLE);

        } else if (TYPE_PREVIEW == type) {
            iv_title_left.setImageResource(R.drawable.lib_album_ic_title_back);
            llyt_title_title_root.setBackgroundDrawable(null);
            tv_title_title.setPadding(Utils.dp2px(mContext, 0),
                    Utils.dp2px(mContext, 4),
                    Utils.dp2px(mContext, 0),
                    Utils.dp2px(mContext, 4));
            iv_title_title.setVisibility(GONE);
        }
        mType = type;
    }

    public void setTitle(CharSequence text) {
        tv_title_title.setText(text);
    }

    public void setMaxCount(int count) {
        this.mMaxCount = count;
    }

    public void setCount(int count) {
        tv_title_right.setEnabled(count > 0);
        tv_title_right.setClickable(count > 0);
        tv_title_right.setText(count > 0
                ? getResources().getText(R.string.lib_album_send)
                + "(" + count + "/" + mMaxCount + ")"
                : getResources().getText(R.string.lib_album_send));
        tv_title_right.setBackgroundDrawable(ContextCompat.getDrawable(mContext, count > 0
                ? R.drawable.lib_album_corner_btn_main_normal
                : R.drawable.lib_album_corner_btn_main_disable));
        tv_title_right.setTextColor(ContextCompat.getColor(mContext, count > 0
                ? R.color.lib_album_color_text
                : R.color.lib_album_color_text_disable));
    }

    public void onLoad() {
        mPresenter.loadAlbum();
        onMediaLoad(Album.createAll());
    }

    private void onMediaLoad(Album album) {
        mAlbum = album;
        mPresenter.loadAlbumMedia(album.id);
    }

    private boolean isOpen() {
        return flyt_album_root.getVisibility() == VISIBLE;
    }

    private void toggle() {
        final boolean open = !isOpen();
        notifyDataSetChanged(open);
    }

    private void notifyDataSetChanged(boolean open) {
        tv_title_title.setText(mAlbum.getDisplayName(mContext));
        iv_title_title.setRotation(open ? 90 : 270);
        flyt_album_root.setVisibility(open ? VISIBLE : GONE);
    }

    @Override
    public void onLoadAlbumSuccess(Cursor cursor) {
        mAdapter.setCursor(cursor, true);
    }

    @Override
    public void onLoadAlbumError(Throwable e) {
        mAdapter.setCursor(null, true);
    }

    @Override
    public void onLoadMediaSuccess(String id, Cursor cursor) {
        if (!TextUtils.equals(getAlbum().id, id) || mOnLoadListener == null) {
            Utils.closeQuietly(cursor);
            return;
        }
        mOnLoadListener.onLoad(id, cursor);
    }

    public boolean onBackPressed() {
        if (isOpen()) {
            toggle();
            return true;
        }
        return false;
    }

    private boolean isFinished(AlbumTitleBar view) {
        return view == null || view.getContext() == null
                || view.getContext() instanceof Activity
                && ((Activity) view.getContext()).isFinishing();
    }

    public void onDestroy() {
        mAdapter.setCursor(null, true);
        mPresenter.detachView(false);
    }

    public void setOnLoadListener(OnLoadListener l) {
        this.mOnLoadListener = l;
    }

    public interface OnLoadListener {
        void onLoad(String id, Cursor cursor);
    }
}
