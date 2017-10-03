package io.whz.androidneuralnetwork.pojo.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class TrainedModel {
    @Id(autoincrement = true)
    private Long id;

    @Unique
    private String name;
    private String hiddenSizes;
    private double learningRate;
    private int epochs;
    private int dataSize;

    private long timeUsed;
    private String accuracies;
    private double evaluate;
    private long createdTime;
    @Generated(hash = 1388358350)
    public TrainedModel(Long id, String name, String hiddenSizes,
            double learningRate, int epochs, int dataSize, long timeUsed,
            String accuracies, double evaluate, long createdTime) {
        this.id = id;
        this.name = name;
        this.hiddenSizes = hiddenSizes;
        this.learningRate = learningRate;
        this.epochs = epochs;
        this.dataSize = dataSize;
        this.timeUsed = timeUsed;
        this.accuracies = accuracies;
        this.evaluate = evaluate;
        this.createdTime = createdTime;
    }
    @Generated(hash = 101113492)
    public TrainedModel() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getHiddenSizes() {
        return this.hiddenSizes;
    }
    public void setHiddenSizes(String hiddenSizes) {
        this.hiddenSizes = hiddenSizes;
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
    public String getAccuracies() {
        return this.accuracies;
    }
    public void setAccuracies(String accuracies) {
        this.accuracies = accuracies;
    }
    public double getEvaluate() {
        return this.evaluate;
    }
    public void setEvaluate(double evaluate) {
        this.evaluate = evaluate;
    }
    public long getCreatedTime() {
        return this.createdTime;
    }
    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

}
