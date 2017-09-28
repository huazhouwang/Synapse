package io.whz.androidneuralnetwork.pojo.neural;

public class CompletedModel {
    public final NeuralModel model;
    public final double[] accuracies;
    public final long usedTime;

    public CompletedModel(NeuralModel model, long usedTime, double[] accuracies) {
        this.model = model;
        this.accuracies = accuracies;
        this.usedTime = usedTime;
    }
}
