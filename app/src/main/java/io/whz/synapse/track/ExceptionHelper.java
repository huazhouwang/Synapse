package io.whz.synapse.track;

import org.greenrobot.greendao.annotation.NotNull;

import io.whz.synapse.BuildConfig;

import static io.whz.synapse.pojo.constant.TrackCons.APP.CAUGHT;
import static io.whz.synapse.pojo.constant.TrackCons.Key.MSG;

public class ExceptionHelper {
    private static final boolean sEnable = BuildConfig.TRACK_ENABLE;
    private final Tracker mTracker;

    private ExceptionHelper() {
        mTracker = Tracker.getInstance();
    }

    public void caught(@NotNull Throwable e) {
        if (sEnable) {
            mTracker.event(CAUGHT)
                    .put(MSG, toString(e))
                    .log();
        } else {
            e.printStackTrace();
        }
    }

    private String toString(@NotNull Throwable e) {
        final StackTraceElement[] elements = e.getStackTrace();

        if (elements == null
                || elements.length == 0) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();

        for (StackTraceElement element : elements) {
            builder.append(element.toString())
                    .append("\n");
        }

        return builder.toString();
    }

    public static ExceptionHelper getInstance() {
        return Holder.sInstance;
    }

    private interface Holder {
        ExceptionHelper sInstance = new ExceptionHelper();
    }
}
