package io.whz.synapse.pojo.event;

public abstract class TypeEvent<T> {
    public final int what;
    public final T obj;

    public TypeEvent(int what, T obj) {
        this.what = what;
        this.obj = obj;
    }

    public TypeEvent(int what) {
        this.what = what;
        this.obj = null;
    }
}
