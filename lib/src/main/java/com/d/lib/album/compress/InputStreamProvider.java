package com.d.lib.album.compress;

import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Get the input stream through this interface, and obtain the picture using compatible files and FileProvider
 */
public abstract class InputStreamProvider {
    public abstract String getPath();

    public abstract InputStream open() throws IOException;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InputStreamProvider) {
            return TextUtils.equals(getPath(), ((InputStreamProvider) obj).getPath());
        }
        return super.equals(obj);
    }
}
