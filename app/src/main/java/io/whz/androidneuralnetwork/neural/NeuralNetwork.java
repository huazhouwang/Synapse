package io.whz.androidneuralnetwork.neural;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Arrays;

import io.whz.androidneuralnetwork.component.App;
import io.whz.androidneuralnetwork.element.Scheduler;
import io.whz.androidneuralnetwork.matrix.Matrix;
import io.whz.androidneuralnetwork.util.MatrixUtil;
import io.whz.androidneuralnetwork.util.Precondition;

public class NeuralNetwork {
    private static final String TAG = App.TAG + "-NeuralNetwork";
    
    private static final int INPUT_LAYER_NUMBER = 784;
    private static final int OUTPUT_LAYER_NUMBER = 10;

    private final Matrix[] mBiases;
    private final Matrix[] mWeights;

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

    public void train(int epochs, double learningRate,
                      @NonNull Batch trainBatch, @Nullable Batch validateBatch) {
        Precondition.checkArgument(epochs > 0, "Epochs must greater than 0");
        Precondition.checkArgument(learningRate > 0D, "Learning rate must greater than 0");
        Precondition.checkNotNull(trainBatch);

        Scheduler.Secondary.execute(new TrainRunnable(epochs, learningRate, trainBatch,
                validateBatch, mBiases, mWeights));
    }

    private static class TrainRunnable implements Runnable {
        private final int mEpochs;
        private final double mLearningRate;
        private final Batch mTraining;
        private final Batch mValidation;
        private final Matrix[] mBiases;
        private final Matrix[] mWeights;

        private TrainRunnable(int epochs, double learningRate, Batch training,
                              Batch validation, Matrix[] biases, Matrix[] weights) {
            mEpochs = epochs;
            mLearningRate = learningRate;
            mTraining = training;
            mValidation = validation;
            mBiases = biases;
            mWeights = weights;
        }

        @Override
        public void run() {
            Log.i(TAG, "Start training");

            final boolean test = mValidation != null;

            for (int i = 1; i <= mEpochs; ++i) {
                Log.i(TAG, "Epochs: " + i);

                mTraining.shuffle();
                sgd();

                if (test) {
                    final double rate = evaluate();
                    Log.i(TAG, String.format("Epoch %s: %s", i, rate));
                }
            }
        }

        private void sgd() {
            io.whz.androidneuralnetwork.pojo.neural.Batch batch;

            int i = 0;
            while ((batch = mTraining.next()) != null) {
                ++i;
                Log.i(TAG, "sgd: " + i);
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

        private double evaluate() {
            if (mValidation == null) {
                return 0D;
            }

            io.whz.androidneuralnetwork.pojo.neural.Batch batch;
            int correct = 0;
            int total = 0;

            mValidation.shuffle();
            while ((batch = mValidation.next()) != null) {
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
