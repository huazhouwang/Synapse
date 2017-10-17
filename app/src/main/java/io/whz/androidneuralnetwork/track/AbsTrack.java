package io.whz.androidneuralnetwork.track;

import android.content.Context;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.whz.androidneuralnetwork.pojo.event.TrackEvent;

public abstract class AbsTrack implements ITrack {
    void register(@NonNull EventBus bus) {
        bus.register(this);
    }

    AbsTrack(@NonNull Context context) {}

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public abstract void onTrackEvent(TrackEvent event);
}
