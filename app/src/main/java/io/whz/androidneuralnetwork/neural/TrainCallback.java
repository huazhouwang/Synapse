package io.whz.androidneuralnetwork.neural;

import android.support.annotation.Nullable;

public interface TrainCallback {
    void onStart();

    boolean onUpdate(int progress, double accurate);

    void onComplete(long usedTime, @Nullable double[] accuracies);
}
