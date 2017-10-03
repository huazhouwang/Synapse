package io.whz.androidneuralnetwork.pojo.temp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NewModel implements Serializable {
    private String name;
    private double learningRate;
    private int epochs;
    private int dataSize;
    private long timeUsed;
    private double evaluate;
    private int stepEpoch;
    private final List<Double> accuracies;
    private int[] hiddenSizes;

    public NewModel() {
        accuracies = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLearningRate() {
        return this.learningRate;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public int getEpochs() {
        return this.epochs;
    }

    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }

    public int getDataSize() {
        return this.dataSize;
    }

    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }

    public long getTimeUsed() {
        return this.timeUsed;
    }

    public void setTimeUsed(long timeUsed) {
        this.timeUsed = timeUsed;
    }

    public double getEvaluate() {
        return this.evaluate;
    }

    public void setEvaluate(double evaluate) {
        this.evaluate = evaluate;
    }

    public int[] getHiddenSizes() {
        return hiddenSizes;
    }

    public void setHiddenSizes(int[] hiddenSizes) {
        this.hiddenSizes = hiddenSizes;
    }

    public void addAccuracy(Double accuracy) {
        this.accuracies.add(accuracy);
    }

    public List<Double> getAccuracies() {
        return this.accuracies;
    }

    public double getLastAccuracy() {
        if (this.accuracies.isEmpty()) {
            return 0D;
        }

        return this.accuracies.get(this.accuracies.size() - 1);
    }

    public int getStepEpoch() {
        return stepEpoch;
    }

    public void setStepEpoch(int stepEpoch) {
        this.stepEpoch = stepEpoch;
    }
}