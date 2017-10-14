package io.whz.androidneuralnetwork.pojo.multiple.item;

import android.support.annotation.NonNull;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.whz.androidneuralnetwork.pojo.dao.Model;
import io.whz.androidneuralnetwork.util.DbHelper;
import io.whz.androidneuralnetwork.util.Precondition;

public class TrainedModelItem {
    private final Model mModel;
    private List<Entry> mEntries;

    public TrainedModelItem(@NonNull Model model) {
        mModel = Precondition.checkNotNull(model);
    }

    private List<Entry> format() {
        final List<Double> doubles = getModel().getAccuracies();
        final List<Entry> list = new ArrayList<>();

        for (int i = 0, len = doubles.size(); i < len; ++i) {
            list.add(new Entry(i, (float) (double)doubles.get(i)));
        }

        return list;
    }

    public Model getModel() {
        if (mModel.getHiddenSizes() == null) {
            final int[] hiddenSizes = DbHelper.byteArray2IntArray(mModel.getHiddenSizeBytes());
            final List<Double> accuracies = DbHelper.byteArray2DoubleList(mModel.getAccuracyBytes());

            mModel.setHiddenSizes(hiddenSizes == null ? new int[0] : hiddenSizes);
            mModel.addAllAccuracy(accuracies == null ? new ArrayList<Double>() : accuracies);
        }

        return mModel;
    }

    public List<Entry> getEntries() {
        if (mEntries == null) {
            mEntries = format();
        }

        return mEntries;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof TrainedModelItem)
                && Objects.equals(((TrainedModelItem) obj).getModel().getId(), this.getModel().getId());
    }
}
