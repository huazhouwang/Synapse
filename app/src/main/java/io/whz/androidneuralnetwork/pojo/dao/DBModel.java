package io.whz.androidneuralnetwork.pojo.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class DBModel {
    @Id(autoincrement = true)
    private Long id;

    @Unique
    private String name;
    private long createdTime;
    private double learningRate;
    private int epochs;
    private int dataSize;
    private long timeUsed;
    private double evaluate;
    private byte[] hiddenSizeBytes;
    private byte[] accuracyBytes;
    private byte[] biasBytes;
    private byte[] weightBytes;
    @Generated(hash = 156277296)
    public DBModel(Long id, String name, long createdTime, double learningRate,
            int epochs, int dataSize, long timeUsed, double evaluate,
            byte[] hiddenSizeBytes, byte[] accuracyBytes, byte[] biasBytes,
            byte[] weightBytes) {
        this.id = id;
        this.name = name;
        this.createdTime = createdTime;
        this.learningRate = learningRate;
        this.epochs = epochs;
        this.dataSize = dataSize;
        this.timeUsed = timeUsed;
        this.evaluate = evaluate;
        this.hiddenSizeBytes = hiddenSizeBytes;
        this.accuracyBytes = accuracyBytes;
        this.biasBytes = biasBytes;
        this.weightBytes = weightBytes;
    }
    @Generated(hash = 1265045159)
    public DBModel() {
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
    public byte[] getHiddenSizeBytes() {
        return this.hiddenSizeBytes;
    }
    public void setHiddenSizeBytes(byte[] hiddenSizeBytes) {
        this.hiddenSizeBytes = hiddenSizeBytes;
    }
    public byte[] getAccuracyBytes() {
        return this.accuracyBytes;
    }
    public void setAccuracyBytes(byte[] accuracyBytes) {
        this.accuracyBytes = accuracyBytes;
    }
    public byte[] getBiasBytes() {
        return this.biasBytes;
    }
    public void setBiasBytes(byte[] biasBytes) {
        this.biasBytes = biasBytes;
    }
    public byte[] getWeightBytes() {
        return this.weightBytes;
    }
    public void setWeightBytes(byte[] weightBytes) {
        this.weightBytes = weightBytes;
    }
}
