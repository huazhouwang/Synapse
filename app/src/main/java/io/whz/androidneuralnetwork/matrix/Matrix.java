package io.whz.androidneuralnetwork.matrix;

import android.support.annotation.NonNull;

import java.util.Random;

import static io.whz.androidneuralnetwork.matrix.MatrixChecker.checkDimensions;
import static io.whz.androidneuralnetwork.matrix.MatrixChecker.checkExpression;
import static io.whz.androidneuralnetwork.matrix.MatrixChecker.checkInnerDimensions;
import static io.whz.androidneuralnetwork.matrix.MatrixChecker.checkNotNull;
import static io.whz.androidneuralnetwork.matrix.MatrixChecker.verifyArray;

public class Matrix {
    private final int mRow;
    private final int mCol;
    private final double[][] mArray;

    private Matrix(double[][] array) {
        mRow = array.length;
        mCol = array[0].length;
        mArray = array;
    }

    public int[] shape() {
        return new int[]{mRow, mCol};
    }

    public double[][] getArray() {
        return mArray;
    }

    public Matrix copy() {
        final double[][] array = mArray.clone();

        for (int i = 0, len = mArray.length; i < len; ++i) {
            array[i] = mArray[i].clone();
        }

        return array(array);
    }

    public int getRow() {
        return mRow;
    }

    public int getCol() {
        return mCol;
    }

    public double get(int i, int j) {
        return mArray[i][j];
    }

    public Matrix times(@NonNull Matrix matrix) {
        checkNotNull(matrix);

        return Matrix.times(this, matrix);
    }

    public Matrix times(double num) {
        return Matrix.times(this, num);
    }

    public Matrix timesTo(double num) {
        return Matrix.timesTo(this, num);
    }

    public Matrix timesTo(@NonNull Matrix matrix) {
        checkNotNull(matrix);

        return Matrix.timesTo(this, matrix);
    }

    public Matrix plus(@NonNull Matrix matrix) {
        checkNotNull(matrix);

        return Matrix.plus(this, matrix);
    }

    public Matrix plusTo(@NonNull Matrix matrix) {
        checkNotNull(matrix);

        return Matrix.plusTo(this, matrix);
    }

    public Matrix minus(@NonNull Matrix matrix) {
        checkNotNull(matrix);

        return Matrix.minus(this, matrix);
    }

    public Matrix minusTo(@NonNull Matrix matrix) {
        checkNotNull(matrix);

        return Matrix.minusTo(this, matrix);
    }

    public Matrix dot(@NonNull Matrix matrix) {
        checkNotNull(matrix);

        return Matrix.dot(this, matrix);
    }

    public Matrix transpose() {
        return Matrix.transpose(this);
    }

    public static Matrix array(@NonNull double[][] array) {
        verifyArray(array);

        return new Matrix(array);
    }

    public static Matrix array(@NonNull double[] array, int row) {
        checkNotNull(array);
        checkExpression(row > 0, "Row should be positive");

        final int col = array.length / row;
        checkExpression(row * col == array.length, "Array length must be a multiple of row");

        final Matrix matrix = zeros(row, col);
        final double[][] newArray = matrix.getArray();

        for (int i = 0; i < row; ++i) {
            System.arraycopy(array, i * row, newArray[i], 0, col);
        }

        return matrix;
    }

    public static Matrix randn(int row, int col) {
        checkDimensions(row, col);

        final Matrix matrix = zeros(row, col);
        final double[][] array = matrix.getArray();
        final Random random = new Random(System.currentTimeMillis());

        for (int i = 0; i < row; ++i) {
            for (int j = 0; j < col; ++j) {
                array[i][j] = random.nextGaussian();
            }
        }

        return matrix;
    }

    public static Matrix plus(@NonNull Matrix a, @NonNull Matrix b) {
        checkNotNull(a);
        checkNotNull(b);
        checkDimensions(a, b);

        return plusTo(a.copy(), b);
    }

