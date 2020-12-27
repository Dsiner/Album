package com.d.lib.album.compress.strategy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.annotation.NonNull;

import com.d.lib.album.compress.BitmapOptions;

import java.io.InputStream;

/**
 * DefaultStrategy
 * Created by D on 2020/3/21.
 */
public class DefaultStrategy extends CompressStrategy {

    @Override
    public Bitmap decodeStream(@NonNull InputStream input, @NonNull BitmapOptions opts,
                               @NonNull BitmapFactory.Options setting) {
        final int width = opts.width % 2 == 1 ? opts.width + 1 : opts.width;
        final int height = opts.height % 2 == 1 ? opts.height + 1 : opts.height;
        int inSampleSize = 1;
        while ((width / inSampleSize) * (height / inSampleSize) > mInSampleWidth * mInSampleHeight) {
            inSampleSize *= 2;
        }

        setting.inSampleSize = inSampleSize;
        return BitmapFactory.decodeStream(input, null, setting);
    }

    @Override
    public Bitmap matrix(@NonNull Bitmap source, @NonNull BitmapOptions opts) {
        final int width = source.getWidth();
        final int height = source.getHeight();
        final int longSide = Math.max(width, height);
        final int shortSide = Math.min(width, height);
        if (width * height > mMaxWidth * mMaxHeight
                || Bitmap.CompressFormat.JPEG == opts.format && opts.degree != 0) {
            Bitmap bitmap = null;
            Matrix matrix = new Matrix();

            if (width * height > mMaxWidth * mMaxHeight) {
                // Scale
                float sx = (float) Math.sqrt(mMaxWidth * mMaxHeight / (float) (width * height));
                sx = getScale(sx, shortSide);
                sx = Math.min(1, sx);
                matrix.setScale(sx, sx);
            }

            if (Bitmap.CompressFormat.JPEG == opts.format
                    && opts.degree != 0) {
                // Rotate
                matrix.postRotate(opts.degree);
            }

            try {
                bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
            } catch (OutOfMemoryError ignored) {
            }
            if (bitmap == null) {
                bitmap = source;
            }
            if (source != bitmap) {
                source.recycle();
            }
            return bitmap;
        }
        return source;
    }
}
