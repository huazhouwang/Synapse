package io.whz.androidneuralnetwork.element;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import io.whz.androidneuralnetwork.util.Precondition;

public class Preference {
    private static final String NAME = "global-preferences";

    private static SharedPreferences sInstance;

    public static SharedPreferences getInstance() {
        Precondition.checkNotNull(sInstance);

        return sInstance;
    }

    @MainThread
    public static void initialize(@NonNull Context context) {
        Precondition.checkState(sInstance == null, "Already initialize");
        Precondition.checkNotNull(context);

        sInstance = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }
}
