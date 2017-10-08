package io.whz.androidneuralnetwork.neural;

import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.Arrays;

import io.whz.androidneuralnetwork.component.App;
import io.whz.androidneuralnetwork.matrix.Matrix;
import io.whz.androidneuralnetwork.pojo.neural.Batch;
import io.whz.androidneuralnetwork.util.MatrixUtil;
import io.whz.androidneuralnetwork.util.Precondition;

public class NeuralNetwork {
    private static final String TAG = App.TAG + "-NeuralNetwork";
    
    public static final int INPUT_LAYER_NUMBER = 784;
    public static final int OUTPUT_LAYER_NUMBER = 10;

    private final Matrix[] mWeights;
    private final Matrix[] mBiases;

    public NeuralNetwork(@NonNull int... hiddenLayerSizes) {
        Precondition.checkNotNull(hiddenLayerSizes, "Hidden layers must not be null!");

        final int hiddenLen;
        Precondition.checkArgument((hiddenLen = hiddenLayerSizes.length) != 0, "Size of Hidden layers must not be zero!");

        final int[] totalSizes = new int[hiddenLen + 2];
        System.arraycopy(hiddenLayerSizes, 0, totalSizes, 1, hiddenLen);
        totalSizes[0] = INPUT_LAYER_NUMBER;
        totalSizes[totalSizes.length - 1] = OUTPUT_LAYER_NUMBER;

        mBiases = newBiasesMatrix(totalSizes);
        mWeights = newWeightsMatrices(totalSizes);
    }

    public NeuralNetwork(@NonNull Matrix[] weights, @NonNull Matrix[] biases) {
        Precondition.checkNotNull(weights);
        Precondition.checkNotNull(biases);

        mWeights = weights;
        mBiases = biases;
    }

    private Matrix[] newWeightsMatrices(int[] totalSize) {
        final int len = totalSize.length - 1;

        final int[] rows = new int[len];
        System.arraycopy(totalSize, 1, rows, 0, len);

        final int[] cols = new int[len];
        System.arraycopy(totalSize, 0, cols, 0, len);

        return MatrixUtil.randns(rows, cols);
    }

    private Matrix[] newBiasesMatrix(int[] totalSize) {
        final int len = totalSize.length - 1;

        final int[] rows = new int[len];
        System.arraycopy(totalSize, 1, rows, 0, rows.length);

        final int[] cols = new int[len];
        Arrays.fill(cols, 1);

        return MatrixUtil.randns(rows, cols);
    }

    public Matrix[] getBiases() {
        return mBiases;
    }

    public Matrix[] getWeights() {
        return mWeights;
    }

    public void train(int epochs, double learningRate,
                      @NonNull DataSet trainDataSet, @NotNull DataSet validateDataSet,
                      @NonNull DataSet testDataSet, @NonNull TrainCallback callback) {
        Precondition.checkArgument(epochs > 0, "Epochs must greater than 0");
        Precondition.checkArgument(learningRate > 0D, "Learning rate must greater than 0");
        Precondition.checkNotNull(trainDataSet);

        new TrainRunnable(epochs, learningRate, trainDataSet,
                validateDataSet, testDataSet, mBiases, mWeights, callback)
                .run();
    }

    private static class TrainRunnable implements Runnable {
        private final int mEpochs;
        private final double mLearningRate;
        private final DataSet mTraining;
        private final DataSet mValidation;
        private final DataSet mTest;
        private final Matrix[] mBiases;
        private final Matrix[] mWeights;
        private TrainCallback mCallback;

        private TrainRunnable(int epochs, double learningRate, DataSet training,
                              DataSet validation, DataSet test, Matrix[] biases, Matrix[] weights,
                              TrainCallback callback) {
            mEpochs = epochs;
            mLearningRate = learningRate;
            mTraining = training;
            mValidation = validation;
            mTest = test;
            mBiases = biases;
            mWeights = weights;
            mCallback = callback;
        }

