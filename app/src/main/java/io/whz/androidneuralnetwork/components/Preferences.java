package io.whz.androidneuralnetwork.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import io.whz.androidneuralnetwork.utils.Preconditions;

public class Preferences {
    private static final String NAME = "global-preferences";

    private static SharedPreferences sInstance;

    public static SharedPreferences getInstance() {
        Preconditions.checkNotNull(sInstance);

        return sInstance;
    }

    @MainThread
    public static void initialize(@NonNull Context context) {
        Preconditions.checkState(sInstance == null, "Already initialize");
        Preconditions.checkNotNull(context);

        sInstance = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }
}
