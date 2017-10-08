package io.whz.androidneuralnetwork.util;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.whz.androidneuralnetwork.neural.NeuralNetwork;

public class StringFormatUtil {
    private static final String SPLIT_ITEM = ":";

    public static String formatTimeUsed(long timeUsed) {
        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                timeUsed / (3600000), timeUsed / (60000) % 60, timeUsed / 1000 % 60);
    }

    public static String format2Percent(double value) {
        return String.format(Locale.getDefault(),
                "%.2f%%", value * 100);
    }

    public static List<Double> splitString2DoubleList(@NonNull String accuracyString) {
        final String[] tmp = accuracyString.split(SPLIT_ITEM);
        final List<Double> res = new ArrayList<>();

        for (String item : tmp) {
            res.add(Double.valueOf(item));
        }

        return res;
    }

    public static int[] splitString2IntArray(@NonNull String hiddenSizeString) {
        final String[] sizes = hiddenSizeString.split(SPLIT_ITEM);
        final int[] res = new int[sizes.length];

        for (int i = 0, len = res.length; i < len; ++i) {
            res[i] = Integer.valueOf(sizes[i]);
        }

        return res;
    }

    public static String mergeIntArray2String(@NonNull int... arrays) {
        final StringBuilder builder = new StringBuilder();

        for (int i : arrays) {
            builder.append(i)
                    .append(SPLIT_ITEM);
        }

        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }

    public static String mergeDoubleList2String(@NonNull List<Double> arrays) {
        final StringBuilder builder = new StringBuilder();
        final Locale locale = Locale.getDefault();

        for (double i : arrays) {
            builder.append(String.format(locale, "%.4f", i))
                    .append(SPLIT_ITEM);
        }

        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
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
