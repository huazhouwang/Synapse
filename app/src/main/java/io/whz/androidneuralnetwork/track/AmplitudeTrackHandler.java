package io.whz.androidneuralnetwork.track;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amplitude.api.Amplitude;
import com.amplitude.api.AmplitudeClient;

import org.json.JSONException;
import org.json.JSONObject;

import io.whz.androidneuralnetwork.BuildConfig;
import io.whz.androidneuralnetwork.pojo.event.TrackEvent;

class AmplitudeTrackHandler extends AbsTrackHandler {
    private static final String ORIGINAL_OBJECT = "AmplitudeTrackHandler:OriginalObject";

    private final AmplitudeClient mClient;

    AmplitudeTrackHandler(@NonNull Context context) {
        super(context);

        mClient = Amplitude.getInstance();
        mClient.initialize(context, BuildConfig.AMPLITUDE_ID);
    }

    @Override
    public void onTrackEvent(TrackEvent event) {
        mClient.logEvent(event.id, toJsonObject(event.bundle));
    }

    @Nullable
    private JSONObject toJsonObject(@Nullable Bundle bundle) {
        if (bundle == null) {
            return null;
        }

        final JSONObject object = new JSONObject();

        try {
            for (String key : bundle.keySet()) {
                object.put(key, bundle.get(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();

            try {
                object.put(ORIGINAL_OBJECT, bundle);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }

        return object;
    }
}
