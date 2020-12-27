package com.d.album;

import android.app.Application;
import android.os.StrictMode;

import com.d.lib.album.Album;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // initStrictMode();

        Album.setDiskCache("Album");
    }

    private void initStrictMode() {
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }
}
