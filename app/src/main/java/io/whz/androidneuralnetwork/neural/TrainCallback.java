package io.whz.androidneuralnetwork.neural;

public interface TrainCallback {
    void onStart();

    boolean onUpdate(int progress, double accurate);

    void onEvaluate();

    void onComplete(double evaluate);
}
