package com.example.baselib.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
    public static String getUtilsString() {
        return "from utils string";
    }

    public static String getUtilsString2() {
        return "from utils string2";
    }


    static public boolean copyAllAssertToCacheFolder(Context c)
            throws IOException {
        String[] files = c.getAssets().list("plugins");
        String filefolder = c.getFilesDir().toString();
        File devicefile = new File(filefolder + "/plugins/");
        devicefile.mkdirs();
        for (int i = 0; i < files.length; i++) {
            File devfile = new File(filefolder + "/plugins/" + files[i]);
            if (!devfile.exists()) {
                copyFileTo(c, "plugins/" + files[i], filefolder
                        + "/plugins/" + files[i]);
            }
        }
        String[] filestr = devicefile.list();
        for (int i = 0; i < filestr.length; i++) {
            Log.i("file", filestr[i]);
        }
        return true;
    }

    public static String[] getPluginFilePath(Context context) throws IOException {
        return context.getAssets().list("plugins");
    }


    public static String getPluginFilePathAll(Context context) {
        StringBuilder builder = new StringBuilder();
        String[] pluginFilePath = new File(context.getFilesDir().toString() +"/plugins/").list();
        for (int i = 0; i < pluginFilePath.length; i++) {
            String s = pluginFilePath[i];
            builder.append("/plugins/").append(s);
            if (i != pluginFilePath.length - 1) {
                builder.append(File.separator);
            }
        }
        return builder.toString();
    }

    static public boolean copyFileTo(Context c, String orifile,
                                     String desfile) throws IOException {
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(desfile);
        myInput = c.getAssets().open(orifile);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
        return true;
    }
}

