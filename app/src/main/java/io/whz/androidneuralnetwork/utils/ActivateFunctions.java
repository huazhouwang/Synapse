package io.whz.androidneuralnetwork.utils;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import Jama.Matrix;

public class ActivateFunctions {
    @CheckResult
    public static Matrix sigmoid(@NonNull Matrix matrix) {
        Preconditions.checkNotNull(matrix);

        final double[][] doubles = matrix.getArrayCopy();

        for (int i = 0, iLen = doubles.length, jLen = doubles[0].length;
             i < iLen; ++i) {
            for (int j = 0; j < jLen; ++j) {
                final double cur = -doubles[i][j];
                doubles[i][j] = 1D / (1D + Math.exp(cur));
            }
        }

        return new Matrix(doubles);
    }

    @CheckResult
    public static Matrix sigmoidPrime(@NonNull Matrix activation) {
        Preconditions.checkNotNull(activation);

        final double[][] doubles = activation.getArrayCopy();

        for (int i = 0, iLen = doubles.length, jLen = doubles[0].length;
             i < iLen; ++i) {
            for (int j = 0; j < jLen; ++j) {
                final double cur = doubles[i][j];
                doubles[i][j] = cur * (1 - cur);
            }
        }

        return new Matrix(doubles);
    }
}
