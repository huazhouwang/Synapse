package io.whz.androidneuralnetwork.neural;

public interface TrainCallback {
    void onStart();

    boolean onUpdate(int progress, double accurate);

    void onComplete();
}
