package io.whz.androidneuralnetwork.track;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import io.whz.androidneuralnetwork.BuildConfig;
import io.whz.androidneuralnetwork.pojo.event.TrackEvent;
import io.whz.androidneuralnetwork.util.Precondition;

public class Tracker implements ITracker {
    private final Set<AbsTrackHandler> mTracks = new HashSet<>();

    private EventBus mBus;

    public void initialize(@NonNull Context context, @NonNull EventBus bus) {
        mBus = Precondition.checkNotNull(bus);

        if (BuildConfig.TRACK_ENABLE) {
            mTracks.add(new FirebaseTrackHandler(context));
            mTracks.add(new AmplitudeTrackHandler(context));
        } else {
            mTracks.add(new DebugTrackHandler(context));
        }

        for (AbsTrackHandler track : mTracks) {
            track.register(bus);
        }

        if (context instanceof Application) {
            ((Application) context).registerActivityLifecycleCallbacks(new ActivityLifecycleTracker(this));
        }
    }

    @Override
    public void logEvent(@NonNull TrackEvent event) {
        if (mBus == null) {
            new NullPointerException("EvenBus is null, please initialize first")
                    .printStackTrace();
            return;
        }

        mBus.post(event);
    }

    public void logEvent(@NonNull String id) {
        Precondition.checkNotNull(id);

        logEvent(new TrackEvent(id, null));
    }

    public EventBuilder event(@NonNull String id) {
        Precondition.checkNotNull(id);

        return new EventBuilder(id);
    }

    public static Tracker getInstance() {
        return Holder.sInstance;
    }

    private interface Holder {
        Tracker sInstance = new Tracker();
    }

    public static class EventBuilder{
        private final Bundle bundle = new Bundle();
        private final String id;

        EventBuilder(@NonNull String id) {
            this.id = id;
        }

        public EventBuilder put(@NonNull String key, boolean value) {
            bundle.putBoolean(key, value);

            return this;
        }

        public EventBuilder put(@NonNull String key, int value) {
            bundle.putInt(key, value);

            return this;
        }

        public EventBuilder put(@NonNull String key, double value) {
            bundle.putDouble(key, value);
            return this;
        }

        public EventBuilder put(@NonNull String key, String value) {
            bundle.putString(key, value);

            return this;
        }

        public EventBuilder put(@NonNull String key, Serializable value) {
            bundle.putSerializable(key, value);

            return this;
        }

        public void log() {
            Holder.sInstance.logEvent(new TrackEvent(id, bundle));
        }
    }
}