    public static Matrix plusTo(@NonNull Matrix a,@NonNull Matrix b) {
        checkNotNull(a);
        checkNotNull(b);
        checkDimensions(a, b);

        final int[] shape = a.shape();
        final double[][] aArr = a.getArray();
        final double[][] bArr = b.getArray();

        for (int i = 0, row = shape[0]; i < row; ++i) {
            for (int j = 0, col = shape[1]; j < col; ++j) {
                aArr[i][j] += bArr[i][j];
            }
        }

        return a;
    }

    public static Matrix minus(@NonNull Matrix a,@NonNull Matrix b) {
        checkNotNull(a);
        checkNotNull(b);
        checkDimensions(a, b);

        return minusTo(a.copy(), b);
    }

    public static Matrix minusTo(@NonNull Matrix a, @NonNull Matrix b) {
        checkNotNull(a);
        checkNotNull(b);
        checkDimensions(a, b);

        final int[] shape = a.shape();
        final double[][] aArr = a.getArray();
        final double[][] bArr = b.getArray();

        for (int i = 0, row = shape[0]; i < row; ++i) {
            for (int j = 0, col = shape[1]; j < col; ++j) {
                aArr[i][j] -= bArr[i][j];
            }
        }

        return a;
    }

    public static Matrix times(@NonNull Matrix a, @NonNull Matrix b) {
        checkNotNull(a);
        checkNotNull(b);
        checkDimensions(a, b);

        return timesTo(a.copy(), b);
    }

    public static Matrix times(@NonNull Matrix matrix, double num) {
        checkNotNull(matrix);

        return timesTo(matrix.copy(), num);
    }

    public static Matrix timesTo(@NonNull Matrix matrix, double num) {
        checkNotNull(matrix);

        final double[][] array = matrix.getArray();

        for (int i = 0, row = matrix.getRow(), col = matrix.getCol(); i < row; ++i) {
            for (int j = 0; j < col; ++j) {
                array[i][j] *= num;
            }
        }

        return matrix;
    }

    public static Matrix timesTo(@NonNull Matrix a, @NonNull Matrix b) {
        checkNotNull(a);
        checkNotNull(b);
        checkDimensions(a, b);

        final int[] shape = a.shape();
        final double[][] aArr = a.getArray();
        final double[][] bArr = b.getArray();

        for (int i = 0, row = shape[0]; i < row; ++i) {
            for (int j = 0, col = shape[1]; j < col; ++j) {
                aArr[i][j] *= bArr[i][j];
            }
        }

        return a;
    }

    public static Matrix dot(@NonNull Matrix a, @NonNull Matrix b) {
        checkNotNull(a);
        checkNotNull(b);
        checkInnerDimensions(a, b);

        final Matrix res = zeros(a.getRow(), b.getCol());
        final double[][] array = res.getArray();
        final int[] shape = res.shape();

        double[] tmpRow;
        double tmpNum;
        final double[] tmpCol = new double[b.getRow()];
        final double[][] aArr = a.getArray();
        final double[][] bArr = b.getArray();

        for (int j = 0, row = shape[0],
             col = shape[1], inner = b.getRow(); j < col; ++j) {

            for (int i = 0; i < inner; ++i) {
                tmpCol[i] = bArr[i][j];
            }

            for (int i = 0; i < row; ++i) {
                tmpRow = aArr[i];
                tmpNum = 0D;

                for (int k = 0; k < inner; ++k) {
                    tmpNum += (tmpRow[k] * tmpCol[k]);
                }

                array[i][j] = tmpNum;
            }
        }

        return res;
    }

    public static Matrix transpose(@NonNull Matrix matrix) {
        checkNotNull(matrix);

        final int[] shape = matrix.shape();
        final Matrix res = zeros(shape[1], shape[0]);

        final double[][] oldArr = matrix.getArray();
        final double[][] newArr = res.getArray();

        for (int i = 0, row = shape[1], col = shape[0]; i < row; ++i) {
            for (int j = 0; j < col; ++j) {
                newArr[i][j] = oldArr[j][i];
            }
        }

        return res;
    }

    public static Matrix zeroLike(Matrix matrix) {
        checkNotNull(matrix);

        return zeros(matrix.shape());
    }

    public static Matrix zeros(@NonNull int... shape) {
        checkExpression(shape != null && shape.length != 2, "Shape is incorrect");

        return new Matrix(new double[shape[0]][shape[1]]);
    }
}
