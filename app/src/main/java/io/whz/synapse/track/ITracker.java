package io.whz.synapse.track;

import android.support.annotation.NonNull;

import io.whz.synapse.pojo.event.TrackEvent;

interface ITracker {
    void logEvent(@NonNull TrackEvent event);
}
