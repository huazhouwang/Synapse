package io.whz.androidneuralnetwork.utils;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import Jama.Matrix;

public class ActivateFuns {
    public static void sigmoidSelf(@NonNull Matrix matrix) {
        Preconditions.checkNotNull(matrix);

        final double[][] doubles = matrix.getArray();

        for (int i = 0, iLen = doubles.length, jLen = doubles[0].length;
             i < iLen; ++i) {
            for (int j = 0; j < jLen; ++j) {
                final double cur = doubles[i][j];
                doubles[i][j] = 1D / (1D + Math.exp(cur));
            }
        }
    }

    @CheckResult
    public static Matrix sigmoidPrime(@NonNull Matrix activation) {
        Preconditions.checkNotNull(activation);

        final Matrix prime = activation.copy();
        final double[][] doubles = prime.getArray();

        for (int i = 0, iLen = doubles.length, jLen = doubles[0].length;
             i < iLen; ++i) {
            for (int j = 0; j < jLen; ++j) {
                final double cur = doubles[i][j];
                doubles[i][j] = cur * (1 - cur);
            }
        }

        return prime;
    }
}
