package io.whz.androidneuralnetwork.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import io.whz.androidneuralnetwork.matrix.Matrix;
import io.whz.androidneuralnetwork.pojo.dao.DBModel;
import io.whz.androidneuralnetwork.pojo.neural.Model;

public class DbHelper {
    public static Model dbModel2Model(@NonNull DBModel dbModel) {
        Precondition.checkNotNull(dbModel);

        final Model model = new Model();

        model.setId(dbModel.getId());
        model.setName(dbModel.getName());
        model.setCreatedTime(dbModel.getCreatedTime());
        model.setLearningRate(dbModel.getLearningRate());
        model.setEpochs(dbModel.getEpochs());
        model.setStepEpoch(dbModel.getEpochs());
        model.setDataSize(dbModel.getDataSize());
        model.setTimeUsed(dbModel.getTimeUsed());
        model.setEvaluate(dbModel.getEvaluate());
        model.setHiddenSizes(byteArray2IntArray(dbModel.getHiddenSizeBytes()));
        model.setAccuracies(byteArray2DoubleArray(dbModel.getAccuracyBytes()));
        model.setBiases(byteArray2MatrixArray(dbModel.getBiasBytes()));
        model.setWeights(byteArray2MatrixArray(dbModel.getWeightBytes()));

        return model;
    }

    public static DBModel model2DBModel(@NonNull Model model) {
        Precondition.checkNotNull(model);

        final DBModel dbModel = new DBModel();

        dbModel.setId(model.getId());
        dbModel.setName(model.getName());
        dbModel.setCreatedTime(model.getCreatedTime());
        dbModel.setLearningRate(model.getLearningRate());
        dbModel.setEpochs(model.getEpochs());
        dbModel.setDataSize(model.getDataSize());
        dbModel.setTimeUsed(model.getTimeUsed());
        dbModel.setEvaluate(model.getEvaluate());
        dbModel.setHiddenSizeBytes(convert2ByteArray(model.getHiddenSizes()));
        dbModel.setAccuracyBytes(convert2ByteArray(model.getAccuracies()));
        dbModel.setBiasBytes(convert2ByteArray(model.getBiases()));
        dbModel.setWeightBytes(convert2ByteArray(model.getWeights()));

        return dbModel;
    }

    @Nullable
    private static byte[] convert2ByteArray(int... array) {
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
    private static byte[] convert2ByteArray(double... array) {
        if (array == null) {
            return  null;
        }

        ByteBuffer buffer = null;

        try {
            buffer = ByteBuffer.allocate(array.length << 3);

            for (double i : array) {
                buffer.putDouble(i);
            }
        } catch (Exception e) {
            e.printStackTrace();

            buffer = null;
        }

        return buffer == null ? null : buffer.array();
    }

    @Nullable
    private static byte[] convert2ByteArray(Matrix... matrices) {
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
    private static Matrix[] byteArray2MatrixArray(byte... array) {
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
    private static int[] byteArray2IntArray(byte... array) {
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
    private static double[] byteArray2DoubleArray(byte... array) {
        if (array == null) {
            return null;
        }

        final int len = array.length;
        final double[] res = new double[len >> 3];

        try {
            final DoubleBuffer buffer = ByteBuffer.wrap(array).asDoubleBuffer();

            for (int i = 0, iLen = len >> 3; i < iLen; ++i) {
                res[i] = buffer.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }
}
