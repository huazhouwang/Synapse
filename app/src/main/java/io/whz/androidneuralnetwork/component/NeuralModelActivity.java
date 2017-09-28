package io.whz.androidneuralnetwork.component;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.whz.androidneuralnetwork.R;
import io.whz.androidneuralnetwork.neural.MNISTUtil;
import io.whz.androidneuralnetwork.pojo.neural.NeuralModel;
import io.whz.androidneuralnetwork.transition.FabTransform;

public class NeuralModelActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final int MAX_HIDDEN_SIZE = 5;
    private static final int MINI_BATCH_SIZE = MNISTUtil.PRE_FILE_SIZE;
    private static final int MAX_PROGRESS = 100;
    private static final float STEP_NUMBER = (MNISTUtil.MAX_TRAINING_SIZE - MNISTUtil.PRE_FILE_SIZE) / MNISTUtil.PRE_FILE_SIZE;

    private final List<TextView> mHiddenSizeInputs = new ArrayList<>();

    private ViewGroup mHiddenGroup;
    private ViewGroup mContainer;
    private LayoutInflater mInflater;
    private TextView mDataSizeText;
    private View mAddNewHidden;
    private TextView mLearningRateInput;
    private TextView mEpochsInput;
    private SeekBar mTrainingSize;

    private String mDataSizeTemplate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neural_model);

        mInflater = LayoutInflater.from(this);
        mDataSizeTemplate = getString(R.string.template_data_size);

        mContainer = findViewById(R.id.container);
        mHiddenGroup = findViewById(R.id.layout_hidden_layers);
        mDataSizeText = findViewById(R.id.selected_data_size);
        mLearningRateInput = findViewById(R.id.input_learning_rate);
        mEpochsInput = findViewById(R.id.input_epochs);
        mTrainingSize = findViewById(R.id.seek_bar);
        mAddNewHidden = findViewById(R.id.action_add_new_layer);

        prepareSeekBar(mTrainingSize, mDataSizeText);
        mAddNewHidden.setOnClickListener(this);
        addNewHiddenLayer();

        FabTransform.setup(this, mContainer);
    }

    private void prepareSeekBar(@NonNull SeekBar seekBar, @NonNull TextView dataSizeText) {
        seekBar.setMax(MAX_PROGRESS);
        seekBar.setProgress(MAX_PROGRESS >> 1);
        seekBar.setOnSeekBarChangeListener(this);
        dataSizeText.setText(formatDataSize(seekBar.getProgress()));
    }

    private void addNewHiddenLayer() {
        final View layout = mInflater.inflate(R.layout.hidden_layer_input, mHiddenGroup, false);

        final TextView textView = layout.findViewById(R.id.input_hidden);
        final View delete = layout.findViewById(R.id.action_delete_layer);

        if (mHiddenSizeInputs.isEmpty()) {
            delete.setVisibility(View.GONE);
        } else {
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(this);
            delete.setTag(layout);
        }

        mHiddenSizeInputs.add(textView);
        mHiddenGroup.addView(layout);
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();

        switch (id) {
            case R.id.action_add_new_layer:
                handleAddNewLayer(view);
                break;

            case R.id.action_delete_layer:
                handleDeleteLayer(view);

            default:
                break;
        }
    }

    private void prepareAnimation() {
        TransitionManager.beginDelayedTransition(mContainer);
    }

    private void handleDeleteLayer(@NonNull View view) {
        final ViewGroup layout = (ViewGroup) view.getTag();

        if (layout == null) {
            return;
        }

        final TextView input = layout.findViewById(R.id.input_hidden);

        if (input == null) {
            return;
        }

        prepareAnimation();
        mHiddenSizeInputs.remove(input);
        mHiddenGroup.removeView(layout);

        checkHiddenSize();
    }

    private void checkHiddenSize() {
        final int size = mHiddenSizeInputs.size();

        if (size < MAX_HIDDEN_SIZE && mAddNewHidden.getVisibility() != View.VISIBLE) {
            mAddNewHidden.setVisibility(View.VISIBLE);
        } else if (size >= MAX_HIDDEN_SIZE && mAddNewHidden.getVisibility() != View.GONE) {
            mAddNewHidden.setVisibility(View.GONE);
        }
    }

    private void handleAddNewLayer(@NonNull View view) {
        if (mHiddenSizeInputs.size() < MAX_HIDDEN_SIZE) {
            prepareAnimation();
            addNewHiddenLayer();
        }

        checkHiddenSize();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        mDataSizeText.setText(formatDataSize(i));
    }

    private int calculateDataSize(float progress) {
        final int size = (int) ((progress / MAX_PROGRESS) * STEP_NUMBER) * MINI_BATCH_SIZE;

        return size <= 0 ? MINI_BATCH_SIZE : size;
    }

    private String formatDataSize(float progress) {
        final int size = calculateDataSize(progress);

        return String.format(mDataSizeTemplate, size);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    public void onDismiss(View view) {
        finishAfterTransition();
    }

    public void onConfirm(View view) {
        final NeuralModel config = checkInputs();

        if (config == null) {
            return;
        }

        final Intent intent = new Intent(this, MainService.class);
        intent.putExtra(MainService.ACTION_KEY, MainService.ACTION_TRAIN);
        intent.putExtra(MainService.EXTRAS_NEURAL_CONFIG, config);

        startService(intent);

        final Animation out = AnimationUtils.loadAnimation(this, R.anim.side_top_fade_out);

        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                NeuralModelActivity.this.finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        mContainer.startAnimation(out);
    }

    private NeuralModel checkInputs() {
        final int[] hiddenSizes = new int[mHiddenSizeInputs.size()];

        for (int i = 0, len = hiddenSizes.length; i < len; ++i) {
            final String size = String.valueOf(mHiddenSizeInputs.get(i).getText());

            if (TextUtils.isEmpty(size)) {
                showSnackbar(getString(R.string.text_error_empty_hidden));
                return null;
            } else if ((hiddenSizes[i] = solveInt(size)) <= 0) {
                showSnackbar(getString(R.string.text_error_zero_hidden_size));

                return null;
            }
        }

        final double leaningRate = solveDouble(String.valueOf(mLearningRateInput.getText()));

        if (leaningRate <= 0) {
            showSnackbar(getString(R.string.text_error_zero_learning_rate));
            return null;
        }

        final int epochs = solveInt(String.valueOf(mEpochsInput.getText()));

        if (epochs <= 0) {
            showSnackbar(getString(R.string.text_error_zero_epochs));
            return null;
        }

        final int trainingSize = calculateDataSize(mTrainingSize.getProgress());

        return new NeuralModel("testingModel", hiddenSizes, leaningRate, epochs, trainingSize);
    }

    private int solveInt(@NonNull String input) {
        int res;

        try {
            res =  Integer.valueOf(input);
        } catch (NumberFormatException e) {
            res = 0;
        }

        return res;
    }

    private double solveDouble(@NonNull String input) {
        double res;

        try {
            res = Double.valueOf(input);
        } catch (NumberFormatException e) {
            res = 0D;
        }

        return res;
    }

    private void showSnackbar(@NonNull String text) {
        Snackbar.make(mContainer, text, Snackbar.LENGTH_SHORT)
                .show();
    }
}
