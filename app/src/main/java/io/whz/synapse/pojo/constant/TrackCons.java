package io.whz.synapse.pojo.constant;

import android.support.annotation.NonNull;

import io.whz.synapse.BuildConfig;
import io.whz.synapse.component.App;
import io.whz.synapse.component.MainActivity;
import io.whz.synapse.component.MainService;
import io.whz.synapse.component.ModelDetailActivity;
import io.whz.synapse.component.NeuralModelActivity;
import io.whz.synapse.component.PlayActivity;

public class TrackCons {
    private static final String SPLIT = "_";
    private static final String INDEX = BuildConfig.DEBUG ? "debug" : "";

    public interface Main {
        String INDEX = concat(TrackCons.INDEX, MainActivity.class.getSimpleName().toLowerCase());

        String CLICK_DOWNLOAD = concat(INDEX, "click_download");
        String CLICK_FAB = concat(INDEX, "click_fab");
        String CLICK_PLAY = concat(INDEX, "click_play");
        String CLICK_TRAINED = concat(INDEX, "click_trained");
        String CLICK_TRAINING = concat(INDEX, "click_training");
        String CLICK_ABOUT = concat(INDEX, "click_about");

        String SCROLL_BOTTOM = concat(INDEX, "scroll_bottom");
    }

    public interface Play {
        String INDEX = concat(TrackCons.INDEX, PlayActivity.class.getSimpleName().toLowerCase());

        String CLICK_MODEL_SELECTION = concat(INDEX, "click_model_selection");
        String CLICK_VIEW_DETAIL = concat(INDEX, "click_view_detail");

        String PLAY_MNIST = concat(INDEX, "play_mnist");
        String PLAY_HAND_WRITE = concat(INDEX, "play_hand_write");
    }

    public interface Model {
        String INDEX = concat(TrackCons.INDEX, NeuralModelActivity.class.getSimpleName().toLowerCase());

        String CLICK_ADD_NEW_LAYER = concat(INDEX, "click_add_new_layer");
        String CLICK_LAYER_DELETE = concat(INDEX, "click_layer_delete");
        String CLICK_TRAIN = concat(INDEX, "click_train");
    }

    public interface Detail {
        String INDEX = concat(TrackCons.INDEX, ModelDetailActivity.class.getSimpleName().toLowerCase());

        String CLICK_INTERRUPT = concat(INDEX, "click_interrupt");
        String CLICK_DELETE = concat(INDEX, "click_delete");
        String CLICK_PLAY = concat(INDEX, "click_play");
    }

    public interface Service {
        String INDEX = concat(TrackCons.INDEX, MainService.class.getSimpleName().toLowerCase());

        String DOWNLOAD = concat(INDEX, "download");
        String DECOMPRESS = concat(INDEX, "decompress");
        String TRAIN = concat(INDEX, "train");
        String INTERRUPT_REQ = concat(INDEX, "interrupt_req");
        String INTERRUPT = concat(INDEX, "interrupt");
    }

    public interface APP {
        String INDEX = concat(TrackCons.INDEX, App.class.getSimpleName().toLowerCase());

        String INITIALIZE = concat(INDEX, "initialize");
        String CAUGHT = concat(INDEX, "catch_exception");
    }

    public interface Key {
        String SUCCESS = "success";
        String MSG = "msg";
        String TIME_USED = "time_used";
    }

    public interface Lifecycle {
        String INDEX = concat(TrackCons.INDEX, "lifecycle");

        String ENTER = concat(INDEX, "enter");
        String LEAVE = concat(INDEX, "leave");
    }

    public static String concat(@NonNull String... array) {
        final StringBuilder builder = new StringBuilder();

        for (int i = 0, len = array.length; i < len; ++i) {
            if (builder.length() != 0) {
                builder.append(SPLIT);
            }

            builder.append(array[i]);
        }

        return builder.toString();
    }
}
