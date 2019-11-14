package com.example.plugintest;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.baselib.utils.Utils;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    public static final String plugin_path = "/plugins/";
    public static final String plugin_path_unzip = "/plugin_unzip";
    private TextView txvPlugin;
    private PluginInfo mPluginInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadPlugins();
        setContentView(R.layout.activity_main);
        initView();
    }

    private void loadPlugins() {
        String unzipFolderPath = getApplicationContext().getFilesDir().toString() + plugin_path_unzip;
        File devicefile = new File(unzipFolderPath);
        devicefile.mkdirs();
        String dexPath = getApplicationContext().getFilesDir().toString() + "/plugins/plugina.apk";
        String dexPathb = getApplicationContext().getFilesDir().toString() + "/plugins/pluginb.apk";
        String all = dexPath + File.pathSeparator + dexPathb;

        Log.d("dexclassloader", "loadPlugins: " + all);
        mPluginInfo = new PluginInfo();
        mPluginInfo.classLoader = new DexClassLoader(all,
                plugin_path_unzip, null, getClassLoader());
    }


    private void initView() {
        txvPlugin = findViewById(R.id.txv_main);
        txvPlugin.setText(R.string.formlib);
        try {
            Class<?> codeFromA = mPluginInfo.classLoader.loadClass("com.example.plugina.CodeFromA");
            Class<?> codeFromB = mPluginInfo.classLoader.loadClass("com.example.pluginb.CodeFromB");
            Method[] methods = codeFromA.getMethods();
            for (Method method : methods) {
                Log.d("codeFrom", "A: " + method.getName());
            }
            Method[] methodb = codeFromB.getMethods();
            for (Method method : methodb) {
                Log.d("codeFrom", "B: " + method.getName());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
