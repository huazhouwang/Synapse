package io.whz.androidneuralnetwork.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import io.whz.androidneuralnetwork.matrix.Matrix;

public class DbHelper {

    @Nullable
    public static byte[] convert2ByteArray(int... array) {
        if (array == null) {
            return null;
        }

        ByteBuffer buffer = null;

        try {
            buffer = ByteBuffer.allocate(array.length << 2);

            for (int i : array) {
                buffer.putInt(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
            buffer = null;
        }

        return buffer == null ? null : buffer.array();
    }

    @Nullable
    public static byte[] convert2ByteArray(List<Double> list) {
        if (list == null) {
            return  null;
        }

        ByteBuffer buffer = null;

        try {
            buffer = ByteBuffer.allocate(list.size() << 3);

            for (double i : list) {
                buffer.putDouble(i);
            }
        } catch (Exception e) {
            e.printStackTrace();

            buffer = null;
        }

        return buffer == null ? null : buffer.array();
    }

    @Nullable
    public static byte[] convert2ByteArray(Matrix... matrices) {
        if (matrices == null) {
            return null;
        }

        ByteBuffer buffer = null;
        int sum = 0;

        sum += 4;

        for (Matrix matrix : matrices) {
            sum += calMatrixLen(matrix);
        }

        try {
            buffer = ByteBuffer.allocate(sum);

            buffer.putInt(matrices.length);

            for (Matrix matrix : matrices) {
                buffer.putInt(matrix.getRow());
                buffer.putInt(matrix.getCol());

                final double[] doubles = matrix.getArray();

                for (double d : doubles) {
                    buffer.putDouble(d);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return buffer == null ? null : buffer.array();
    }

    private static int calMatrixLen(@NonNull Matrix matrix) {
        int sum = 0;

        sum += 8;
        sum += (matrix.getArray().length << 3);

        return sum;
    }

    @Nullable
    public static Matrix[] byteArray2MatrixArray(byte... array) {
        if (array == null) {
            return null;
        }

        Matrix[] res = null;

        try {
            final ByteBuffer buffer = ByteBuffer.wrap(array);

            final int len = buffer.getInt();
            res = new Matrix[len];

            for (int i = 0; i < len; ++i) {
                final int row = buffer.getInt();
                final int col = buffer.getInt();
                final double[] doubles = new double[row * col];

                for (int j = 0, jLen = doubles.length; j < jLen; ++j) {
                    doubles[j] = buffer.getDouble();
                }

                res[i] = Matrix.array(doubles, row);
            }
        } catch (Exception e) {
            e.printStackTrace();

            res = null;
        }

        return res;
    }

    @Nullable
    public static int[] byteArray2IntArray(byte... array) {
        if (array == null) {
            return null;
        }

        final int[] res = new int[array.length >> 2];

        try {
            final IntBuffer buffer = ByteBuffer.wrap(array).asIntBuffer();

            for (int i = 0, iLen = res.length; i < iLen; ++i) {
                res[i] = buffer.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    @Nullable
    public static List<Double> byteArray2DoubleList(byte... array) {
        if (array == null) {
            return null;
        }

        final int len = array.length;
        final List<Double> res = new ArrayList<>(len >> 3);

        try {
            final DoubleBuffer buffer = ByteBuffer.wrap(array).asDoubleBuffer();

            for (int i = 0, iLen = len >> 3; i < iLen; ++i) {
                res.add(buffer.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }
}
