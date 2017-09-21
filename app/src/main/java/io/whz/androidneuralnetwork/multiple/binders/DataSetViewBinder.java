package io.whz.androidneuralnetwork.multiple.binders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import io.whz.androidneuralnetwork.R;
import io.whz.androidneuralnetwork.events.MANEvent;
import io.whz.androidneuralnetwork.multiple.items.DataSetItem;
import me.drakeet.multitype.ItemViewBinder;

public class DataSetViewBinder extends ItemViewBinder<DataSetItem, DataSetViewBinder.DataSetHolder>
        implements View.OnClickListener {

    @NonNull
    @Override
    protected DataSetHolder onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
        final View v = layoutInflater.inflate(R.layout.ac_main_rv_item_mnist, viewGroup, false);
        final DataSetHolder holder = new DataSetHolder(v);
        holder.downloadButton.setOnClickListener(this);

        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull DataSetHolder dataSetHolder, @NonNull DataSetItem dataSet) {
        final int state = dataSet.state();

        if (state == DataSetItem.READY) {
            dataSetHolder.downloadButton.setVisibility(View.GONE);
            dataSetHolder.downloadedText.setVisibility(View.VISIBLE);
        } else if (state == DataSetItem.UNREADY){
            dataSetHolder.downloadedText.setVisibility(View.GONE);
            dataSetHolder.downloadButton.setVisibility(View.VISIBLE);
            dataSetHolder.downloadButton.setClickable(true);
        }
    }

    @Override
    public void onClick(View view) {
        EventBus.getDefault()
                .post(new MANEvent<Void>(MANEvent.CLICK_DOWNLOAD));

        view.setClickable(false);
        ((Button) view).setText(R.string.downloading);
    }

    static class DataSetHolder extends RecyclerView.ViewHolder {
        final Button downloadButton;
        final TextView downloadedText;

        DataSetHolder(View itemView) {
            super(itemView);

            downloadButton = itemView.findViewById(R.id.download_data);
            downloadedText = itemView.findViewById(R.id.downloaded);
        }
    }
}
