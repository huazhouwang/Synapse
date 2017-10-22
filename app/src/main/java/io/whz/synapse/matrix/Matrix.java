package io.whz.synapse.matrix;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Random;

import static io.whz.synapse.matrix.MatrixChecker.checkDimensions;
import static io.whz.synapse.matrix.MatrixChecker.checkExpression;
import static io.whz.synapse.matrix.MatrixChecker.checkInnerDimensions;
import static io.whz.synapse.matrix.MatrixChecker.checkNotNull;
import static io.whz.synapse.matrix.MatrixChecker.verifyArrays;

public class Matrix implements Serializable {
    private final int mRow;
    private final int mCol;
    private final double[] mArray;

    private Matrix(double[] array, int row, int  col) {
        mRow = row;
        mCol = col;
        mArray = array;
    }

    public int[] shape() {
        return new int[]{mRow, mCol};
    }

    public double[] getArray() {
        return mArray;
    }

    public double[][] getArrays() {
        final double[][] arrays = new double[mRow][mCol];

        for (int i = 0; i < mRow; ++i) {
            for (int j = 0; j < mCol; ++j) {
                arrays[i][j] = mArray[i * mCol + j];
            }
        }

        return arrays;
    }

    public Matrix copy() {
        final double[] array = mArray.clone();

        return Matrix.array(array, mRow);
    }

    public int getRow() {
        return mRow;
    }

    public int getCol() {
        return mCol;
    }

    public double get(int i, int j) {
        checkExpression(i >= 0 && i < mRow && j >= 0 && j < mCol,
                "Row or col is out of bounds");

        return mArray[i * mCol + j];
    }

    public void set(int i, int j, double value) {
        checkExpression(i >= 0 && i < mRow && j >= 0 && j < mCol,
                "Row or col is out of bounds");

        mArray[i * mCol + j] = value;
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

    public static Matrix array(@NonNull double[][] arrays) {
        verifyArrays(arrays);

        final int row = arrays.length;
        final int col = arrays[0].length;
        final double[] array = new double[row * col];

        for (int i = 0; i < row; ++i) {
            System.arraycopy(arrays[i], 0, array, i * col, col);
        }

        return Matrix.array(array, row);
    }

    public static Matrix array(@NonNull double[] array, int row) {
        checkNotNull(array);
        checkExpression(row > 0,
                "Row should be positive");

        final int col = array.length / row;
        checkExpression(row * col == array.length,
                "Array length must be a multiple of row");

        return new Matrix(array.clone(), row, col);
    }

    public static Matrix randn(int row, int col) {
        checkDimensions(row, col);

        final Matrix matrix = Matrix.zeros(row, col);
        final double[] array = matrix.getArray();
        final Random random = new Random(System.currentTimeMillis());

        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = random.nextGaussian();
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

        final double[] aArr = a.getArray();
        final double[] bArr = b.getArray();

        for (int i = 0, len = aArr.length; i < len; ++i) {
            aArr[i] += bArr[i];
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

        final double[] aArr = a.getArray();
        final double[] bArr = b.getArray();

        for (int i = 0, len = aArr.length; i < len; ++i) {
            aArr[i] -= bArr[i];
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

        final double[] array = matrix.getArray();

        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] *= num;
        }

        return matrix;
    }

    public static Matrix timesTo(@NonNull Matrix a, @NonNull Matrix b) {
        checkNotNull(a);
        checkNotNull(b);
        checkDimensions(a, b);

        final double[] aArr = a.getArray();
        final double[] bArr = b.getArray();

        for (int i = 0, len = aArr.length; i < len; ++i) {
            aArr[i] *= bArr[i];
        }

        return a;
    }

    public static Matrix dot(@NonNull Matrix a, @NonNull Matrix b) {
        checkNotNull(a);
        checkNotNull(b);
        checkInnerDimensions(a, b);

        final Matrix c = zeros(a.getRow(), b.getCol());
        final double[] cArr = c.getArray();

        final double[] aArr = a.getArray();
        final double[] bArr = b.getArray();

        final int aRow = a.getRow();
        final int bCol = b.getCol();
        final int aCol = a.getCol();
        double tmpNum;

        for (int i = 0; i < aRow; ++i) {
            for (int j = 0; j < bCol; ++j) {
                tmpNum = 0;

                for (int k = 0; k < aCol; ++k) {
                    tmpNum += aArr[aCol * i + k] * bArr[bCol * k + j];
                }

                cArr[i * bCol + j] = tmpNum;
            }
        }

        return c;
    }

    public static Matrix transpose(@NonNull Matrix a) {
        checkNotNull(a);

        final int aRow = a.getRow();
        final int aCol = a.getCol();
        final Matrix b = zeros(aCol, aRow);

        final double[] aArr = a.getArray();
        final double[] bArr = b.getArray();

        for (int i = 0; i < aRow; ++i) {
            for (int j = 0; j < aCol; ++j) {
                bArr[j * aRow + i] = aArr[i * aCol + j];
            }
        }

        return b;
    }

    public static Matrix zeroLike(Matrix matrix) {
        checkNotNull(matrix);

        return zeros(matrix.shape());
    }

    public static Matrix zeros(@NonNull int... shape) {
        checkNotNull(shape);
        checkExpression( shape.length == 2, "Shape is incorrect");

        return new Matrix(new double[shape[0] * shape[1]],
                shape[0], shape[1]);
    }
}
