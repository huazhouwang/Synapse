package io.whz.androidneuralnetwork.pojo.event;

abstract class NormalEvent<T> extends TypeEvent<T>{
    private static final int WHAT = 0xFF;

    NormalEvent(T obj) {
        super(WHAT, obj);
    }

    private NormalEvent() {
        super(WHAT);
    }
}
