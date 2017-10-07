package io.whz.androidneuralnetwork.pojo.multiple.item;

import android.support.annotation.NonNull;

import io.whz.androidneuralnetwork.pojo.dao.Model;

public class TrainingModelItem {
    private final Model mModel;
    private final int mCurStep;

    public TrainingModelItem(@NonNull Model model) {
        mModel = model;
        mCurStep = model.getStepEpoch();
    }

    public Model getModel() {
        return mModel;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof TrainingModelItem)
                && ((TrainingModelItem) obj).mCurStep == this.mCurStep;
    }
}
