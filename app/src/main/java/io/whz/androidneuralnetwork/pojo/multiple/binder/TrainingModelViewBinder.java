package io.whz.androidneuralnetwork.pojo.multiple.binder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.whz.androidneuralnetwork.R;
import io.whz.androidneuralnetwork.pojo.multiple.item.TrainingModelItem;
import me.drakeet.multitype.ItemViewBinder;

public class TrainingModelViewBinder extends ItemViewBinder<TrainingModelItem, TrainingModelViewBinder.TrainingModelViewHolder> {

    @NonNull
    @Override
    protected TrainingModelViewHolder onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
        final View view = layoutInflater.inflate(R.layout.trained_model_item, viewGroup, false);

        return new TrainingModelViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull TrainingModelViewHolder trainingModelViewHolder, @NonNull TrainingModelItem trainingModelItem) {
        trainingModelViewHolder.name.setText(String.valueOf(trainingModelItem.getModel().getStepEpoch()));
    }

    static class TrainingModelViewHolder extends RecyclerView.ViewHolder {
        final TextView name;

        TrainingModelViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
        }
    }
}
