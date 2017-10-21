package io.whz.androidneuralnetwork.component;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.text.method.ReplacementTransformationMethod;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.widget.SeekBar;
import android.widget.TextView;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.whz.androidneuralnetwork.R;
import io.whz.androidneuralnetwork.element.Global;
import io.whz.androidneuralnetwork.neural.MNISTUtil;
import io.whz.androidneuralnetwork.pojo.constant.TrackCons;
import io.whz.androidneuralnetwork.pojo.dao.Model;
import io.whz.androidneuralnetwork.pojo.dao.ModelDao;
import io.whz.androidneuralnetwork.track.ExceptionHelper;
import io.whz.androidneuralnetwork.track.Tracker;
import io.whz.androidneuralnetwork.transition.FabTransform;

public class NeuralModelActivity extends WrapperActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final int MAX_HIDDEN_SIZE = 5;
    private static final int MAX_PROGRESS = 100;
    private static final float STEP_NUMBER = MNISTUtil.MAX_TRAINING_SIZE / MNISTUtil.PRE_FILE_SIZE;

    private final List<TextView> mHiddenSizeInputs = new ArrayList<>();

    private View mPage;
    private ViewGroup mHiddenGroup;
    private ViewGroup mContainer;
    private LayoutInflater mInflater;
    private TextView mDataSizeText;
    private View mAddNewHidden;
    private TextView mLearningRateInput;
    private TextView mEpochsInput;
    private SeekBar mTrainingSize;
    private TextView mNameInput;

    private String mDataSizeTemplate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neural_model);

        mInflater = LayoutInflater.from(this);
        mDataSizeTemplate = getString(R.string.template_data_size);

        mPage = findViewById(R.id.page);
        mContainer = findViewById(R.id.container);
        mNameInput = findViewById(R.id.input_name);
        mHiddenGroup = findViewById(R.id.layout_hidden_layers);
        mDataSizeText = findViewById(R.id.selected_data_size);
        mLearningRateInput = findViewById(R.id.input_learning_rate);
        mEpochsInput = findViewById(R.id.input_epochs);
        mTrainingSize = findViewById(R.id.seek_bar);
        mAddNewHidden = findViewById(R.id.action_add_new_layer);

        mNameInput.setTransformationMethod(new InputLowerToUpper());
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

        Tracker.getInstance()
                .logEvent(TrackCons.Model.CLICK_LAYER_DELETE);
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

        Tracker.getInstance()
                .logEvent(TrackCons.Model.CLICK_ADD_NEW_LAYER);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        mDataSizeText.setText(formatDataSize(i));
    }

    private int calculateDataSize(float progress) {
        final int size = (int) ((progress / MAX_PROGRESS) * STEP_NUMBER) * MNISTUtil.PRE_FILE_SIZE;

        return size <= 0 ? MNISTUtil.PRE_FILE_SIZE << 1 : size;
    }

    private String formatDataSize(float progress) {
        final int size = calculateDataSize(progress);

        return String.format(mDataSizeTemplate, size, MNISTUtil.MAX_TRAINING_SIZE);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    public void onDismiss(View view) {
        finishAfterTransition();
    }

    public void onConfirm(View view) {
        final Model model = checkInputs();

        if (model == null) {
            return;
        }

        final Intent intent = new Intent(this, MainService.class);
        intent.putExtra(MainService.ACTION_KEY, MainService.ACTION_TRAIN);
        intent.putExtra(MainService.EXTRAS_NEURAL_CONFIG, model);
        startService(intent);

        final int height = mContainer.getHeight();
        final Activity that = this;

        mPage.setClickable(false);

        mContainer.animate()
                .y(-height)
                .alpha(0F)
                .setInterpolator(new AnticipateInterpolator())
                .setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (!that.isFinishing()) {
                            that.finish();
                        }
                    }
                }).start();

        Tracker.getInstance()
                .event(TrackCons.Model.CLICK_TRAIN)
                .put(TrackCons.Key.MSG, snapshot(model))
                .log();
    }

    private String snapshot(@NotNull Model model) {
        final String split = "; ";
        final StringBuilder builder = new StringBuilder();

        builder.append("HiddenSizes: ")
                .append(Arrays.toString(model.getHiddenSizes()))
                .append(split)
                .append("LearningRate: ")
                .append(model.getLearningRate())
                .append(split)
                .append("Epochs: ")
                .append(model.getEpochs())
                .append(split)
                .append("Data Sizes: ")
                .append(model.getDataSize());

        return builder.toString();
    }

    private Model checkInputs() {
        String name = String.valueOf(mNameInput.getText());

        if (TextUtils.isEmpty(name.trim())) {
            showSnackBar("Empty name is illegal");
            return null;
        } else {
            boolean hasAlready = false;
            name = name.toUpperCase();

            try {
                hasAlready = Global.getInstance()
                        .getSession()
                        .getModelDao()
                        .queryBuilder()
                        .where(ModelDao.Properties.Name.eq(name))
                        .count() > 0;
            } catch (Exception e) {
                ExceptionHelper.getInstance()
                        .caught(e);
            }

            if (hasAlready) {
                showSnackBar(String.format(getString(R.string.text_error_repeat_name), name));
                return null;
            }
        }

        final int[] hiddenSizes = new int[mHiddenSizeInputs.size()];

        for (int i = 0, len = hiddenSizes.length; i < len; ++i) {
            final String size = String.valueOf(mHiddenSizeInputs.get(i).getText());

            if (TextUtils.isEmpty(size)) {
                showSnackBar(getString(R.string.text_error_empty_hidden));
                return null;
            } else if ((hiddenSizes[i] = solveInt(size)) <= 0) {
                showSnackBar(getString(R.string.text_error_zero_hidden_size));

                return null;
            }
        }

        final double leaningRate = solveDouble(String.valueOf(mLearningRateInput.getText()));

        if (leaningRate <= 0) {
            showSnackBar(getString(R.string.text_error_zero_learning_rate));
            return null;
        }

        final int epochs = solveInt(String.valueOf(mEpochsInput.getText()));

        if (epochs <= 0) {
            showSnackBar(getString(R.string.text_error_zero_epochs));
            return null;
        }

        final int dataSize = calculateDataSize(mTrainingSize.getProgress());

        final Model model = new Model();

        model.setName(name);
        model.setHiddenSizes(hiddenSizes);
        model.setLearningRate(leaningRate);
        model.setEpochs(epochs);
        model.setDataSize(dataSize);

        return model;
    }

    private int solveInt(@NonNull String input) {
        int res;

        try {
            res =  Integer.valueOf(input);
        } catch (NumberFormatException e) {
            res = 0;
            ExceptionHelper.getInstance()
                    .caught(e);
        }

        return res;
    }

    private double solveDouble(@NonNull String input) {
        double res;

        try {
            res = Double.valueOf(input);
        } catch (NumberFormatException e) {
            res = 0D;
            ExceptionHelper.getInstance()
                    .caught(e);
        }

        return res;
    }

    private void showSnackBar(@NonNull String text) {
        Snackbar.make(mContainer, text, Snackbar.LENGTH_SHORT)
                .show();
    }

    private static class InputLowerToUpper extends ReplacementTransformationMethod {
        @Override
        protected char[] getOriginal() {
            return new char[]{
                    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j','k','l',
                    'm','n','o','p','q','r','s','t','u','v','w','x','y','z'
            };
        }

        @Override
        protected char[] getReplacement() {
            return new char[]{
                    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J','K','L',
                    'M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'
            };
        }
    }
}
