package com.d.lib.album.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.d.lib.album.R;
import com.d.lib.album.loader.AlbumLoader;

public class Album implements Parcelable {
    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
    public static final String ALBUM_ID_ALL = String.valueOf(-1);
    public static final String ALBUM_NAME_ALL = "All";

    public final String id;
    public final Uri coverUri;
    public final String displayName;
    public final long count;

    public Album(String id, Uri coverUri, String displayName, long count) {
        this.id = id;
        this.coverUri = coverUri;
        this.displayName = displayName;
        this.count = count;
    }

    private Album(Parcel source) {
        this.id = source.readString();
        this.coverUri = source.readParcelable(Uri.class.getClassLoader());
        this.displayName = source.readString();
        this.count = source.readLong();
    }

    public static Album createAll() {
        return new Album(ALBUM_ID_ALL, null, ALBUM_NAME_ALL, 0);
    }

    /**
     * Constructs a new {@link Album} entity from the {@link Cursor}.
     * This method is not responsible for managing cursor resource, such as close, iterate, and so on.
     */
    public static Album valueOf(Cursor cursor) {
        String column = cursor.getString(cursor.getColumnIndex(AlbumLoader.COLUMN_URI));
        return new Album(
                cursor.getString(cursor.getColumnIndex("bucket_id")),
                Uri.parse(column != null ? column : ""),
                cursor.getString(cursor.getColumnIndex("bucket_display_name")),
                cursor.getLong(cursor.getColumnIndex(AlbumLoader.COLUMN_COUNT)));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeParcelable(coverUri, 0);
        dest.writeString(displayName);
        dest.writeLong(count);
    }

    public String getDisplayName(Context context) {
        if (ALBUM_ID_ALL.equals(id)) {
            return context.getString(R.string.lib_album_all);
        }
        return displayName;
    }
}