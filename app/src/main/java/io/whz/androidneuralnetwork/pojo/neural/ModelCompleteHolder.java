package io.whz.androidneuralnetwork.pojo.neural;

public class ModelCompleteHolder {
    public final NeuralModel model;
    public final double[] accuracies;
    public final long usedTime;

    public ModelCompleteHolder(NeuralModel model, long usedTime, double[] accuracies) {
        this.model = model;
        this.accuracies = accuracies;
        this.usedTime = usedTime;
    }
}
