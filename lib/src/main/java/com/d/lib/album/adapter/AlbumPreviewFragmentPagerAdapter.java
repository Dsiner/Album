package com.d.lib.album.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.d.lib.album.fragment.AlbumPreviewFragment;
import com.d.lib.album.model.Media;

import java.util.List;

/**
 * AlbumPreviewFragmentPagerAdapter
 * Created by D on 2020/10/10.
 */
@Deprecated
public class AlbumPreviewFragmentPagerAdapter extends FragmentPagerAdapter
        implements AlbumPreviewPagerAdapter {
    private final List<Fragment> mFragments;

    public AlbumPreviewFragmentPagerAdapter(FragmentManager fm,
                                            List<Fragment> fragments) {
        super(fm);
        this.mFragments = fragments;
    }

    @Override
    public Media get(int position) {
        AlbumPreviewFragment fragment = (AlbumPreviewFragment) mFragments.get(position);
        return fragment.getMedia();
    }

    @Override
    public int index(Media item) {
        for (int i = 0; i < mFragments.size(); i++) {
            AlbumPreviewFragment fragment = (AlbumPreviewFragment) mFragments.get(i);
            if (fragment.getMedia().equals(item)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }
}
