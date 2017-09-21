package io.whz.androidneuralnetwork.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Arrays;

import io.whz.androidneuralnetwork.App;
import io.whz.androidneuralnetwork.components.Scheduler;
import io.whz.androidneuralnetwork.matrix.Matrix;
import io.whz.androidneuralnetwork.pojos.Batch;

public class NeuralNetwork {
    private static final String TAG = App.TAG + "-NeuralNetwork";
    
    private static final int INPUT_LAYER_NUMBER = 784;
    private static final int OUTPUT_LAYER_NUMBER = 10;

    private final Matrix[] mBiases;
    private final Matrix[] mWeights;

    public NeuralNetwork(@NonNull int... hiddenLayerSizes) {
        Preconditions.checkNotNull(hiddenLayerSizes, "Hidden layers must not be null!");

        final int hiddenLen;
        Preconditions.checkArgument((hiddenLen = hiddenLayerSizes.length) != 0, "Size of Hidden layers must not be zero!");

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

        return MatrixUtils.randns(rows, cols);
    }

    private Matrix[] newBiasesMatrix(int[] totalSize) {
        final int len = totalSize.length - 1;

        final int[] rows = new int[len];
        System.arraycopy(totalSize, 1, rows, 0, rows.length);

        final int[] cols = new int[len];
        Arrays.fill(cols, 1);

        return MatrixUtils.randns(rows, cols);
    }

    public void train(int epochs, double learningRate,
                      @NonNull Batches trainBatch, @Nullable Batches validateBatch) {
        Preconditions.checkArgument(epochs > 0, "Epochs must greater than 0");
        Preconditions.checkArgument(learningRate > 0D, "Learning rate must greater than 0");
        Preconditions.checkNotNull(trainBatch);

        Scheduler.Secondary.execute(new TrainRunnable(epochs, learningRate, trainBatch,
                validateBatch, mBiases, mWeights));
    }

    private static class TrainRunnable implements Runnable {
        private final int mEpochs;
        private final double mLearningRate;
        private final Batches mTraining;
        private final Batches mValidation;
        private final Matrix[] mBiases;
        private final Matrix[] mWeights;

        private TrainRunnable(int epochs, double learningRate, Batches training,
                              Batches validation, Matrix[] biases, Matrix[] weights) {
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
            Batch batch;

            int i = 0;
            while ((batch = mTraining.next()) != null) {
                ++i;
                Log.i(TAG, "sgd: " + i);
                updateMiniBatch(batch.inputs, batch.targets);
            }
        }

        private void updateMiniBatch(Matrix[] inputs, Matrix[] targets) {
            final Matrix[] batchWeights = MatrixUtils.zerosLike(mWeights);
            final Matrix[] batchBiases = MatrixUtils.zerosLike(mBiases);
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
            Matrix delta = error.times(ActivateFunctions.sigmoidPrime(activations[aLen - 1]));

            batchBiases[bLen - 1].plusTo(delta);
            batchWeights[bLen - 1].plusTo(delta.dot(activations[aLen - 2].transpose()));

            for (int i = 2; i < aLen; ++i) {
                final Matrix prime = ActivateFunctions.sigmoidPrime(activations[aLen - i]);
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

                activations[i + 1] = ActivateFunctions.sigmoid(matrix);
            }

            return activations;
        }

        private double evaluate() {
            if (mValidation == null) {
                return 0D;
            }

            Batch batch;
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

                    if (MatrixUtils.argmax(output) == MatrixUtils.index(targets[i])) {
                        ++correct;
                    }
                }
            }

            return total != 0 ? (double) correct / total : 0D;
        }
    }

    public int predict(@NonNull Matrix input) {
        return MatrixUtils.argmax(feedForward(mWeights, mBiases, input));
    }

    private static Matrix feedForward(@NonNull Matrix[] weights, @NonNull Matrix[] biases,
                                      @NonNull Matrix input) {
        final int len = weights.length;
        Matrix res = input;

        for (int i = 0; i < len; ++i) {
            res = ActivateFunctions.sigmoid(weights[i].dot(res).plus(biases[i]));
        }

        return res;
    }
}
