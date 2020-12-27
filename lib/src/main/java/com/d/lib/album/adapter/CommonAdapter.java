package com.d.lib.album.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * CommonAdapter for RecyclerView
 * Created by D on 2017/4/25.
 */
abstract class CommonAdapter<T> extends RecyclerView.Adapter<CommonHolder> {
    protected Context mContext;
    @NonNull
    protected List<T> mDatas;
    protected int mLayoutId;
    protected MultiItemTypeSupport<T> mMultiItemTypeSupport;

    public CommonAdapter(@NonNull Context context, List<T> datas, int layoutId) {
        mContext = context;
        mDatas = datas != null ? new ArrayList<>(datas) : new ArrayList<T>();
        mLayoutId = layoutId;
    }

    public CommonAdapter(@NonNull Context context, List<T> datas, MultiItemTypeSupport<T> multiItemTypeSupport) {
        mContext = context;
        mDatas = datas != null ? new ArrayList<>(datas) : new ArrayList<T>();
        mMultiItemTypeSupport = multiItemTypeSupport;
    }

    public List<T> getDatas() {
        return mDatas;
    }

    public void setDatas(List<T> datas) {
        if (mDatas != null && datas != null) {
            mDatas.clear();
            mDatas.addAll(datas);
        }
    }

    @Override
    public int getItemCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (mMultiItemTypeSupport != null) {
            if (mDatas != null && mDatas.size() > 0) {
                return mMultiItemTypeSupport.getItemViewType(position, mDatas.get(position));
            }
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public CommonHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = mLayoutId;
        if (mMultiItemTypeSupport != null) {
            // MultiType
            if (mDatas != null && mDatas.size() > 0) {
                layoutId = mMultiItemTypeSupport.getLayoutId(viewType);
            }
        }
        CommonHolder holder = CommonHolder.create(mContext, parent, layoutId);
        onViewHolderCreated(holder, holder.itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommonHolder holder, int position) {
        convert(position, holder, mDatas.get(position));
    }

    public void onViewHolderCreated(CommonHolder holder, View itemView) {
    }

    /**
     * @param position The position of the item within the adapter's data set.
     * @param holder   Holder
     * @param item     Data
     */
    public abstract void convert(int position, CommonHolder holder, T item);
}
