package io.whz.androidneuralnetwork.utils;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import io.whz.androidneuralnetwork.matrix.Matrix;

public class ActivateFunctions {
    @CheckResult
    public static Matrix sigmoid(@NonNull Matrix matrix) {
        Preconditions.checkNotNull(matrix);

        final Matrix copy = matrix.copy();
        final double[] doubles = copy.getArray();

        for (int i = 0, len = doubles.length; i < len; ++i) {
                final double cur = -doubles[i];
                doubles[i] = 1D / (1D + Math.exp(cur));
        }

        return copy;
    }

    @CheckResult
    public static Matrix sigmoidPrime(@NonNull Matrix activation) {
        Preconditions.checkNotNull(activation);

        final Matrix copy = activation.copy();
        final double[] doubles = copy.getArray();

        for (int i = 0, iLen = doubles.length; i < iLen; ++i) {
            final double cur = doubles[i];
            doubles[i] = cur * (1 - cur);
        }

        return copy;
    }
}
