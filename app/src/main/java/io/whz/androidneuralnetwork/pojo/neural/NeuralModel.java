package io.whz.androidneuralnetwork.pojo.neural;

import java.io.Serializable;

public class NeuralModel implements Serializable {
    public final String name;
    public final int[] hiddenSizes;
    public final double learningRate;
    public final int epochs;
    public final int trainingSize;

    public NeuralModel(String name, int[] hiddenSizes, double learningRate, int epochs, int trainingSize) {
        this.name = name;
        this.hiddenSizes = hiddenSizes;
        this.learningRate = learningRate;
        this.epochs = epochs;
        this.trainingSize = trainingSize;
    }
}
