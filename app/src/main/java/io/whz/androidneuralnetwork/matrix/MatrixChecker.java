package io.whz.androidneuralnetwork.matrix;

import android.support.annotation.NonNull;

import io.whz.androidneuralnetwork.utils.Preconditions;

class MatrixChecker {
    static void verifyArray(@NonNull double[][] array) {
        checkNotNull(array);

        final int m = array.length;
        checkExpression(m > 0,
                "Row should be positive");

        final int n = array[0].length;
        for (int i = 1; i < m; ++i) {
            checkExpression(array[i].length == n,
                    "All rows must have the same length");
        }
    }

    static void checkNotNull(Object o) {
        Preconditions.checkNotNull(o);
    }

    static void checkExpression(boolean expression, String message) {
        Preconditions.checkArgument(expression, message);
    }

    static void checkExpression(boolean expression) {
        checkExpression(expression,
                "Expression Fail");
    }

    static void checkDimensions(int row, int col) {
        checkExpression(row > 0 && col > 0,
                "Row and column should be positive");
    }

    static void checkDimensions(@NonNull Matrix a, @NonNull Matrix b) {
        final int[] aDim = a.shape();
        final int[] bDim = b.shape();

        checkExpression(aDim[0] == bDim[0] && aDim[1] == bDim[1],
                "Matrix dimensions must agree");
    }

    static void checkInnerDimensions(@NonNull Matrix a, @NonNull Matrix b) {
        checkExpression(a.getCol() == b.getRow(),
                "Matrix inner dimensions must agree");
    }
}
