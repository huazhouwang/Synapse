package io.whz.androidneuralnetwork.pojo.multiple.binder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.whz.androidneuralnetwork.R;
import io.whz.androidneuralnetwork.pojo.multiple.item.TrainedModelItem;
import me.drakeet.multitype.ItemViewBinder;

public class TrainedModelViewBinder extends ItemViewBinder<TrainedModelItem, TrainedModelViewBinder.TrainedModelViewHolder> {

    @NonNull
    @Override
    protected TrainedModelViewHolder onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
        final View view = layoutInflater.inflate(R.layout.trained_model_item, viewGroup, false);

        return new TrainedModelViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull TrainedModelViewHolder trainedModelViewHolder, @NonNull TrainedModelItem trainedModelItem) {
        trainedModelViewHolder.name.setText(trainedModelItem.getModel().getName());
    }

    static class TrainedModelViewHolder extends RecyclerView.ViewHolder {
        final TextView name;

        TrainedModelViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
        }
    }
}
