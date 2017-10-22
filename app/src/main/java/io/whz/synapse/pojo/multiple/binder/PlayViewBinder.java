package io.whz.synapse.pojo.multiple.binder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.whz.synapse.R;
import io.whz.synapse.element.Global;
import io.whz.synapse.pojo.event.MANEvent;
import io.whz.synapse.pojo.multiple.item.PlayItem;
import me.drakeet.multitype.ItemViewBinder;

public class PlayViewBinder extends ItemViewBinder<PlayItem, PlayViewBinder.PlayViewHolder>
        implements View.OnClickListener {

    @NonNull
    @Override
    protected PlayViewHolder onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
        final PlayViewHolder holder = PlayViewHolder.newInstance(layoutInflater, viewGroup);

        holder.itemView.setOnClickListener(this);

        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull PlayViewHolder playViewHolder, @NonNull PlayItem playItem) {}

    @Override
    public void onClick(View view) {
        Global.getInstance()
                .getBus()
                .post(new MANEvent<>(MANEvent.JUMP_TO_PLAY));
    }

    static class PlayViewHolder extends RecyclerView.ViewHolder {
        PlayViewHolder(View itemView) {
            super(itemView);
        }

        static PlayViewHolder newInstance(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
            final View view = layoutInflater.inflate(R.layout.item_paly, viewGroup, false);
            return new PlayViewHolder(view);
        }
    }
}
