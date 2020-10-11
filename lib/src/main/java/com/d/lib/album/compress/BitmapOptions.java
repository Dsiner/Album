package com.d.lib.album.compress;

import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;

import com.d.lib.album.compress.strategy.CompressStrategy;
import com.d.lib.album.compress.strategy.DefaultStrategy;

/**
 * BitmapOptions
 * Created by D on 2019/6/18.
 **/
public class BitmapOptions {
    public Bitmap.CompressFormat format;
    public int width;
    public int height;
    public int degree;
    public int quality = 85;
    public int size;
    CompressStrategy strategy = new DefaultStrategy();
    Bitmap.Config config; // Target image config for decoding.

    public static String mimeType(Bitmap.CompressFormat format) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                && format == Bitmap.CompressFormat.WEBP) {
            return ".webp";
        }
        if (format == Bitmap.CompressFormat.JPEG) {
            return ".jpg";
        } else if (format == Bitmap.CompressFormat.PNG) {
            return ".png";
        }
        return ".jpg";
    }

    public static Bitmap.CompressFormat format(String mimeType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                && TextUtils.equals(mimeType, ".webp")) {
            return Bitmap.CompressFormat.WEBP;
        }
        if (TextUtils.equals(mimeType, ".jpg")) {
            return Bitmap.CompressFormat.JPEG;
        } else if (TextUtils.equals(mimeType, ".png")) {
            return Bitmap.CompressFormat.PNG;
        }
        return Bitmap.CompressFormat.JPEG;
    }

    @Override
    public String toString() {
        return "" + width + "*" + height
                + "-" + quality + "-" + size
                + "-" + config + "-" + format;
    }
}
