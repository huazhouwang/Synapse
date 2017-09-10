package io.whz.androidneuralnetwork.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Arrays;

import Jama.Matrix;
import io.whz.androidneuralnetwork.App;
import io.whz.androidneuralnetwork.pojos.Batch;

public class NeuralNetwork {
    private static final String TAG = App.TAG + "-NeuralNetwork";
    
    private final int INPUT_LAYER_NUMBER = 784;
    private final int OUTPUT_LAYER_NUMBER = 10;

    private final int[] mHLSizes;
    private final Matrix[] mBiases;
    private final Matrix[] mWeights;

    public NeuralNetwork(@NonNull int... hiddenLayerSizes) {
        Preconditions.checkNotNull(hiddenLayerSizes, "Hidden layers must not be null!");

        final int hiddenLen;
        Preconditions.checkArgument((hiddenLen = hiddenLayerSizes.length) == 0, "Size of Hidden layers must not be zero!");

        final int[] totalSizes = new int[hiddenLen + 2];
        System.arraycopy(hiddenLayerSizes, 0, totalSizes, 1, hiddenLen);
        totalSizes[0] = INPUT_LAYER_NUMBER;
        totalSizes[totalSizes.length - 1] = OUTPUT_LAYER_NUMBER;

        mHLSizes = totalSizes;
        mBiases = newBiasesMatrix(totalSizes);
        mWeights = newWeightsMatrices(totalSizes);
    }

    private Matrix[] newWeightsMatrices(int[] totalSize) {
        final int len = totalSize.length - 1;

        final int[] rows = new int[len];
        System.arraycopy(totalSize, 0, rows, 0, len);

        final int[] cols = new int[len];
        System.arraycopy(totalSize, 1, rows, 0, len);

        return newRandomMatrix(rows, cols);
    }

    private Matrix[] newBiasesMatrix(int[] totalSize) {
        final int len = totalSize.length - 1;

        final int[] rows = new int[len];
        System.arraycopy(totalSize, 1, rows, 0, rows.length);

        final int[] cols = new int[len];
        Arrays.fill(cols, 1);

        return newRandomMatrix(rows, cols);
    }

    private Matrix[] newRandomMatrix(int[] rows, int[] cols) {
        final int len;
        Preconditions.checkArgument((len = rows.length) == cols.length);

        final Matrix[] matrices = new Matrix[len];

        for (int i = 0; i < len; ++i) {
            matrices[i] = Matrix.random(rows[i], cols[i]);
        }

        return matrices;
    }

    public void train(int epochs, double learningRate,
                      @NonNull Batches trainBatch, @Nullable Batches validateBatch) {
        Preconditions.checkArgument(epochs > 0, "Epochs must greater than 0");
        Preconditions.checkArgument(learningRate > 0F, "Learning rate must greater than 0");
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

            for (int i = 1; i <= mEpochs; ++i) {
                Log.i(TAG, "Epochs: " + i);

                mTraining.shuffle();
                updateMiniBatch();
            }
        }

        private void updateMiniBatch() {
            final Matrix[] batchWeights = MatrixUtils.copyZeroes(mWeights);
            final Matrix[] batchBiases = MatrixUtils.copyZeroes(mBiases);

            Batch batch;
            int i = 0;

            while ((batch = mTraining.next()) != null) {
                ++i;
                sgd(batch.input, batch.target, batchWeights, batchBiases);
            }

            update(batchWeights, batchBiases, i);
        }

        private void update(Matrix[] batchWeights, Matrix[] batchBiases, int count) {
            final int len = mWeights.length;
            final double tmp = mLearningRate / count;

            for (int i = 0; i < len; ++i) {
                mWeights[i].minusEquals(batchWeights[i].times(tmp));
                mBiases[i].minusEquals(batchBiases[i].times(tmp));
            }
        }

        private void sgd(Matrix input, Matrix target, Matrix[] weights, Matrix[] biases) {
            final Matrix[] activations = feedForward(input);
            final int aLen = activations.length;
            final int bLen = weights.length;// aLen = bLen + 1

            Matrix delta = backPropagation(activations[aLen - 1], target);
            biases[bLen - 1].plusEquals(delta);
            weights[bLen - 1].plusEquals(activations[aLen - 2].transpose().times(delta));

            for (int i = aLen - 2; i >= 0; --i) {
                Matrix prime = ActivateFuns.sigmoidPrime(activations[i]);
            }
        }

        private Matrix backPropagation(Matrix output, Matrix target) {
            final Matrix error = output.minus(target); // TODO: 10/09/2017 还是 target - output?
            final Matrix prime = ActivateFuns.sigmoidPrime(output);

            return error.arrayTimes(prime);
        }

        private Matrix[] feedForward(@NonNull Matrix input) {
            final int len = mWeights.length;
            final Matrix[] activations = new Matrix[len + 1];
            activations[0] = input;

            for (int i = 0; i < len; ++i) {
                final Matrix matrix = activations[i]
                        .times(mWeights[i])
                        .plus(mBiases[i]);

                ActivateFuns.sigmoidSelf(matrix);
                activations[i + 1] = matrix;
            }

            return activations;
        }
    }
}
