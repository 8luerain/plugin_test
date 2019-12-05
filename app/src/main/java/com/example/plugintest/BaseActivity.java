package com.example.plugintest;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.plugintest.utils.PluginDataManager;
import com.example.plugintest.utils.Reflect;
import dalvik.system.DexClassLoader;

import java.io.File;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    protected PluginInfo mPluginInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadPlugins();
    }

    protected void loadPlugins() {
        //构建插件化环境
        String restoredPath = PluginDataManager.getPluginsDirFullPath(this) + File.separator + "plugina.apk";
        Log.d(TAG, "loadPlugins: restoredPath [" + restoredPath + "]");
        StringBuilder addApkDex = new StringBuilder();
        File pluginsDir = new File(PluginDataManager.getPluginsDirFullPath(this));
        for (File file : pluginsDir.listFiles()) {
            addApkDex.append(file.getAbsolutePath()).append(File.pathSeparator);
        }
        mPluginInfo = new PluginInfo();
        mPluginInfo.classLoader = new DexClassLoader(addApkDex.toString(),
                PluginDataManager.getPluginsUnzipDirFullPath(this), null, getClassLoader());
//        try {
//            PackageManager manager = getPackageManager();
//            PackageInfo info = manager.getPackageArchiveInfo(restoredPath,
//                    PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES | PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS | PackageManager.GET_META_DATA);
//            info.applicationInfo.publicSourceDir = restoredPath;
//            info.applicationInfo.sourceDir = restoredPath;
//            mPluginInfo.resources = manager.getResourcesForApplication(info.applicationInfo);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e(TAG, "loadPlugins: ", e);
//        }
    }

    protected Object getInstanceFromPlugin(String className, @Nullable Object... args) {
        try {
            Object o = Reflect.on(className, mPluginInfo.classLoader)
                    .create(args)
                    .get();
            Log.d(TAG, "getInstanceFromPlugin: [" + o + "]");
            return o;
        } catch (Reflect.ReflectException e) {
            e.printStackTrace();
        }
        return null;
    }
}