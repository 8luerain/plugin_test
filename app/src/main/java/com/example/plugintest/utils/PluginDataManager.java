package com.example.plugintest.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PluginDataManager {
    private static final String TAG = "FilesUtils";

    private static final String plugins_asset_dir = "plugins";
    public static final String plugins_dir = "/plugins";
    public static final String unzip_dir = "/unzip";

    public static String getPluginsDirFullPath(Context context) {
        return context.getApplicationContext().getFilesDir().getAbsoluteFile() + plugins_dir;
    }

    public static String getPluginsUnzipDirFullPath(Context context) {
        String fullUnzipPath = getPluginsDirFullPath(context) + unzip_dir;
        File unzipDir = new File(fullUnzipPath);
        if (!unzipDir.exists()) {
            unzipDir.mkdirs();
        }
        return fullUnzipPath;
    }

    public static void copyAllPlugin(Context context) {
        try {
            //1:建立unzipPath
            File pluginDir = new File(getPluginsDirFullPath(context));

            //拷贝到本地目录
            byte[] bf = new byte[1024];
            BufferedInputStream inputStream = new BufferedInputStream(context.getAssets().open( "pluginc.apk"));
            FileOutputStream fileOutputStream = new FileOutputStream(new File(pluginDir.getAbsolutePath()));
            while (inputStream.read(bf) != -1) {
                fileOutputStream.write(bf);
            }
            inputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "copyPlugin: ", e);
        }
    }
}
