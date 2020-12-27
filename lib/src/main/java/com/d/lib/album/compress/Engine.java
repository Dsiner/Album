package com.d.lib.album.compress;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Engine {

    public static final int MAX_QUALITY = 90;
    public static final int MAX_OUTPUT_SIZE = 375;

    final InputStreamProvider mProvider;
    final BitmapOptions mRequestOptions;
    final BitmapOptions mOptions;

    Engine(@NonNull InputStreamProvider provider, @NonNull BitmapOptions requestOptions)
            throws IOException {
        mProvider = provider;
        mRequestOptions = requestOptions;
        mOptions = new BitmapOptions();
        initOptions();
    }

    public static void load(Context context, Uri uri, ImageView imageView) {
        try {
            ByteArrayOutputStream outputStream = compress(context.getApplicationContext(), uri);
            Bitmap bitmap = BitmapFactory.decodeByteArray(outputStream.toByteArray(),
                    0, outputStream.size());
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageBitmap(null);
        }
    }

    public static ByteArrayOutputStream compress(final Context context, final Uri uri) throws Exception {
        BitmapOptions requestOpts = new BitmapOptions();
        requestOpts.config = Bitmap.Config.ARGB_8888;
        requestOpts.format = Bitmap.CompressFormat.JPEG;
        requestOpts.quality = MAX_QUALITY;
        requestOpts.size = MAX_OUTPUT_SIZE;
        Engine engine = new Engine(new InputStreamProvider() {
            @Override
            public String getPath() {
                return UriUtils.getPath(context, uri);
            }

            @Override
            public InputStream open() throws IOException {
                return context.getContentResolver().openInputStream(uri);
            }
        }, requestOpts);
        return engine.compress();
    }

    private static ByteArrayOutputStream convert(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream;
        outputStream = new ByteArrayOutputStream();
        byte[] b = new byte[4096];
        int len;
        while ((len = inputStream.read(b)) != -1) {
            outputStream.write(b, 0, len);
            outputStream.flush();
        }
        return outputStream;
    }

    @Nullable
    public static BitmapFactory.Options decodeStream(final File file) {
        try {
            return decodeStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BitmapFactory.Options decodeStream(final InputStream input) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 1;
            BitmapFactory.decodeStream(input, null, options);
            return options;
        } finally {
            ImageUtils.closeQuietly(input);
        }
    }

    public static ByteArrayOutputStream qualityCompress(Bitmap bitmap,
                                                        Bitmap.CompressFormat format,
                                                        int quality, int size) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, outputStream);
        if (Bitmap.CompressFormat.PNG == format || size <= 0) {
            return outputStream;
        }
        while (outputStream.size() / 1024f > size && quality > 0) {
            outputStream.reset();
            if (quality > 10) {
                quality -= 10;
            } else {
                quality -= 3;
            }
            quality = Math.max(0, quality);
            bitmap.compress(format, quality, outputStream);
        }
        Log.d("Compress", "Compress size: " + outputStream.size() + " quality: " + quality);
        return outputStream;
    }

    private void initOptions() throws IOException {
        BitmapFactory.Options options = decodeStream(mProvider.open());
        mOptions.width = options.outWidth;
        mOptions.height = options.outHeight;
        mOptions.format = BitmapOptions.format(options.outMimeType.replace("image/", "."));
        if (Bitmap.CompressFormat.JPEG == mOptions.format) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mOptions.degree = ImageUtils.getImageDegree24(mProvider.open());
                } else {
                    mOptions.degree = ImageUtils.getImageDegree(mProvider.getPath());
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    ByteArrayOutputStream compress() throws IOException {
        InputStream input = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];
            if (mRequestOptions.config != null) {
                options.inPreferredConfig = mRequestOptions.config;
            }
            input = mProvider.open();
            Bitmap bitmap = mRequestOptions.strategy.decodeStream(input, mOptions, options);
            bitmap = mRequestOptions.strategy.matrix(bitmap, mOptions);
            ByteArrayOutputStream stream = mRequestOptions.strategy.qualityCompress(bitmap,
                    mOptions, mRequestOptions);
            bitmap.recycle();
            return stream;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            ImageUtils.closeQuietly(input);
        }
    }
}