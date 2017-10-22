package io.whz.synapse.pojo.event;

public class MSNEvent<T> extends TypeEvent<T> {
    public static final int DOWNLOAD_COMPLETE = 0x01;
    public static final int DECOMPRESS_COMPLETE = 0x01 << 1;
    public static final int TRAIN_INTERRUPT = 0x01 << 2;

    public MSNEvent(int what, T obj) {
        super(what, obj);
    }

    public MSNEvent(int what) {
        super(what);
    }
}
