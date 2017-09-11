package io.whz.androidneuralnetwork.utils;

import android.support.annotation.NonNull;

import Jama.Matrix;

public class MatrixUtils {
    public static Matrix[] zerosLike(@NonNull Matrix[] matrices) {
        Preconditions.checkNotNull(matrices);

        final int len = matrices.length;
        final Matrix[] res = new Matrix[len];

        for (int i = 0; i < len; ++i) {
            final Matrix matrix = matrices[i];
            res[i] = new Matrix(matrix.getRowDimension(), matrix.getColumnDimension());
        }

        return res;
    }

    public static Matrix[] random(int[] rows, int[] cols) {
        final int len;
        Preconditions.checkArgument((len = rows.length) == cols.length);

        final Matrix[] matrices = new Matrix[len];

        for (int i = 0; i < len; ++i) {
            matrices[i] = Matrix.random(rows[i], cols[i]);
        }

        return matrices;
    }

    public static double[][] reShape(@NonNull double[][] src, int targetRow, int targetCol) {
        Preconditions.checkNotNull(src);

        final int srcRow = src.length;
        final int srcCol = src[0].length;

        Preconditions.checkArgument(srcCol * srcRow == targetCol * targetRow, "Matrix dimensions must agree.");

        int si = 0, sj = 0;
        final double[][] target = new double[targetRow][targetCol];

        for (int i = 0; i < targetRow; ++i) {
            for (int j = 0; j < targetCol; ++j) {
                target[i][j] = src[si][sj++];

                if (sj >= srcCol) {
                    sj = 0;
                    ++si;
                }
            }
        }

        return target;
    }

    public static Matrix reShape(@NonNull Matrix matrix, int targetRow, int targetCol) {
        Preconditions.checkNotNull(matrix);

        final int srcRow = matrix.getRowDimension();
        final int srcCol = matrix.getColumnDimension();

        Preconditions.checkArgument(srcCol * srcRow == targetCol * targetRow, "Matrix dimensions must agree.");

        int si = 0, sj = 0;

        final double[][] src = matrix.getArray();
        final double[][] target = new double[targetRow][targetCol];

        for (int i = 0; i < targetRow; ++i) {
            for (int j = 0; j < targetCol; ++j) {
                target[i][j] = src[si][sj++];

                if (sj >= srcCol) {
                    sj = 0;
                    ++si;
                }
            }
        }

        return new Matrix(target);
    }

    public static int index(Matrix matrix) {
        final double[][] doubles = matrix.getArray();

        Preconditions.checkArgument(doubles[0].length == 1);

        for (int i = 0, len = doubles.length; i < len; ++i) {
            if (doubles[i][0] == 1) {
                return i;
            }
        }

        throw new IllegalStateException("Can not find 1 in matrix");
    }

    public static int argmax(Matrix matrix) {
        final double[][] doubles = matrix.getArray();

        Preconditions.checkArgument(doubles[0].length == 1);
        int res = 0;

        for (int i = 1, len = doubles.length; i < len; ++i) {
            if (doubles[i][0] > doubles[res][0]) {
                res = i;
            }
        }

        return res;
    }
}
