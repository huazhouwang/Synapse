package io.whz.androidneuralnetwork.pojo.constant;

import android.support.annotation.NonNull;

import io.whz.androidneuralnetwork.BuildConfig;
import io.whz.androidneuralnetwork.component.MainActivity;
import io.whz.androidneuralnetwork.component.PlayActivity;

public class TrackCons {
    private static final String SPLIT = "_";
    private static final String INDEX = BuildConfig.DEBUG ? "debug" : "";

    public interface Main {
        String INDEX = concat(TrackCons.INDEX, MainActivity.class.getSimpleName());

        String CLICK_DOWNLOAD = concat(INDEX, "click_download");
        String CLICK_FAB = concat(INDEX, "click_fab");
        String CLICK_PLAY = concat(INDEX, "click_play");
        String CLICK_TRAINED = concat(INDEX, "click_trained");
        String CLICK_TRAINING = concat(INDEX, "click_training");

        String FAIL_DOWNLOAD = concat(INDEX, "fail_download");
        String FAIL_DECOMPRESS = concat(INDEX, "fail_decompress");

        String SCROLL_BOTTOM = concat(INDEX, "scroll_bottom");
    }

    public interface Play {
        String INDEX = concat(TrackCons.INDEX, PlayActivity.class.getSimpleName());

        String CLICK_MODEL_SELECTION = concat(INDEX, "click_model_selection");
        String CLICK_VIEW_DETAIL = concat(INDEX, "click_view_detail");

        String PREDICT_MNIST = concat(INDEX, "predict_mnist");
        String PREDICT_HAND_WRITE = concat(INDEX, "predict_hand_write");
    }

    public interface Lifecycle {
        String INDEX = concat(TrackCons.INDEX, "lifecycle");

        String ENTER = concat(INDEX, "enter");
        String LEAVE = concat(INDEX, "leave");
    }

    public static String concat(@NonNull String... array) {
        final StringBuilder builder = new StringBuilder(array[0]);

        for (int i = 1, len = array.length; i < len; ++i) {
            builder.append(SPLIT)
                    .append(array[i]);
        }

        return builder.toString();
    }
}
