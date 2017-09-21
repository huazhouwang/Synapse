package io.whz.androidneuralnetwork.events;

/**
 * Main Activity Normal Event
 */
public class MANEvent<T> extends NormalEvent<T>{
    public static final int CLICK_DOWNLOAD = 0x01;
    public static final int DOWNLOAD_COMPLETE = 0x01 << 1;
    public static final int DECOMPRESS_COMPLETE = 0x01 << 2;

    public MANEvent(int what, T obj) {
        super(what, obj);
    }

    public MANEvent(int what) {
        super(what);
    }
}
