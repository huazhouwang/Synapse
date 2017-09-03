package io.whz.androidneuralnetwork.utils;

import android.support.annotation.NonNull;

import java.io.File;

public class FileUtils {

    public static void clear(@NonNull File... files) {
        final StringBuilder builder = new StringBuilder();

        for (File file : files) {
            if (file.exists()) {
                builder.append(file.getAbsolutePath())
                        .append(' ');
            }
        }

        String res;

        if (!(res = builder.toString()).isEmpty()) {
            ProcessUtils.execcommand(String.format("rm -rf %s", res));
        }
    }

    public static void makeDirs(@NonNull File... dirs) {
        for (File dir : dirs) {
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }
}
