package com.d.lib.album.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;

import com.d.lib.album.Album;
import com.d.lib.album.compress.Engine;
import com.d.lib.album.compress.ImageUtil;
import com.d.lib.album.compress.UriUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CachePool
 * Created by D on 2020/10/11.
 **/
public class CachePool {

    private static String CAMERA_DIRECTORY_PATH
            = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
            + File.separator + Album.sName
            + File.separator + "camera";
    private static String EXPORT_DIRECTORY_PATH
            = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
            + File.separator + Album.sName
            + File.separator + "export";
    public static final String FILE_CAMERA_PREFIX = "album_camera_";
    public static final String FILE_EXPORT_PREFIX = "album_export_";

    private volatile static CachePool INSTANCE;
    private final Context mContext;
    private final LinkedHashMap<Uri, File> mHashMap = new LinkedHashMap<>();

    public CachePool(Context context) {
        mContext = context.getApplicationContext();
    }

    public static CachePool getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CachePool.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CachePool(context);
                }
            }
        }
        return INSTANCE;
    }

    public static String getCameraDirectoryPath() {
        return CAMERA_DIRECTORY_PATH;
    }

    public static String getExportDirectoryPath() {
        return EXPORT_DIRECTORY_PATH;
    }

    public static void setName(String name) {
        CAMERA_DIRECTORY_PATH
                = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                + File.separator + name
                + File.separator + "camera";
        EXPORT_DIRECTORY_PATH
                = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                + File.separator + name
                + File.separator + "export";
    }

    private static File convertFile(final ByteArrayOutputStream outputStream, final String path) throws Exception {
        FileOutputStream fos = null;
        try {
            final File file = new File(path);
            final String fileName = getExportName(file);
            final String destinationPath = EXPORT_DIRECTORY_PATH
                    + File.separator + fileName;
            final File parentFile = new File(destinationPath).getParentFile();
            if (!parentFile.exists() || !parentFile.isDirectory()) {
                parentFile.mkdirs();
            }

            fos = new FileOutputStream(destinationPath);
            outputStream.writeTo(fos);
            return new File(destinationPath);
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        } finally {
            ImageUtil.closeQuietly(fos);
        }
    }

    private static String getExportName(File file) {
        final String[] paths = file.getName().split("\\.(?=[^\\.]+$)");
        String fileName = FILE_EXPORT_PREFIX + System.currentTimeMillis();
        if (paths.length > 1) {
            fileName += "." + "jpg";
        }
        return fileName;
    }

    /**
     * Delete the directory.
     *
     * @param dir The directory.
     * @return {@code true}: success<br>{@code false}: fail
     */
    private static boolean deleteDir(final File dir) {
        if (dir == null) return false;
        // dir doesn't exist then return true
        if (!dir.exists()) return true;
        // dir isn't a directory then return false
        if (!dir.isDirectory()) return false;
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!file.delete()) return false;
                } else if (file.isDirectory()) {
                    if (!deleteDir(file)) return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * Delete the file.
     *
     * @param file The file.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean deleteFile(final File file) {
        return file != null && (!file.exists() || file.isFile() && file.delete());
    }

    public boolean has(Uri uri) {
        File file = get(uri);
        return file != null && file.exists();
    }

    public File get(Uri uri) {
        return mHashMap.get(uri);
    }

    public File put(Uri uri, Bitmap bitmap) {
        return put(uri, bitmap, null);
    }

    public File put(Uri uri, Bitmap bitmap,
                    @Nullable MediaScannerConnection.OnScanCompletedListener callback) {
        try {
            ByteArrayOutputStream outputStream = Engine.qualityCompress(bitmap,
                    Bitmap.CompressFormat.JPEG,
                    Engine.MAX_QUALITY, Engine.MAX_OUTPUT_SIZE);
            String path = UriUtil.getPath(mContext, uri);
            File file = convertFile(outputStream, path);
            if (file.exists() && file.isFile()) {
                MediaScannerConnection.scanFile(mContext,
                        new String[]{file.getAbsolutePath()}, null, callback);
                mHashMap.put(uri, file);
            }
            return get(uri);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void remove(Uri uri) {
        mHashMap.remove(uri);
    }

    public void clear() {
        mHashMap.clear();
    }

    public void delete(Uri uri) {
        File file = get(uri);
        if (file != null && file.exists()) {
            deleteFile(file);
            MediaScannerConnection.scanFile(mContext,
                    new String[]{file.getAbsolutePath()}, null, null);
        }
        mHashMap.remove(uri);
    }

    public void deleteAll() {
        final List<Uri> list = new ArrayList<>();
        for (Map.Entry<Uri, File> entry : mHashMap.entrySet()) {
            list.add(entry.getKey());
        }
        for (Uri uri : list) {
            delete(uri);
        }
        mHashMap.clear();
    }
}
