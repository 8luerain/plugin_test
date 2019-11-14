package com.example.plugintest;

import android.app.Application;

import com.example.baselib.utils.Utils;

import java.io.File;
import java.io.IOException;

import dalvik.system.DexClassLoader;

public class HostApplication extends Application {
    public String dex_base_path;


    @Override
    public void onCreate() {
        super.onCreate();
        try {
            dex_base_path = getFilesDir().toString() + "/plugins/";
            Utils.copyAllAssertToCacheFolder(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
