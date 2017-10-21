package io.whz.androidneuralnetwork.track;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;

import io.whz.androidneuralnetwork.pojo.event.TrackEvent;

class FirebaseTrackHandler extends AbsTrackHandler {

    private final FirebaseAnalytics mAnalyties;

    FirebaseTrackHandler(@NonNull Context context) {
        super(context);

        mAnalyties = FirebaseAnalytics.getInstance(context);
    }

    @Override
    public void onTrackEvent(TrackEvent event) {
        mAnalyties.logEvent(event.id, event.bundle);
    }
}
