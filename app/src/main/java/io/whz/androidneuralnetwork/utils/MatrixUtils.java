package io.whz.androidneuralnetwork.utils;

import android.support.annotation.NonNull;

import Jama.Matrix;

public class MatrixUtils {
    public static Matrix[] copyZeroes(@NonNull Matrix[] matrices) {
        Preconditions.checkNotNull(matrices);

        final int len = matrices.length;
        final Matrix[] res = new Matrix[len];

        for (int i = 0; i < len; ++i) {
            final Matrix matrix = matrices[i];
            res[i] = new Matrix(matrix.getRowDimension(), matrix.getColumnDimension());
        }

        return res;
    }
}
