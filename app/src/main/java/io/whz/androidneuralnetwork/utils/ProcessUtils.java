package io.whz.androidneuralnetwork.utils;

import java.io.IOException;

public class ProcessUtils {
    public static boolean execcommand(String command) {
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
