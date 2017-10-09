package io.whz.androidneuralnetwork.pojo.multiple.binder;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.transition.TransitionManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import io.whz.androidneuralnetwork.R;
import io.whz.androidneuralnetwork.pojo.event.MANEvent;
import io.whz.androidneuralnetwork.pojo.multiple.item.WelcomeItem;
import me.drakeet.multitype.ItemViewBinder;

public class WelcomeViewBinder extends ItemViewBinder<WelcomeItem, WelcomeViewBinder.DataSetHolder>
        implements View.OnClickListener {

    @NonNull
    @Override
    protected DataSetHolder onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
        final View v = layoutInflater.inflate(R.layout.item_welcome, viewGroup, false);
        final DataSetHolder holder = new DataSetHolder(v);
        holder.download.setOnClickListener(this);

        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull DataSetHolder holder, @NonNull WelcomeItem dataSet) {
        final Resources resources = holder.download.getResources();
        TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);

        switch (dataSet.state()) {
            case WelcomeItem.READY:
                holder.download.setText(R.string.data_ready);
                holder.download.setTextColor(ResourcesCompat.getColor(resources, R.color.data_ready_text, null));
                holder.download.setClickable(false);
                holder.progress.setVisibility(View.GONE);
                break;

            case WelcomeItem.WAITING:
                holder.download.setTextColor(ResourcesCompat.getColor(resources, R.color.data_waiting_text, null));
                holder.download.setText(R.string.data_waiting);
                holder.download.setClickable(true);
                holder.progress.setVisibility(View.VISIBLE);
                break;

            case WelcomeItem.UNREADY:
                holder.download.setTextColor(ResourcesCompat.getColor(resources, R.color.data_unready_text, null));
                holder.download.setText(R.string.data_unready);
                holder.download.setClickable(true);
                holder.progress.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        EventBus.getDefault()
                .post(new MANEvent<Void>(MANEvent.CLICK_DOWNLOAD));
    }

    static class DataSetHolder extends RecyclerView.ViewHolder {
        final TextView download;
        final View progress;

        DataSetHolder(View itemView) {
            super(itemView);

            download = itemView.findViewById(R.id.download);
            progress = itemView.findViewById(R.id.progress);
        }
    }
}
