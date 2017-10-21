package io.whz.androidneuralnetwork.track;

import android.support.annotation.NonNull;

import io.whz.androidneuralnetwork.pojo.event.TrackEvent;

interface ITracker {
    void logEvent(@NonNull TrackEvent event);
}
