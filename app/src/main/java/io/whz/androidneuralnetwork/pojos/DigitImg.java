package io.whz.androidneuralnetwork.pojos;

import android.support.annotation.NonNull;

public class DigitImg {
    public final byte[] colors;
    public final int label;

    public DigitImg(int label, @NonNull byte[] colors) {
        this.colors = colors;
        this.label = label;
    }
}
