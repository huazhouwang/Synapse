package io.whz.androidneuralnetwork.pojos;

import android.support.annotation.NonNull;

public class Digit {
    public final double[] colorRates;
    public final int label;

    public Digit(int label, @NonNull double[] colorRates) {
        this.colorRates = colorRates;
        this.label = label;
    }
}
