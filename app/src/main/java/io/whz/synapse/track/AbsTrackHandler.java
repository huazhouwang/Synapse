package io.whz.synapse.track;

import android.content.Context;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.whz.synapse.pojo.event.TrackEvent;

public abstract class AbsTrackHandler {
    void register(@NonNull EventBus bus) {
        bus.register(this);
    }

    AbsTrackHandler(@NonNull Context context) {}

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public abstract void onTrackEvent(TrackEvent event);
}
