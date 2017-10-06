package io.whz.androidneuralnetwork.pojo.multiple.binder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;

import io.whz.androidneuralnetwork.R;
import io.whz.androidneuralnetwork.pojo.event.MANEvent;
import io.whz.androidneuralnetwork.pojo.multiple.item.DataSetItem;
import me.drakeet.multitype.ItemViewBinder;

public class DataSetViewBinder extends ItemViewBinder<DataSetItem, DataSetViewBinder.DataSetHolder>
        implements View.OnClickListener {

    @NonNull
    @Override
    protected DataSetHolder onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
        final View v = layoutInflater.inflate(R.layout.data_set, viewGroup, false);
        final DataSetHolder holder = new DataSetHolder(v);
        holder.download.setOnClickListener(this);

        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull DataSetHolder holder, @NonNull DataSetItem dataSet) {
        final int state = dataSet.state();
        final View download = holder.download;

        download.clearAnimation();

        if (state == DataSetItem.READY) {
            download.setEnabled(false);
        } else if (state == DataSetItem.PENDING) {
            download.setClickable(false);
            startDownloading(download);
        } else {
            download.setEnabled(true);
            download.setClickable(true);
        }
    }

    private void startDownloading(@NonNull View view) {
        final Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.alpha_repeat);
        animation.setRepeatCount(Animation.INFINITE);
        view.startAnimation(animation);
    }

    @Override
    protected void onViewDetachedFromWindow(@NonNull DataSetHolder holder) {
        super.onViewDetachedFromWindow(holder);

        holder.download.clearAnimation();
    }

    @Override
    public void onClick(View view) {
        EventBus.getDefault()
                .post(new MANEvent<Void>(MANEvent.CLICK_DOWNLOAD));
    }

    static class DataSetHolder extends RecyclerView.ViewHolder {
        final ImageView download;

        DataSetHolder(View itemView) {
            super(itemView);

            download = itemView.findViewById(R.id.download);
        }
    }
}
