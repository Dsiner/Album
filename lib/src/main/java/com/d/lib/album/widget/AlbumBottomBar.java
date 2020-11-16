package com.d.lib.album.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.d.lib.album.R;
import com.d.lib.album.model.Media;

/**
 * AlbumBottomBar
 * Created by D on 2020/10/11.
 **/
public class AlbumBottomBar extends LinearLayout implements View.OnClickListener {
    public static final int TYPE_ALBUM = 0;
    public static final int TYPE_PREVIEW = 1;

    protected Context mContext;
    protected View mRootView;
    private int mType = TYPE_ALBUM;
    private TextView tv_bottom_edit;
    private View llyt_bottom_origin, llyt_bottom_choose;
    private CheckBox cb_toggle_origin, cb_toggle_choose;
    private OnBottomListener mOnBottomListener;
    private Media mMedia;

    public AlbumBottomBar(Context context) {
        this(context, null);
    }

    public AlbumBottomBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlbumBottomBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void onClick(View v) {
        int resId = v.getId();
        if (R.id.llyt_bottom_origin == resId) {
            boolean checked = cb_toggle_origin.isChecked();
            cb_toggle_origin.setChecked(!checked);

        } else if (R.id.llyt_bottom_choose == resId) {
            boolean checked = !cb_toggle_choose.isChecked();
            if (mOnBottomListener != null) {
                boolean changed = mOnBottomListener.onCheck(mMedia, checked);
                if (changed) {
                    cb_toggle_choose.setChecked(checked);
                }
            }
        }
    }

    protected void init(Context context) {
        mContext = context;
        mRootView = LayoutInflater.from(context).inflate(R.layout.lib_album_layout_bottom, this);
        setOrientation(VERTICAL);

        bindView();

        cb_toggle_origin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mOnBottomListener != null) {
                    mOnBottomListener.onOrigin(isChecked);
                }
            }
        });
    }

    private void bindView() {
        tv_bottom_edit = findViewById(R.id.tv_bottom_edit);
        llyt_bottom_origin = findViewById(R.id.llyt_bottom_origin);
        llyt_bottom_choose = findViewById(R.id.llyt_bottom_choose);
        cb_toggle_origin = findViewById(R.id.cb_toggle_origin);
        cb_toggle_choose = findViewById(R.id.cb_toggle_choose);

        llyt_bottom_origin.setOnClickListener(this);
        llyt_bottom_choose.setOnClickListener(this);
    }

    public void setType(int type) {
        if (TYPE_ALBUM == type) {
            tv_bottom_edit.setText(R.string.lib_album_preview);
            tv_bottom_edit.setVisibility(VISIBLE);
            llyt_bottom_origin.setVisibility(VISIBLE);
            llyt_bottom_choose.setVisibility(INVISIBLE);

        } else if (TYPE_PREVIEW == type) {
            tv_bottom_edit.setText(R.string.lib_album_edit);
            tv_bottom_edit.setVisibility(VISIBLE);
            llyt_bottom_origin.setVisibility(VISIBLE);
            llyt_bottom_choose.setVisibility(VISIBLE);
        }
        mType = type;
    }

    public void setCount(int count) {
        if (TYPE_ALBUM == mType) {
            tv_bottom_edit.setEnabled(count > 0);
            tv_bottom_edit.setClickable(count > 0);
            tv_bottom_edit.setText(count > 0
                    ? getResources().getText(R.string.lib_album_preview) + "(" + count + ")"
                    : getResources().getText(R.string.lib_album_preview));
            tv_bottom_edit.setTextColor(ContextCompat.getColor(mContext, count > 0
                    ? R.color.lib_album_color_text_bottom
                    : R.color.lib_album_color_text_bottom_disable));
        }
    }

    public void setOriginEnable(boolean enable) {
        llyt_bottom_origin.setVisibility(enable ? VISIBLE : GONE);
    }

    public boolean isOriginChecked() {
        return cb_toggle_origin.isChecked();
    }

    public void setOriginChecked(boolean checked) {
        cb_toggle_origin.setChecked(checked);
    }

    public void setChecked(boolean checked) {
        cb_toggle_choose.setChecked(checked);
    }

    public Media getMedia() {
        return this.mMedia;
    }

    public void setMedia(Media media) {
        this.mMedia = media;
    }

    public void setOnBottomListener(OnBottomListener l) {
        this.mOnBottomListener = l;
    }

    public interface OnBottomListener {
        void onOrigin(boolean check);

        boolean onCheck(Media item, boolean check);
    }
}
