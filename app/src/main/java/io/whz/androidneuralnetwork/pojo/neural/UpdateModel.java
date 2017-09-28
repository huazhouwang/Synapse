package io.whz.androidneuralnetwork.pojo.neural;

public class UpdateModel {
    public final NeuralModel model;
    public final double accuracy;
    public final int epoch;

    public UpdateModel(NeuralModel model, int epoch, double accuracy)  {
        this.model = model;
        this.accuracy = accuracy;
        this.epoch = epoch;
    }
}