        @Override
        public void run() {
            mCallback.onStart();

            for (int i = 1; i <= mEpochs; ++i) {
                mTraining.shuffle();
                sgd();

                final double rate = evaluate(mValidation);

                if (!mCallback.onUpdate(i, rate)) {
                    mCallback.onTrainComplete();
                    return;
                }
            }

            mCallback.onTrainComplete();

            final double rate = evaluate(mTest);
            mCallback.onEvaluateComplete(rate);
        }

        private void sgd() {
            Batch batch;

            while ((batch = mTraining.nextBatch()) != null) {
                updateMiniBatch(batch.inputs, batch.targets);
            }
        }

        private void updateMiniBatch(Matrix[] inputs, Matrix[] targets) {
            final Matrix[] batchWeights = MatrixUtil.zerosLike(mWeights);
            final Matrix[] batchBiases = MatrixUtil.zerosLike(mBiases);
            final int len = inputs.length;

            for (int i = 0; i < len; ++i) {
                feed(inputs[i], targets[i], batchWeights, batchBiases);
            }

            update(batchWeights, batchBiases, len);
        }

        private void update(Matrix[] batchWeights, Matrix[] batchBiases, int count) {
            final int len = mWeights.length;
            final double tmp = mLearningRate / count;

            for (int i = 0; i < len; ++i) {
                mWeights[i].minusTo(batchWeights[i].times(tmp));
                mBiases[i].minusTo(batchBiases[i].times(tmp));
            }
        }

        private void feed(Matrix input, Matrix target, Matrix[] batchWeights, Matrix[] batchBiases) {
            final Matrix[] activations = forwardPropagation(input);
            final int aLen = activations.length;
            final int bLen = batchWeights.length;

            final Matrix error = activations[aLen - 1].minus(target);
            Matrix delta = error.times(ActivateFunction.sigmoidPrime(activations[aLen - 1]));

            batchBiases[bLen - 1].plusTo(delta);
            batchWeights[bLen - 1].plusTo(delta.dot(activations[aLen - 2].transpose()));

            for (int i = 2; i < aLen; ++i) {
                final Matrix prime = ActivateFunction.sigmoidPrime(activations[aLen - i]);
                delta = mWeights[bLen - i + 1].transpose()
                        .dot(delta)
                        .times(prime);

                batchBiases[bLen - i].plusTo(delta);
                batchWeights[bLen - i].plusTo(delta.dot(activations[aLen - i - 1].transpose()));
            }
        }

        private Matrix[] forwardPropagation(@NonNull Matrix input) {
            final int len = mWeights.length;
            final Matrix[] activations = new Matrix[len + 1];
            activations[0] = input;

            for (int i = 0; i < len; ++i) {
                final Matrix matrix = mWeights[i]
                        .dot(activations[i])
                        .plus(mBiases[i]);

                activations[i + 1] = ActivateFunction.sigmoid(matrix);
            }

            return activations;
        }

        private double evaluate(@NonNull DataSet dataSet) {
            Batch batch;
            int correct = 0;
            int total = 0;

            dataSet.shuffle();

            while ((batch = dataSet.nextBatch()) != null) {
                final Matrix[] inputs = batch.inputs;
                final Matrix[] targets = batch.targets;
                final int len = inputs.length;
                total += len;

                for (int i = 0; i < len; ++i) {
                    final Matrix output = feedForward(mWeights, mBiases, inputs[i]);

                    if (MatrixUtil.argmax(output) == MatrixUtil.index(targets[i])) {
                        ++correct;
                    }
                }
            }

            return total != 0 ? (double) correct / total : 0D;
        }
    }

    public int predict(@NonNull Matrix input) {
        return MatrixUtil.argmax(feedForward(mWeights, mBiases, input));
    }

    private static Matrix feedForward(@NonNull Matrix[] weights, @NonNull Matrix[] biases,
                                      @NonNull Matrix input) {
        final int len = weights.length;
        Matrix res = input;

        for (int i = 0; i < len; ++i) {
            res = ActivateFunction.sigmoid(weights[i].dot(res).plus(biases[i]));
        }

        return res;
    }
}
