package io.whz.androidneuralnetwork;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class NeuralConfigDialog extends DialogFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final int MINI_BATCH_SIZE = 2000;
    private static final int MAX_PROGRESS = 100;
    private static final float STEP_NUMBER = 58000F / MINI_BATCH_SIZE;

    private ViewGroup mLayerGroup;
    private LayoutInflater mInflater;
    private TextView mSelectedDataSize;
    private String mSelectedDataSizeTemplate;

    public static DialogFragment newInstance(@NonNull Bundle bundle) {
        final DialogFragment fragment = new NeuralConfigDialog();

        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();

        mSelectedDataSizeTemplate = context.getString(R.string.text_selected_data_size);
        mInflater = LayoutInflater.from(context);

        final View view = mInflater.inflate(R.layout.dialog_neural_config, null);
        final View addNewLayer = view.findViewById(R.id.action_add_new_layer);
        final SeekBar seekBar = view.findViewById(R.id.seek_bar);

        mLayerGroup = view.findViewById(R.id.layout_hidden_layers);
        mSelectedDataSize = view.findViewById(R.id.selected_data_size);

        seekBar.setMax(MAX_PROGRESS);
        seekBar.setProgress(MAX_PROGRESS >> 1);
        mSelectedDataSize.setText(calculateDataSize(seekBar.getProgress()));

        seekBar.setOnSeekBarChangeListener(this);
        addNewLayer.setOnClickListener(this);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setView(view)
                .setTitle(R.string.text_neural_config_title)
                .setPositiveButton(R.string.text_neural_config_bt_positive, null)
                .setNegativeButton(R.string.text_neural_config_bt_negative, null);

        return builder.create();
    }

    @Override
    public void onClick(View view) {
        final View item = mInflater.inflate(R.layout.hidden_layer_input, mLayerGroup, false);

        mLayerGroup.addView(item);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        mSelectedDataSize.setText(calculateDataSize(i));
    }

    private String calculateDataSize(float progress) {
        final int size = (int) ((progress / MAX_PROGRESS) * STEP_NUMBER) * MINI_BATCH_SIZE;

        return String.format(mSelectedDataSizeTemplate, String.valueOf(size <= 0 ? MINI_BATCH_SIZE : size));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}
}
