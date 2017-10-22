package io.whz.synapse.util;

import android.support.annotation.NonNull;

import java.io.File;

public class FileUtil {

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
            ProcessUtil.execCommand(String.format("rm -rf %s", res));
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
