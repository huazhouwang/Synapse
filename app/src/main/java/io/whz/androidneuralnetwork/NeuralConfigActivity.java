package io.whz.androidneuralnetwork;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.whz.androidneuralnetwork.transitions.FabTransform;

public class NeuralConfigActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final int MAX_HIDDEN_SIZE = 5;
    private static final int MINI_BATCH_SIZE = 2000;
    private static final int MAX_PROGRESS = 100;
    private static final float STEP_NUMBER = 58000F / MINI_BATCH_SIZE;

    private final List<TextView> mHiddenSizeInputs = new ArrayList<>();

    private ViewGroup mHiddenGroup;
    private ViewGroup mContainer;
    private LayoutInflater mInflater;
    private TextView mDataSizeText;
    private View mAddNewHidden;
    private String mDataSizeTemplate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_neural_config);

        mInflater = LayoutInflater.from(this);
        mDataSizeTemplate = getString(R.string.template_data_size);

        mContainer = findViewById(R.id.container);
        mHiddenGroup = findViewById(R.id.layout_hidden_layers);
        mDataSizeText = findViewById(R.id.selected_data_size);

        final SeekBar seekBar = findViewById(R.id.seek_bar);
        prepareSeekBar(seekBar, mDataSizeText);

        mAddNewHidden = findViewById(R.id.action_add_new_layer);
        mAddNewHidden.setOnClickListener(this);

        addNewHiddenLayer();

        FabTransform.setup(this, mContainer);
    }

    private void prepareSeekBar(@NonNull SeekBar seekBar, @NonNull TextView dataSizeText) {
        seekBar.setMax(MAX_PROGRESS);
        seekBar.setProgress(MAX_PROGRESS >> 1);
        seekBar.setOnSeekBarChangeListener(this);
        dataSizeText.setText(calculateDataSize(seekBar.getProgress()));
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
        mDataSizeText.setText(calculateDataSize(i));
    }

    private String calculateDataSize(float progress) {
        final int size = (int) ((progress / MAX_PROGRESS) * STEP_NUMBER) * MINI_BATCH_SIZE;

        return String.format(mDataSizeTemplate, String.valueOf(size <= 0 ? MINI_BATCH_SIZE : size));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    public void onDismiss(View view) {
        finishAfterTransition();
    }

    public void onConfirm(View view) {
    }
}
