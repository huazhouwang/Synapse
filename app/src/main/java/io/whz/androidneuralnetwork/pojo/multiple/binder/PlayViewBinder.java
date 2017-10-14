package io.whz.androidneuralnetwork.pojo.multiple.binder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.whz.androidneuralnetwork.R;
import io.whz.androidneuralnetwork.element.Global;
import io.whz.androidneuralnetwork.pojo.event.MANEvent;
import io.whz.androidneuralnetwork.pojo.multiple.item.PlayItem;
import me.drakeet.multitype.ItemViewBinder;

public class PlayViewBinder extends ItemViewBinder<PlayItem, PlayViewBinder.PlayViewHolder> {

    @NonNull
    @Override
    protected PlayViewHolder onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
        final View view = layoutInflater.inflate(R.layout.item_paly, viewGroup, false);

        return new PlayViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull PlayViewHolder playViewHolder, @NonNull PlayItem playItem) {
    }

    static class PlayViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View play;

        PlayViewHolder(View itemView) {
            super(itemView);

            play = itemView.findViewById(R.id.play);
            play.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Global.getInstance()
                    .getBus()
                    .post(new MANEvent<>(MANEvent.CLICK_PLAY));
        }
    }
}
