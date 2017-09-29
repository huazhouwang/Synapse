package io.whz.androidneuralnetwork.pojo.event;

/**
 * Main Activity Normal Event
 */
public class MANEvent<T> extends TypeEvent<T> {
    public static final int DOWNLOAD_COMPLETE = 0x01 << 1;
    public static final int DECOMPRESS_COMPLETE = 0x01 << 2;
    public static final int REJECT_MSG = 0x01 << 3;

    public MANEvent(int what, T obj) {
        super(what, obj);
    }

    public MANEvent(int what) {
        super(what);
    }
}
