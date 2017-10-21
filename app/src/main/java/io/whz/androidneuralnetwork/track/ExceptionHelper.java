package io.whz.androidneuralnetwork.track;

import org.greenrobot.greendao.annotation.NotNull;

import io.whz.androidneuralnetwork.BuildConfig;

import static io.whz.androidneuralnetwork.pojo.constant.TrackCons.APP.CAUGHT;
import static io.whz.androidneuralnetwork.pojo.constant.TrackCons.Key.MSG;

public class ExceptionHelper {
    private static final boolean sIsDebug = BuildConfig.DEBUG;
    private final Tracker mTracker;

    private ExceptionHelper() {
        mTracker = Tracker.getInstance();
    }

    public void caught(@NotNull Exception e) {
        if (sIsDebug) {
            e.printStackTrace();
        } else {
            mTracker.event(CAUGHT)
                    .put(MSG, toString(e))
                    .log();
        }
    }

    private String toString(@NotNull Exception e) {
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
