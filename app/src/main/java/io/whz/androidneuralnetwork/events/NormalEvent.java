package io.whz.androidneuralnetwork.events;

public class NormalEvent<T> {
    public final int what;
    public final T obj;

    public NormalEvent(int what, T obj) {
        this.what = what;
        this.obj = obj;
    }

    public NormalEvent(int what) {
        this.what = what;
        this.obj = null;
    }
}
