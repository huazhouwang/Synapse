package io.whz.androidneuralnetwork.track;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import io.whz.androidneuralnetwork.pojo.event.TrackEvent;

class DebugTrack extends AbsTrack {
    private static final String TAG = "DebugTrack";

    DebugTrack(@NonNull Context context) {
        super(context);
    }

    @Override
    public void onTrackEvent(TrackEvent event) {
        Log.i(TAG, String.format("ID: %s, Event %s", event.id, event.bundle));
    }
}
