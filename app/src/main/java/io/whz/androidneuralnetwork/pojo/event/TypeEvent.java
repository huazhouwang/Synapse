package io.whz.androidneuralnetwork.pojo.event;

abstract class TypeEvent<T> {
    public final int what;
    public final T obj;

    TypeEvent(int what, T obj) {
        this.what = what;
        this.obj = obj;
    }

    TypeEvent(int what) {
        this.what = what;
        this.obj = null;
    }
}
