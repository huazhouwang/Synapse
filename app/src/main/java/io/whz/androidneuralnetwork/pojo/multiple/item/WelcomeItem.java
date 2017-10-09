package io.whz.androidneuralnetwork.pojo.multiple.item;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class WelcomeItem {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UNREADY, WAITING, READY})
    public @interface State {}

    public static final int UNREADY = 0x01;
    public static final int WAITING = 0x01 << 1;
    public static final int READY = 0x01 << 2;

    @State
    private int mState;

    public WelcomeItem() {
        mState = UNREADY;
    }

    public WelcomeItem(@State int state) {
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
        return (obj instanceof WelcomeItem)
                && ((WelcomeItem) obj).state() == this.state();
    }
}
