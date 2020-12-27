package com.d.lib.album.loader;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.d.lib.album.model.Album;
import com.d.lib.album.model.Media;

/**
 * Load images and videos into a single cursor.
 */
public class AlbumMediaLoader extends CursorLoaderCompat {
    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    private static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            "duration"};
    private static final String ORDER_BY = MediaStore.Images.Media.DATE_TAKEN + " DESC";

    private static final String SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";
    private static final String SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND "
                    + " bucket_id=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private AlbumMediaLoader(@NonNull Context context,
                             @Nullable String selection, @Nullable String[] selectionArgs) {
        super(context, QUERY_URI, PROJECTION, selection, selectionArgs, ORDER_BY);
    }

    private static String[] getSelectionArgsForSingleMediaType(int mediaType) {
        return new String[]{String.valueOf(mediaType)};
    }

    private static String[] getSelectionAlbumArgsForSingleMediaType(int mediaType, String albumId) {
        return new String[]{String.valueOf(mediaType), albumId};
    }

    public static AlbumMediaLoader load(final Context context, final String albumId,
                                        final Callback callback) {
        String selection;
        String[] selectionArgs;

        if (Album.ALBUM_ID_ALL.equals(albumId) || TextUtils.isEmpty(albumId)) {
            selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE;
            selectionArgs = getSelectionArgsForSingleMediaType(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
        } else {
            selection = SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE;
            selectionArgs = getSelectionAlbumArgsForSingleMediaType(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                    albumId);
        }
        final AlbumMediaLoader loader = new AlbumMediaLoader(context, selection, selectionArgs);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Handler mMainHandler = new Handler(Looper.getMainLooper());
                try {
                    final Cursor cursor = loader.loadInBackground();
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onSuccess(cursor);
                            }
                        }
                    });
                } catch (final Throwable e) {
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onError(e);
                            }
                        }
                    });
                }
            }
        }).start();
        return loader;
    }

    public static Cursor mergeCapture(Cursor cursor) {
        MatrixCursor capture = new MatrixCursor(PROJECTION);
        capture.addRow(new Object[]{Media.ITEM_ID_CAPTURE, Media.ITEM_DISPLAY_NAME_CAPTURE, "", 0, 0});
        return new MergeCursor(new Cursor[]{capture, cursor});
    }
}
