package io.whz.synapse.pojo.neural;

import java.io.Serializable;

import io.whz.synapse.matrix.Matrix;

public class Model implements Serializable {
    public static final long serialVersionUID = 0xAAFF;

    private Long id;
    private String name;
    private long createdTime;
    private double learningRate;
    private int epochs;
    private int dataSize;
    private long timeUsed;
    private double evaluate;
    private int stepEpoch;
    private double[] accuracies;
    private int[] hiddenSizes;
    private Matrix[] biases;
    private Matrix[] weights;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public int getEpochs() {
        return epochs;
    }

    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }

    public int getDataSize() {
        return dataSize;
    }

    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }

    public long getTimeUsed() {
        return timeUsed;
    }

    public void setTimeUsed(long timeUsed) {
        this.timeUsed = timeUsed;
    }

    public double getEvaluate() {
        return evaluate;
    }

    public void setEvaluate(double evaluate) {
        this.evaluate = evaluate;
    }

    public int getStepEpoch() {
        return stepEpoch;
    }

    public void setStepEpoch(int stepEpoch) {
        this.stepEpoch = stepEpoch;
    }

    public int[] getHiddenSizes() {
        return hiddenSizes;
    }

    public void setHiddenSizes(int[] hiddenSizes) {
        this.hiddenSizes = hiddenSizes;
    }

    public Matrix[] getBiases() {
        return biases;
    }

    public void setBiases(Matrix[] biases) {
        this.biases = biases;
    }

    public Matrix[] getWeights() {
        return weights;
    }

    public void setWeights(Matrix[] weights) {
        this.weights = weights;
    }

    public double[] getAccuracies() {
        return accuracies;
    }

    public void setAccuracies(double[] accuracies) {
        this.accuracies = accuracies;
    }
}

