package io.whz.androidneuralnetwork.pojo.event;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TrainStateEvent<T> extends TypeEvent<T> {
    public static final int START = 0x01;
    public static final int UPDATE = 0x01 << 1;
    public static final int COMPLETE = 0x01 << 2;
    public static final int ERROR = 0x01 << 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({START, UPDATE, COMPLETE, ERROR})
    public @interface Type {
    }

    public TrainStateEvent(@Type int what, T obj) {
        super(what, obj);
    }

    public TrainStateEvent(@Type int what) {
        super(what);
    }
}
