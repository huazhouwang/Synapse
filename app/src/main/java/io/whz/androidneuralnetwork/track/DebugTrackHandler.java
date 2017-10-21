package io.whz.androidneuralnetwork.track;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import io.whz.androidneuralnetwork.pojo.event.TrackEvent;

class DebugTrackHandler extends AbsTrackHandler {
    private static final String TAG = "DebugTrackHandler";

    DebugTrackHandler(@NonNull Context context) {
        super(context);
    }

    @Override
    public void onTrackEvent(TrackEvent event) {
        Log.i(TAG, "ID: " + event.id +
                (event.bundle == null ? "" : "Event: " + event.bundle));
    }
}
