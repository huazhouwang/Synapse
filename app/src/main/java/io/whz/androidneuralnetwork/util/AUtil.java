package io.whz.androidneuralnetwork.util;

import java.util.Locale;

public class AUtil {
    public static String formatTimeUsed(long timeUsed) {
        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                timeUsed / (3600000), timeUsed / (60000) % 60, timeUsed / 1000 % 60);
    }

    public static String format2Percent(double value) {
        return String.format(Locale.getDefault(),
                "%.2f%%", value * 100);
    }
}
