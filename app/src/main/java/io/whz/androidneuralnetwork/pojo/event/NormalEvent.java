package io.whz.androidneuralnetwork.pojo.event;

abstract class NormalEvent<T> {
    public final T obj;

    NormalEvent(T obj) {
        this.obj = obj;
    }
}
