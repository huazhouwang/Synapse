package io.whz.androidneuralnetwork.util;

import android.support.annotation.NonNull;

import io.whz.androidneuralnetwork.matrix.Matrix;

public class MatrixUtil {
    public static Matrix[] zerosLike(@NonNull Matrix[] matrices) {
        Precondition.checkNotNull(matrices);

        final int len = matrices.length;
        final Matrix[] res = new Matrix[len];

        for (int i = 0; i < len; ++i) {
            final Matrix matrix = matrices[i];
            res[i] = Matrix.zeroLike(matrix);
        }

        return res;
    }

    public static Matrix[] randns(int[] rows, int[] cols) {
        final int len;
        Precondition.checkArgument((len = rows.length) == cols.length);

        final Matrix[] matrices = new Matrix[len];

        for (int i = 0; i < len; ++i) {
            matrices[i] = Matrix.randn(rows[i], cols[i]);
        }

        return matrices;
    }

    public static int index(Matrix matrix) {
        final double[] doubles = matrix.getArray();

        for (int i = 0, len = doubles.length; i < len; ++i) {
            if (doubles[i] == 1) {
                return i;
            }
        }

        throw new IllegalStateException("Can not find 1 in matrix");
    }

    public static int argmax(Matrix matrix) {
        final double[] doubles = matrix.getArray();

        int res = 0;

        for (int i = 1, len = doubles.length; i < len; ++i) {
            if (doubles[i] > doubles[res]) {
                res = i;
            }
        }

        return res;
    }
}
