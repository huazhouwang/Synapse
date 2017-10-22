package io.whz.synapse.neural;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.whz.synapse.component.App;
import io.whz.synapse.matrix.Matrix;
import io.whz.synapse.pojo.neural.Batch;
import io.whz.synapse.pojo.neural.Digit;
import io.whz.synapse.util.Precondition;

public class DataSet {
    private static final String TAG = App.TAG + "-DataSet";
    private static final int DEFAULT_MIN_BATCH = 20;
    private static final int DIGIT_COUNT = 10;
    private static final int PIXEL_COUNT = 784;

    private final int mMiniBatch;
    private final List<File> mBatchFiles = new ArrayList<>();
    private final List<File> mCurFiles = new ArrayList<>();

    private Digit[] mCurDigits;
    private int mRemain;
    private int mIndex;

    public DataSet(@NonNull File... batchFiles) {
        mMiniBatch = DEFAULT_MIN_BATCH;
        mBatchFiles.addAll(Arrays.asList(batchFiles));

        reset();
    }

    public DataSet(int miniBatch, @NonNull File... batchFiles) {
        Precondition.checkArgument(miniBatch <= 0, "MiniBatch must be positive");

        mMiniBatch = miniBatch;
        mBatchFiles.addAll(Arrays.asList(batchFiles));

        reset();
    }

    private void reset() {
        mCurFiles.clear();
        mCurFiles.addAll(mBatchFiles);
        mCurDigits = null;
        mRemain = 0;
        mIndex = 0;
    }

    @Nullable
    public Batch nextBatch() {
        final List<Digit> batch = new ArrayList<>();
        int need = mMiniBatch;

        while (need > 0) {
            while (mRemain <= 0 && !mCurFiles.isEmpty()) {
                final File file = mCurFiles.remove(0);
                mCurDigits = nextDigits(file);

                if (mCurDigits != null && mCurDigits.length != 0) {
                    mRemain = mCurDigits.length;
                    mIndex = 0;
                }
            }

            if (mRemain <= 0) {
                break;
            }

            final int size = Math.min(need, mRemain);
            need -= size;
            mRemain -= size;

            final Digit[] window = new Digit[size];
            System.arraycopy(mCurDigits, mIndex, window, 0, size);
            mIndex += size;
            batch.addAll(Arrays.asList(window));
        }

        return convert2Matrix(batch);
    }

    private Batch convert2Matrix(@NonNull List<Digit> digits) {
        if (digits.isEmpty()) {
            return null;
        }

        final int len = digits.size();

        final Matrix[] inputs = new Matrix[len];
        final Matrix[] targets = new Matrix[len];
        Digit digit;

        for (int i = 0; i < len; ++i) {
            digit = digits.get(i);

            inputs[i] = Matrix.array(digit.colors, PIXEL_COUNT);
            targets[i] = oneHot(digit.label);
        }

        return new Batch(inputs, targets);
    }

    private Matrix oneHot(int actual) {
        final double[] doubles = new double[DIGIT_COUNT];
        doubles[actual] = 1D;

        return Matrix.array(doubles, DIGIT_COUNT);
    }

    @Nullable
    private Digit[] nextDigits(@NonNull File file) {
        final Digit[] res = MNISTUtil.readBatches(file);

        return res == null ? null : normalize(res);
    }

    private Digit[] normalize(@NonNull Digit[] digits) {
        final List<Digit> tmp = new ArrayList<>(Arrays.asList(digits));
        Collections.shuffle(tmp);
        tmp.toArray(digits);

        return digits;
    }

    public void shuffle() {
        Collections.shuffle(mBatchFiles);
        reset();
    }
}
