package io.whz.androidneuralnetwork.pojo.event;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.whz.androidneuralnetwork.util.Precondition;

public class TrackEvent {
    @NonNull
    public final String id;
    @Nullable
    public final Bundle bundle;

    public TrackEvent(@NonNull String id, @Nullable Bundle bundle) {
        Precondition.checkNotNull(id);

        this.id = id;
        this.bundle = bundle;
    }
}
