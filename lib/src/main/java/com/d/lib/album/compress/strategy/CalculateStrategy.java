package com.d.lib.album.compress.strategy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;

import com.d.lib.album.compress.BitmapOptions;

import java.io.InputStream;

/**
 * CalculateStrategy
 * Created by D on 2020/3/21.
 */
@Deprecated
public class CalculateStrategy extends CompressStrategy {

    @Override
    public Bitmap decodeStream(@NonNull InputStream input, @NonNull BitmapOptions opts,
                               @NonNull BitmapFactory.Options setting) {
        final int width = opts.width % 2 == 1 ? opts.width + 1 : opts.width;
        final int height = opts.height % 2 == 1 ? opts.height + 1 : opts.height;
        final int longSide = Math.max(width, height);
        final int shortSide = Math.min(width, height);
        final float scale = ((float) shortSide / longSide);
        int inSampleSize = 1;
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                inSampleSize = 1;
            } else if (longSide < 4990) {
                inSampleSize = 2;
            } else if (longSide > 4990 && longSide < 10240) {
                inSampleSize = 4;
            } else {
                inSampleSize = longSide / 1280 == 0 ? 1 : longSide / 1280;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            inSampleSize = longSide / 1280 == 0 ? 1 : longSide / 1280;
        } else {
            inSampleSize = (int) Math.ceil(longSide / (1280.0 / scale));
        }

        setting.inSampleSize = inSampleSize;
        return BitmapFactory.decodeStream(input, null, setting);
    }
}
