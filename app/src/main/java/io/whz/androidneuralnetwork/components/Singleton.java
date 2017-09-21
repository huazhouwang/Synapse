package io.whz.androidneuralnetwork.components;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.atomic.AtomicReference;

import io.whz.androidneuralnetwork.App;

public class Singleton<T> {
    private static final String TAG = App.TAG + "-Singleton";
    private final AtomicReference<T> mReference;

    public Singleton() {
        mReference = new AtomicReference<>();
    }

    public Singleton<T> bind(@NonNull T object) {
        if (mReference.compareAndSet(null, object)) {
            Log.w(TAG, "Reference has already referred to an object");
        }

        return this;
    }

    public T get() {
        return mReference.get();
    }
}
