package io.whz.androidneuralnetwork.track;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import java.util.Map;

public class TimeHelper {
    private final Map<String, Long> mMap;

    private TimeHelper() {
        mMap = new ArrayMap<>();
    }

    public void start(@NonNull String id) {
        if (TextUtils.isEmpty(id) || mMap.containsKey(id)) {
            new IllegalArgumentException("Illegal id")
                    .printStackTrace();

            return;
        }

        mMap.put(id, System.currentTimeMillis());
    }

    public long stop(@NonNull String id) {
        if (TextUtils.isEmpty(id) || !mMap.containsKey(id)) {
            new IllegalArgumentException("Illegal id")
                    .printStackTrace();

            return 0L;
        }

        final long startTIme = mMap.remove(id);

        return System.currentTimeMillis() - startTIme;
    }

    public static TimeHelper getInstance() {
        return Holder.sInstance;
    }

    private interface Holder {
        TimeHelper sInstance = new TimeHelper();
    }
}
