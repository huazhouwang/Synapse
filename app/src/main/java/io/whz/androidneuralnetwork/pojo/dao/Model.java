package io.whz.androidneuralnetwork.pojo.dao;

import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Model implements Serializable {
    public static final long serialVersionUID = 0xff;

    @Id(autoincrement = true)
    private Long id;

    @Unique
    private String name;
    private String hiddenSizeString;
    private String accuracyString;
    private long createdTime;
    private double learningRate;
    private int epochs;
    private int dataSize;
    private long timeUsed;
    private double evaluate;

    @Transient
    private int stepEpoch;

    @Transient
    private final List<Double> accuracies = new ArrayList<>();

    @Transient
    private int[] hiddenSizes;

    @Generated(hash = 725346043)
    public Model(Long id, String name, String hiddenSizeString,
            String accuracyString, long createdTime, double learningRate,
            int epochs, int dataSize, long timeUsed, double evaluate) {
        this.id = id;
        this.name = name;
        this.hiddenSizeString = hiddenSizeString;
        this.accuracyString = accuracyString;
        this.createdTime = createdTime;
        this.learningRate = learningRate;
        this.epochs = epochs;
        this.dataSize = dataSize;
        this.timeUsed = timeUsed;
        this.evaluate = evaluate;
    }

    @Generated(hash = 2118404446)
    public Model() {
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

    public void addAllAccuracy(@NonNull List<Double> accuracies) {
        this.accuracies.addAll(accuracies);
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

    public String getHiddenSizeString() {
        return this.hiddenSizeString;
    }

    public void setHiddenSizeString(String hiddenSizeString) {
        this.hiddenSizeString = hiddenSizeString;
    }

    public String getAccuracyString() {
        return this.accuracyString;
    }

    public void setAccuracyString(String accuracyString) {
        this.accuracyString = accuracyString;
    }

    public long getCreatedTime() {
        return this.createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
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
}