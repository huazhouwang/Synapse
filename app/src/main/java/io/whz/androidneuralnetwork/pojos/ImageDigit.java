package io.whz.androidneuralnetwork.pojos;

import android.support.annotation.NonNull;

public class ImageDigit {
    public final byte[] colors;
    public final int label;

    public ImageDigit(int label, @NonNull byte[] colors) {
        this.colors = colors;
        this.label = label;
    }
}
