package com.d.album;

import android.app.Application;
import android.os.StrictMode;

import com.d.lib.album.Album;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        Album.setDiskCache("Album");
    }
}
