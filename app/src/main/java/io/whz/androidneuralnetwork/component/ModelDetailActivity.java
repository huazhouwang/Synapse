package io.whz.androidneuralnetwork.component;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
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
import io.whz.androidneuralnetwork.pojo.event.MSNEvent;
import io.whz.androidneuralnetwork.pojo.event.TrainEvent;
import io.whz.androidneuralnetwork.util.StringFormatUtil;

public class ModelDetailActivity extends AppCompatActivity {
    public static final String INTENT_TYPE = "intent_type";
    public static final String TRAINED_ID = "trained_id";
    public static final String INTERRUPT_ACTION = "interrupt_action";

    public static final int ILLEGAL = 0x00;
    public static final int IS_TRAINING = 0x01;
    public static final int IS_TRAINED = 0x01 << 1;

    private TextView mLayersText;
    private TextView mLearningRateText;
    private TextView mEpochsText;
    private TextView mDataSizeText;
    private TextView mTimeUsedText;
    private TextView mEvaluateText;
    private LineChart mChart;

    private MenuItem mInterruptItem;
    private MenuItem mDeleteItem;

    private int mIntentType;
    private boolean mIsFirst = true;
    private final List<Entry> mAccuracyData = new ArrayList<>();
    @Nullable private Model mModel;

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
        final boolean interrupt = intent.getBooleanExtra(INTERRUPT_ACTION, false);

        if (interrupt) {
            showInterruptDialog();
        }
    }

    private void showInterruptDialog() {
        final Activity that = this;

        new AlertDialog.Builder(this)
                .setTitle(R.string.text_dialog_interrupt_title)
                .setMessage(R.string.text_dialog_interrupt_msg)
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Global.getInstance()
                                .getBus()
                                .post(new MSNEvent<>(MSNEvent.TRAIN_INTERRUPT));

                        if (!that.isFinishing()) {
                            finishAfterTransition();
                        }
                    }
                }).setNegativeButton(R.string.dialog_negative, null)
                .show();
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
            case TrainEvent.INTERRUPTED:
            default:
                break;
        }
    }

    private void handleTrainCompleteEvent(@NonNull TrainEvent event) {
        final Model model = (Model) event.obj;

        if (model == null) {
            return;
        }

        mModel = model;
        setUpTrainCompleteValues(model);

        if (mInterruptItem != null) {
            mInterruptItem.setVisible(false);
            mDeleteItem.setVisible(false);
        }
    }

    private void handleTrainingEvent(@NonNull TrainEvent event) {
        final Model model = (Model) event.obj;

        if (model == null) {
            return;
        }

        mModel = model;

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
        setTitle(model.getName());
        mLayersText.setText(MNISTUtil.getLayerSizes(model.getHiddenSizes()));
        mLearningRateText.setText(String.valueOf(model.getLearningRate()));
        mEpochsText.setText(String.valueOf(model.getEpochs()));
        mDataSizeText.setText(String.valueOf(model.getDataSize()));
    }

    private void setUpTrainCompleteValues(@NonNull Model model) {
        mTimeUsedText.setText(StringFormatUtil.formatTimeUsed(model.getTimeUsed()));
        mEvaluateText.setText(StringFormatUtil.format2Percent(model.getEvaluate()));
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
        mIsFirst = false;
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

        mModel = model;

        if (mInterruptItem != null) {
            mInterruptItem.setVisible(false);
            mDeleteItem.setVisible(true);
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

        final int[] hiddenSizes = StringFormatUtil.splitString2IntArray(hiddenSizeString);
        final List<Double> accuracies = StringFormatUtil.splitString2DoubleList(accuracyString);

        model.setHiddenSizes(hiddenSizes);
        model.addAllAccuracy(accuracies);

        setUpNormalValues(model);
        setUpTrainCompleteValues(model);
        setUpChart(model);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        final int newType = intent.getIntExtra(INTENT_TYPE, ILLEGAL);
        final boolean reset;

        if (newType != mIntentType) {
            mIntentType = newType;
            reset = true;
        } else {
            reset = newType != IS_TRAINING;
        }

        if (reset) {
            mAccuracyData.clear();
            mIsFirst = true;
            resetAllText();
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater()
                .inflate(R.menu.ac_model_detail_menu, menu);

        mInterruptItem = menu.findItem(R.id.interrupt);
        mDeleteItem = menu.findItem(R.id.delete);

        if (mIntentType == IS_TRAINING) {
            mInterruptItem.setVisible(true);
            mDeleteItem.setVisible(false);
        } else if (mIntentType == IS_TRAINED) {
            mInterruptItem.setVisible(false);
            mDeleteItem.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.interrupt:
                showInterruptDialog();
                return true;

            case R.id.delete:
                deleteModel(mModel);
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteModel(@Nullable Model model) {
        if (model == null
                || model.getId() == null) {
            finishAfterTransition();
            return;
        }

        final long id = model.getId();
        final Activity that = this;

        new AlertDialog.Builder(this)
                .setTitle(R.string.text_dialog_delete_model_title)
                .setMessage(String.format(getString(R.string.text_dialog_delete_model_msg), model.getName()))
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Global.getInstance()
                                    .getSession()
                                    .getModelDao()
                                    .deleteByKey(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (!that.isFinishing()) {
                                that.finishAfterTransition();
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_negative, null)
                .show();
    }
}
