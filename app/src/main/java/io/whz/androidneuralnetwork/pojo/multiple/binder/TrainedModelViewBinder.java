package io.whz.androidneuralnetwork.pojo.multiple.binder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

import io.whz.androidneuralnetwork.R;
import io.whz.androidneuralnetwork.element.Global;
import io.whz.androidneuralnetwork.pojo.dao.Model;
import io.whz.androidneuralnetwork.pojo.event.MANEvent;
import io.whz.androidneuralnetwork.pojo.multiple.item.TrainedModelItem;
import io.whz.androidneuralnetwork.util.StringFormatUtil;
import me.drakeet.multitype.ItemViewBinder;

public class TrainedModelViewBinder extends ItemViewBinder<TrainedModelItem, TrainedModelViewBinder.TrainedModelViewHolder>
        implements View.OnClickListener {
    private static final int[] FG = new int[]{
            R.color.item_chart_fg$1,
            R.color.item_chart_fg$2,
            R.color.item_chart_fg$3,
            R.color.item_chart_fg$4,
            R.color.item_chart_fg$5,
            R.color.item_chart_fg$6,
            R.color.item_chart_fg$7,
            R.color.item_chart_fg$8,
            R.color.item_chart_fg$9,
            R.color.item_chart_fg$10
    };

    private static final int[] BG = new int[]{
            R.color.item_chart_bg$1,
            R.color.item_chart_bg$2,
            R.color.item_chart_bg$3,
            R.color.item_chart_bg$4,
            R.color.item_chart_bg$5,
            R.color.item_chart_bg$6,
            R.color.item_chart_bg$7,
            R.color.item_chart_bg$8,
            R.color.item_chart_bg$9,
            R.color.item_chart_bg$10
    };

    @NonNull
    @Override
    protected TrainedModelViewHolder onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
        final TrainedModelViewHolder holder = TrainedModelViewHolder.newInstance(layoutInflater, viewGroup);

        holder.itemView.setOnClickListener(this);
        prepareChart(holder.chart);

        return holder;
    }

    @Override
    public void onClick(View view) {
        final Long id = (Long) view.getTag();

        if (id == null) {
            return;
        }

        Global.getInstance()
                .getBus()
                .post(new MANEvent<>(MANEvent.JUMP_TO_TRAINED, id));
    }

    private void prepareChart(@NonNull LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(true);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.setViewPortOffsets(0, 0, 0, 0);
    }

    @Override
    protected void onBindViewHolder(@NonNull TrainedModelViewHolder holder, @NonNull TrainedModelItem item) {
        if (holder.data == null) {
            holder.data = prepareInitData(holder.chart, item.getEntries());
        } else {
            holder.data.setValues(item.getEntries());
            holder.chart.getData().notifyDataChanged();
            holder.chart.notifyDataSetChanged();
        }

        holder.itemView.setTag(item.getModel().getId());
        renderModel(holder, item.getModel());
        changeStyle(item.getModel().getId(), holder.chart, holder.data);

        holder.chart.invalidate();
    }

    private void changeStyle(long id, LineChart chart, LineDataSet set) {
        final int index = (int) (id % FG.length);
        final Context context = chart.getContext();

        final int fg = ContextCompat.getColor(context, FG[index]);
        set.setColor(fg);
        set.setFillColor(fg);

        chart.setGridBackgroundColor(ContextCompat.getColor(context, BG[index]));
    }

    private LineDataSet prepareInitData(@NonNull LineChart chart, @NonNull List<Entry> entries) {
        final LineDataSet set = new LineDataSet(entries, "Accuracy");

        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(2F);
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setHighlightEnabled(false);
        set.setDrawFilled(true);

        final LineData group = new LineData(set);
        group.setDrawValues(false);

        chart.setData(group);

        return set;
    }

    private void renderModel(TrainedModelViewHolder holder, Model model) {
        holder.name.setText(model.getName());
        holder.layers.setText(StringFormatUtil.formatLayerSizes(model.getHiddenSizes()));
        holder.epochs.setText(String.format("E: %s", model.getEpochs()));
        holder.learningRate.setText(String.format("L: %s", model.getLearningRate()));
        holder.dataSize.setText(String.format("D: %s", model.getDataSize()));
        holder.timeUsed.setText(String.format("T: %s", StringFormatUtil.formatTimeUsed(model.getDataSize())));
        holder.evaluate.setText(String.format("%s%%", (int)(model.getEvaluate() * 100)));
    }

    static class TrainedModelViewHolder extends RecyclerView.ViewHolder {
        final LineChart chart;
        final TextView name;
        final TextView layers;
        final TextView epochs;
        final TextView learningRate;
        final TextView dataSize;
        final TextView timeUsed;
        final TextView evaluate;

        @Nullable LineDataSet data;

        TrainedModelViewHolder(View itemView) {
            super(itemView);

            chart = itemView.findViewById(R.id.line_chart);
            name = itemView.findViewById(R.id.name);
            layers = itemView.findViewById(R.id.layers);
            epochs = itemView.findViewById(R.id.epochs);
            learningRate = itemView.findViewById(R.id.learning_rate);
            dataSize = itemView.findViewById(R.id.data_size);
            timeUsed = itemView.findViewById(R.id.time_used);
            evaluate = itemView.findViewById(R.id.accuracy);
        }

        private static TrainedModelViewHolder newInstance(@NonNull LayoutInflater layoutInflater,
                                                          @NonNull ViewGroup viewGroup) {
            final View view = layoutInflater.inflate(R.layout.item_trained, viewGroup, false);

            return new TrainedModelViewHolder(view);
        }
    }
}
