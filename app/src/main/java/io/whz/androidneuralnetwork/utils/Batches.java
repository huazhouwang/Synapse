package io.whz.androidneuralnetwork.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.whz.androidneuralnetwork.App;
import io.whz.androidneuralnetwork.pojos.Batch;
import io.whz.androidneuralnetwork.pojos.Digit;

public class Batches {
    private static final String TAG = App.TAG + "-Batches";
    private static final int DEFAULT_MIN_BATCH = 20;
    private static final int DIGIT_COUNT = 10;

    private final int mMiniBatch;
    private final List<File> mBatchFiles = new ArrayList<>();
    private final List<File> mCurFiles = new ArrayList<>();
    private final LruCache<String, Digit[]> mCache = BatchCache.getInstance();// TODO: 09/09/2017 缓存导致占用内存过高，命中率太低，去除

    private Digit[] mCurDigits;
    private int mRemain;
    private int mIndex;

    public Batches(@NonNull File... batchFiles) {
        mMiniBatch = DEFAULT_MIN_BATCH;
        mBatchFiles.addAll(Arrays.asList(batchFiles));
    }

    public Batches(int miniBatch, @NonNull File... batchFiles) {
        Preconditions.checkArgument(miniBatch > 0, "MiniBatch must be positive");

        mMiniBatch = miniBatch;
        mBatchFiles.addAll(Arrays.asList(batchFiles));
    }

    public void reset() {
        mCurFiles.clear();
        mCurFiles.addAll(mBatchFiles);
        mCurDigits = null;
        mRemain = 0;
        mIndex = 0;
    }

    @Nullable
    public Batch next() {
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
        final double[][] inputs = new double[len][digits.get(0).pixels.length];
        final double[][] outputs = new double[len][DIGIT_COUNT];
        Digit digit;

        for (int i = 0; i < len; ++i) {
            digit = digits.get(i);
            inputs[i] = digit.pixels;
            outputs[i] = oneHot(digit.actual);
        }

        return new Batch(inputs, outputs);
    }

    private double[] oneHot(int actual) {
        final double[] res = new double[DIGIT_COUNT];
        res[actual] = 1F;

        return res;
    }

    @Nullable
    private Digit[] nextDigits(@NonNull File file) {
        final Digit[] res = MNISTUtils.readBatches(file);

        if (res != null && res.length != 0) {
            normalize(res);
        }

        return res;
    }

    private void normalize(@NonNull Digit[] digits) {
        for (Digit digit : digits) {
            final double[] pixels = digit.pixels;

            for (int j = 0, jLen = pixels.length; j < jLen; ++j) {
                pixels[j] /= 0xFF;
            }
        }

        final List<Digit> tmp = new ArrayList<>(Arrays.asList(digits));
        Collections.shuffle(tmp);
        tmp.toArray(digits);
    }

    public void shuffle() {
        Collections.shuffle(mBatchFiles);
        reset();
    }
}
