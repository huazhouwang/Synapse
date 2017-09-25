package io.whz.androidneuralnetwork.pojos;

import java.io.Serializable;

public class NeuralModel implements Serializable {
    public final int[] hiddenSizes;
    public final double learningRate;
    public final int epochs;
    public final int trainingSize;

    public NeuralModel(int[] hiddenSizes, double learningRate, int epochs, int trainingSize) {
        this.hiddenSizes = hiddenSizes;
        this.learningRate = learningRate;
        this.epochs = epochs;
        this.trainingSize = trainingSize;
    }
}
