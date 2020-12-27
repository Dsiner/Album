package com.d.lib.album.compress.strategy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.d.lib.album.compress.BitmapOptions;

import java.io.InputStream;

/**
 * LimitStrategy
 * Created by D on 2020/3/21.
 */
public class LimitStrategy extends CompressStrategy {
    private final boolean mAverage;

    public LimitStrategy() {
        this.mInSampleWidth = 1080 * 2;
        this.mInSampleHeight = 1920 * 2;
        this.mAverage = true;
    }

    public LimitStrategy(int maxWidth, int maxHeight, boolean average) {
        this.mInSampleWidth = maxWidth;
        this.mInSampleHeight = maxHeight;
        this.mAverage = average;
    }

    @Override
    public Bitmap decodeStream(@NonNull InputStream input, @NonNull BitmapOptions opts,
                               @NonNull BitmapFactory.Options setting) {
        final int width = opts.width % 2 == 1 ? opts.width + 1 : opts.width;
        final int height = opts.height % 2 == 1 ? opts.height + 1 : opts.height;
        int inSampleSize = 1;
        if (mAverage) {
            while ((width / inSampleSize) * (height / inSampleSize) > mInSampleWidth * mInSampleHeight) {
                inSampleSize *= 2;
            }
        } else {
            while (width / inSampleSize > mInSampleWidth || height / inSampleSize > mInSampleHeight) {
                inSampleSize *= 2;
            }
        }

        setting.inSampleSize = inSampleSize;
        return BitmapFactory.decodeStream(input, null, setting);
    }
}
