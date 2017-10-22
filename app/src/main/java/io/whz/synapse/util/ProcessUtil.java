package io.whz.synapse.util;

import java.io.IOException;

public class ProcessUtil {
    public static boolean execCommand(String command) {
        final Runtime runtime = Runtime.getRuntime();
        boolean result;

        try {
            Process process = runtime.exec(command);
            process.waitFor();
            result = true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }
}
