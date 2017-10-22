package io.whz.synapse.pojo.neural;

import android.support.annotation.NonNull;

public class Figure {
    public final byte[] bytes;
    public final int label;

    public Figure(int label, @NonNull byte[] bytes) {
        this.bytes = bytes;
        this.label = label;
    }
}
