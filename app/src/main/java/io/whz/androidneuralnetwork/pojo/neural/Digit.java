package io.whz.androidneuralnetwork.pojo.neural;

import android.support.annotation.NonNull;

public class Digit {
    public final double[] colors;
    public final int label;

    public Digit(int label, @NonNull double[] colors) {
        this.colors = colors;
        this.label = label;
    }
}
