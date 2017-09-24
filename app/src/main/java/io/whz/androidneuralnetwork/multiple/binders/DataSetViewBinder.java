package io.whz.androidneuralnetwork.multiple.binders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
        holder.download.setOnClickListener(this);

        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull DataSetHolder holder, @NonNull DataSetItem dataSet) {
        final int state = dataSet.state();

        if (state == DataSetItem.READY) {
            holder.download.setImageResource(R.drawable.ic_cloud_done_24dp);
            holder.download.setClickable(false);
            holder.download.setActivated(true);
        } else if (state == DataSetItem.UNREADY){
            holder.download.setImageResource(R.drawable.ic_cloud_download_24dp);
            holder.download.setClickable(true);
            holder.download.setActivated(false);
        }
    }

    @Override
    public void onClick(View view) {
        EventBus.getDefault()
                .post(new MANEvent<Void>(MANEvent.CLICK_DOWNLOAD));

        view.setClickable(false);
    }

    static class DataSetHolder extends RecyclerView.ViewHolder {
        final ImageView download;

        DataSetHolder(View itemView) {
            super(itemView);

            download = itemView.findViewById(R.id.download);

        }
    }
}
