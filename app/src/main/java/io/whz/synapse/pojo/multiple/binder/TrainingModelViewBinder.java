package io.whz.synapse.pojo.multiple.binder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.whz.synapse.R;
import io.whz.synapse.element.Global;
import io.whz.synapse.pojo.event.MANEvent;
import io.whz.synapse.pojo.multiple.item.TrainingModelItem;
import io.whz.synapse.pojo.neural.Model;
import io.whz.synapse.util.StringFormatUtil;
import me.drakeet.multitype.ItemViewBinder;

public class TrainingModelViewBinder extends ItemViewBinder<TrainingModelItem, TrainingModelViewBinder.TrainingModelViewHolder>
        implements View.OnClickListener {

    @NonNull
    @Override
    protected TrainingModelViewHolder onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
        final TrainingModelViewHolder holder = TrainingModelViewHolder.newInstance(layoutInflater, viewGroup);

        holder.itemView.setOnClickListener(this);

        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull TrainingModelViewHolder trainingModelViewHolder, @NonNull TrainingModelItem trainingModelItem) {
        renderModel(trainingModelViewHolder, trainingModelItem.getModel());
    }

    private void renderModel(TrainingModelViewHolder holder, Model model) {
        final int step = model.getStepEpoch();

        holder.name.setText(model.getName());
        holder.step.setText(String.format("S: %s", step));
        holder.layers.setText(StringFormatUtil.formatLayerSizes(model.getHiddenSizes()));
        holder.epochs.setText(String.format("E: %s", model.getEpochs()));
        holder.learningRate.setText(String.format("R: %s", model.getLearningRate()));
        holder.dataSize.setText(String.format("D: %s", model.getDataSize()));

        if (step == 0 || step == model.getEpochs()) {
            holder.progress.setIndeterminate(true);
        } else {
            holder.progress.setIndeterminate(false);
            holder.progress.setProgress(step * 100 / model.getEpochs());
        }

        final double[] accuracies = model.getAccuracies();

        if (accuracies == null || accuracies.length == 0) {
            holder.accuracy.setText("--%");
        } else {
            holder.accuracy.setText(String.format("%s%%", (int)(accuracies[step - 1] * 100)));
        }
    }

    @Override
    public void onClick(View view) {
        Global.getInstance()
                .getBus()
                .post(new MANEvent<>(MANEvent.JUMP_TO_TRAINING));
    }

    static class TrainingModelViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView step;
        final TextView layers;
        final TextView epochs;
        final TextView learningRate;
        final TextView dataSize;
        final TextView accuracy;
        final ProgressBar progress;

        TrainingModelViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            step = itemView.findViewById(R.id.step);
            layers = itemView.findViewById(R.id.layers);
            epochs = itemView.findViewById(R.id.epochs);
            learningRate = itemView.findViewById(R.id.learning_rate);
            dataSize = itemView.findViewById(R.id.data_size);
            accuracy = itemView.findViewById(R.id.accuracy);
            progress = itemView.findViewById(R.id.progress);
        }

        static TrainingModelViewHolder newInstance(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
            final View view = layoutInflater.inflate(R.layout.item_training, viewGroup, false);

            return new TrainingModelViewHolder(view);
        }
    }
}
