package io.whz.androidneuralnetwork.pojo.multiple.item;

import android.support.annotation.NonNull;

import io.whz.androidneuralnetwork.pojo.dao.Model;
import io.whz.androidneuralnetwork.util.Precondition;

public class TrainedModelItem {
    private final Model mModel;

    public TrainedModelItem(@NonNull Model model) {
        mModel = Precondition.checkNotNull(model);
    }

    public Model getModel() {
        return mModel;
    }
}
