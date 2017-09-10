package io.whz.androidneuralnetwork.pojos;

import android.support.annotation.NonNull;

public class Digit {
    public final double[] pixels;
    public final int actual;

    public Digit(int actual, @NonNull double[] pixels) {
        this.pixels = pixels;
        this.actual = actual;
    }
}
