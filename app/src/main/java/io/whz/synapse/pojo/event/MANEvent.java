package io.whz.synapse.pojo.event;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Main Activity Normal Event
 */
public class MANEvent<T> extends TypeEvent<T> {
    public static final int CLICK_DOWNLOAD = 0x01;
    public static final int DOWNLOAD_COMPLETE = 0x01 << 1;
    public static final int DECOMPRESS_COMPLETE = 0x01 << 2;
    public static final int REJECT_MSG = 0x01 << 3;
    public static final int JUMP_TO_PLAY = 0x01 << 4;
    public static final int JUMP_TO_TRAINED = 0x01 << 5;
    public static final int JUMP_TO_TRAINING = 0x01 << 6;

    @IntDef({
            CLICK_DOWNLOAD, DOWNLOAD_COMPLETE, DECOMPRESS_COMPLETE,
            REJECT_MSG, JUMP_TO_PLAY, JUMP_TO_TRAINED, JUMP_TO_TRAINING
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Event {
    }

    public MANEvent(int what, T obj) {
        super(what, obj);
    }

    public MANEvent(int what) {
        super(what);
    }
}
