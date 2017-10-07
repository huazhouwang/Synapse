package io.whz.androidneuralnetwork.pojo.multiple.item;

import android.support.annotation.NonNull;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof TrainedModelItem)
                && Objects.equals(((TrainedModelItem) obj).getModel().getId(), this.getModel().getId());
    }
}
