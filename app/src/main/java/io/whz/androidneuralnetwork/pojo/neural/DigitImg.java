package io.whz.androidneuralnetwork.pojo.neural;

import android.support.annotation.NonNull;

public class DigitImg {
    public final byte[] colors;
    public final int label;

    public DigitImg(int label, @NonNull byte[] colors) {
        this.colors = colors;
        this.label = label;
    }
}
