package com.d.lib.album.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

    private final MediaScannerConnection mMsc;
    private final String mPath;
    private final ScanListener mListener;

    public SingleMediaScanner(Context context, String mPath, ScanListener mListener) {
        this.mPath = mPath;
        this.mListener = mListener;
        this.mMsc = new MediaScannerConnection(context, this);
        this.mMsc.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        mMsc.scanFile(mPath, null);
    }

    @Override
    public void onScanCompleted(String mPath, Uri mUri) {
        mMsc.disconnect();
        if (mListener != null) {
            mListener.onScanFinish();
        }
    }

    public interface ScanListener {
        void onScanFinish();
    }
}
