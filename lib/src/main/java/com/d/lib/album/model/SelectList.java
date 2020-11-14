package com.d.lib.album.model;

import java.util.ArrayList;
import java.util.List;

/**
 * SelectList
 * Created by D on 2020/10/11.
 */
public class SelectList<E> {
    public static final int MAX_COUNT = 9;

    final List<E> mList = new ArrayList<>();
    final int mMaxCount;

    public SelectList() {
        this(MAX_COUNT);
    }

    public SelectList(int count) {
        this.mMaxCount = count;
    }

    public int getMaxCount() {
        return mMaxCount;
    }

    public List<E> asList() {
        return new ArrayList<>(mList);
    }

    public boolean add(E e) {
        if (mList.size() >= mMaxCount) {
            return false;
        }
        mList.add(e);
        return true;
    }

    public boolean remove(E e) {
        return mList.remove(e);
    }

    public boolean addAll(List<E> list) {
        return mList.addAll(list);
    }

    public void clear() {
        mList.clear();
    }

    public E get(int index) {
        return mList.get(index);
    }

    public int indexOf(E e) {
        return mList.indexOf(e);
    }

    public boolean contains(E e) {
        return mList.contains(e);
    }

    public int size() {
        return mList.size();
    }
}
