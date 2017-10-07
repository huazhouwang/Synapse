package io.whz.androidneuralnetwork.pojo.multiple.item;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DataSetItem {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UNREADY, PENDING, READY})
    public @interface State {}

    public static final int UNREADY = 0x01;
    public static final int PENDING = 0x01 << 1;
    public static final int READY = 0x01 << 2;

    @State
    private int mState;

    public DataSetItem() {
        mState = UNREADY;
    }

    public DataSetItem(@State int state) {
        mState = state;
    }

    public void change(@State int state) {
        mState = state;
    }

    @State
    public int state() {
        return mState;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof DataSetItem)
                && ((DataSetItem) obj).state() == this.state();
    }
}
