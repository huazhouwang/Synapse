package io.whz.androidneuralnetwork.neural;

public interface TrainCallback {
    void onStart();

    boolean onUpdate(int progress, double accurate);

    void onTrainComplete();

    void onInterrupted();

    void onEvaluateComplete(double evaluate);
}
