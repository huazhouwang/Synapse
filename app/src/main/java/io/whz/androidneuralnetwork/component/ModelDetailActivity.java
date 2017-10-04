package io.whz.androidneuralnetwork.component;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.whz.androidneuralnetwork.R;
import io.whz.androidneuralnetwork.element.Global;
import io.whz.androidneuralnetwork.neural.MNISTUtil;
import io.whz.androidneuralnetwork.pojo.dao.Model;
import io.whz.androidneuralnetwork.pojo.dao.ModelDao;
import io.whz.androidneuralnetwork.pojo.event.TrainEvent;

public class ModelDetailActivity extends AppCompatActivity {
    public static final String INTENT_TYPE = "intent_type";
    public static final String TRAINED_ID = "trained_id";

    public static final int ILLEGAL = 0;
    public static final int IS_TRAINING = 1;
    public static final int IS_TRAINED = 2;

    private TextView mLayersText;
    private TextView mLearningRateText;
    private TextView mEpochsText;
    private TextView mDataSizeText;
    private TextView mTimeUsedText;
    private TextView mEvaluateText;
    private LineChart mChart;

    private int mIntentType;
    private boolean mIsFirst;
    private final List<Entry> mAccuracyData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_detail);

        mLayersText = findViewById(R.id.item1_text);
        mLearningRateText = findViewById(R.id.item2_text);
        mEpochsText = findViewById(R.id.item3_text);
        mDataSizeText = findViewById(R.id.item4_text);
        mTimeUsedText = findViewById(R.id.item5_text);
        mEvaluateText = findViewById(R.id.item6_text);
        mChart = findViewById(R.id.line_chart);

        prepareChart();
        handleIntent();
    }

    private void prepareChart() {
        mChart.getDescription().setEnabled(false);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setHighlightPerDragEnabled(true);
        mChart.setPinchZoom(true);
        mChart.setDrawGridBackground(false);
        mChart.setBackgroundColor(Color.TRANSPARENT);
        mChart.getLegend().setEnabled(true);
        mChart.getAxisRight().setEnabled(false);

        final YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.chart_left_axis));
        leftAxis.setAxisMinimum(0F);
        leftAxis.setAxisMaximum(1F);
        leftAxis.setLabelCount(5, true);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(false);
    }

    private void handleIntent() {
        final Intent intent = getIntent();

        mIntentType = intent.getIntExtra(INTENT_TYPE, ILLEGAL);
        intentSwitch(mIntentType, intent);
    }

    private void intentSwitch(int intentType, @NonNull Intent intent) {
        switch (intentType) {
            case IS_TRAINING:
                handleTrainingIntent(intent);
                break;

            case IS_TRAINED:
                handleTrainedIntent(intent);
                break;

            default:
                showNoData();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerEventBus();
    }

    private void registerEventBus() {
        if (mIntentType == IS_TRAINING) {
            Global.getInstance()
                    .getBus()
                    .register(this);
        }
    }

    private void unregisterEvenBus() {
        if (mIntentType == IS_TRAINING) {
            Global.getInstance()
                    .getBus()
                    .unregister(this);
        }
    }

    private void handleTrainingIntent(@NonNull Intent intent) {
        mIsFirst = true;
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onTraining(@NonNull TrainEvent event) {
        @TrainEvent.Type final int what = event.what;

        switch (what) {
            case TrainEvent.START:
            case TrainEvent.UPDATE:
                handleTrainingEvent(event);
                break;

            case TrainEvent.COMPLETE:
                handleTrainingEvent(event);
                handleTrainCompleteEvent(event);
                break;

            case TrainEvent.EVALUATE:
            case TrainEvent.ERROR:
            default:
                break;
        }
    }

    private void handleTrainCompleteEvent(@NonNull TrainEvent event) {
        final Model model = (Model) event.obj;

        if (model == null) {
            return;
        }

        setUpTrainCompleteValues(model);
    }

    private void handleTrainingEvent(@NonNull TrainEvent event) {
        final Model model = (Model) event.obj;

        if (model == null) {
            return;
        }

        if (mIsFirst) {
            setUpNormalValues(model);
            mIsFirst = !setUpChart(model);
        } else {
            final List<Double> accuracies = model.getAccuracies();
            final int lastIndex = accuracies.size() - 1;

            mAccuracyData.add(new Entry(lastIndex, (float) (double) accuracies.get(lastIndex)));
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.invalidate();
        }
    }

    private void setUpNormalValues(@NonNull Model model) {
        mLayersText.setText(MNISTUtil.getLayerSizes(model.getHiddenSizes()));
        mLearningRateText.setText(String.valueOf(model.getLearningRate()));
        mEpochsText.setText(String.valueOf(model.getEpochs()));
        mDataSizeText.setText(String.valueOf(model.getDataSize()));
    }

    private void setUpTrainCompleteValues(@NonNull Model model) {
        mTimeUsedText.setText(String.valueOf(model.getTimeUsed()));
        mEvaluateText.setText(String.valueOf(model.getEvaluate()));
    }

    private boolean setUpChart(@NonNull Model model) {
        final List<Double> accuracies = model.getAccuracies();

        if (accuracies.isEmpty()) {
            return false;
        }

        mAccuracyData.clear();

        for (int i = 0, len = accuracies.size(); i < len; ++i) {
            mAccuracyData.add(new Entry(i, (float) (double) accuracies.get(i)));
        }

        final LineDataSet set = new LineDataSet(mAccuracyData, getString(R.string.text_chart_left_axis));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ContextCompat.getColor(this, R.color.chart_left_axis));
        set.setCircleColor(ContextCompat.getColor(this, R.color.chart_left_axis));
        set.setHighLightColor(ContextCompat.getColor(this, R.color.chart_highlight));
        set.setCircleColorHole(Color.WHITE);
        set.setDrawCircleHole(true);
        set.setHighlightEnabled(true);
        set.setLineWidth(2F);
        set.setCircleRadius(3F);
        set.setFillColor(Color.CYAN);

        final LineData group = new LineData(set);
        group.setDrawValues(false);

        setXAxis(model.getEpochs());

        mChart.setData(group);
        mChart.invalidate();

        return true;
    }

    private void setXAxis(int epochs) {
        final XAxis axis = mChart.getXAxis();
        axis.setEnabled(true);
        axis.setAxisMinimum(0F);
        axis.setAxisMaximum(epochs - 1);
        axis.setPosition(XAxis.XAxisPosition.BOTTOM);
        axis.setDrawAxisLine(false);
        axis.setDrawGridLines(false);
    }

    private void handleTrainedIntent(@NonNull Intent intent) {
        final long id = intent.getLongExtra(TRAINED_ID, -1);
        final Model model;

        if (id == -1L) {
            final List<Model> list = Global.getInstance()
                    .getSession()
                    .getModelDao()
                    .queryBuilder()
                    .orderDesc(ModelDao.Properties.CreatedTime)
                    .listLazy();

            if (list != null && !list.isEmpty()) {
                model = list.get(list.size() - 1);
            } else {
                showNoData();
                return;
            }
        } else {
            model = Global.getInstance()
                    .getSession()
                    .getModelDao()
                    .queryBuilder()
                    .where(ModelDao.Properties.Id.eq(id))
                    .unique();
        }

        if (model == null) {
            showNoData();
            return;
        }

        displayTrainedModel(model);
    }

    private void showNoData() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.text_dialog_no_data_title)
                .setMessage(R.string.text_dialog_no_data_msg)
                .setNegativeButton(R.string.text_dialog_no_data_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishAfterTransition();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finishAfterTransition();
                    }
                }).show();
    }

    private void displayTrainedModel(@NonNull Model model) {
        final String hiddenSizeString = model.getHiddenSizeString();
        final String accuracyString = model.getAccuracyString();

        final int[] hiddenSizes = getHiddenSizes(hiddenSizeString);
        final List<Double> accuracies = getAccuracies(accuracyString);

        model.setHiddenSizes(hiddenSizes);
        model.addAllAccuracy(accuracies);

        setUpNormalValues(model);
        setUpTrainCompleteValues(model);
        setUpChart(model);
    }

    private List<Double> getAccuracies(@NonNull String accuracyString) {
        final String[] tmp = accuracyString.split(":");
        final List<Double> res = new ArrayList<>();

        for (String item : tmp) {
            res.add(Double.valueOf(item));
        }

        return res;
    }

    private int[] getHiddenSizes(@NonNull String hiddenSizeString) {
        final String[] sizes = hiddenSizeString.split(":");
        final int[] res = new int[sizes.length];

        for (int i = 0, len = res.length; i < len; ++i) {
            res[i] = Integer.valueOf(sizes[i]);
        }

        return res;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        mIntentType = intent.getIntExtra(INTENT_TYPE, ILLEGAL);
        mAccuracyData.clear();
        mIsFirst = true;
        resetAllText();

        intentSwitch(mIntentType, intent);
    }

    private void resetAllText() {
        final String unable = getString(R.string.text_value_unable);

        mLayersText.setText(unable);
        mLearningRateText.setText(unable);
        mEpochsText.setText(unable);
        mDataSizeText.setText(unable);
        mTimeUsedText.setText(unable);
        mEvaluateText.setText(unable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterEvenBus();
    }
}
