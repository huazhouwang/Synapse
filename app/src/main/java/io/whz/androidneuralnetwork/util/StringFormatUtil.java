package io.whz.androidneuralnetwork.util;

import android.support.annotation.NonNull;

import java.util.Locale;

import io.whz.androidneuralnetwork.neural.NeuralNetwork;

public class StringFormatUtil {
    private static final String SPLIT_ITEM = ":";

    public static String formatTimeUsed(long timeUsed) {
        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                timeUsed / (3600000), timeUsed / (60000) % 60, timeUsed / 1000 % 60);
    }

    public static String formatPercent(double value) {
        return String.format(Locale.getDefault(),
                "%.2f%%", value * 100);
    }

    public static String formatLayerSizes(@NonNull int[] hiddenSizes) {
        final StringBuilder builder = new StringBuilder();
        final String spilt = " × ";

        builder.append(NeuralNetwork.INPUT_LAYER_NUMBER)
                .append(spilt);

        for (int size : hiddenSizes) {
            builder.append(size)
                    .append(spilt);
        }

        builder.append(NeuralNetwork.OUTPUT_LAYER_NUMBER);

        return builder.toString();
    }
}
