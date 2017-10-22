package io.whz.synapse.element;

import android.support.annotation.NonNull;

import java.util.concurrent.atomic.AtomicReference;

import io.whz.synapse.component.App;
import io.whz.synapse.util.Precondition;

public class Singleton<T> {
    private static final String TAG = App.TAG + "-Singleton";
    private final AtomicReference<T> mReference;

    public Singleton() {
        mReference = new AtomicReference<>();
    }

    public Singleton<T> setAndLock(@NonNull T object) {
        if (!mReference.compareAndSet(null, object)) {
            new UnsupportedOperationException("Already locked, can't set new instance again")
                    .printStackTrace();
        }

        return this;
    }

    public boolean isSet() {
        return mReference.get() != null;
    }

    public T get() {
        return Precondition.checkNotNull(mReference.get(), "You should bind before get instance");
    }
}
