package io.whz.androidneuralnetwork.component;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import io.whz.androidneuralnetwork.R;
import io.whz.androidneuralnetwork.element.DigitView;
import io.whz.androidneuralnetwork.element.FigureProvider;
import io.whz.androidneuralnetwork.element.Global;
import io.whz.androidneuralnetwork.element.Scheduler;
import io.whz.androidneuralnetwork.neural.MNISTUtil;
import io.whz.androidneuralnetwork.neural.NeuralNetwork;
import io.whz.androidneuralnetwork.pojo.constant.TrackCons;
import io.whz.androidneuralnetwork.pojo.dao.Model;
import io.whz.androidneuralnetwork.pojo.dao.ModelDao;
import io.whz.androidneuralnetwork.pojo.multiple.binder.TrainedModelViewBinder;
import io.whz.androidneuralnetwork.pojo.neural.Figure;
import io.whz.androidneuralnetwork.track.Track;
import io.whz.androidneuralnetwork.util.DbHelper;
import io.whz.androidneuralnetwork.util.Precondition;

import static io.whz.androidneuralnetwork.R.id.chart;
import static io.whz.androidneuralnetwork.R.id.predict;

public class PlayActivity extends WrapperActivity implements View.OnClickListener {
    public static final String ID = "id";

    private static final String TAG = App.TAG + "-PlayActivity";
    private static final int[] FG = TrainedModelViewBinder.FG;
    private static final int[] BG = TrainedModelViewBinder.BG;

    private Toolbar mToolbar;
    private LineChart mChart;
    private DigitView mDigitView;
    private TextView mPredictText;
    private TextView mRateText;
    private View mRefresh;
    private View mPageLoading;

    private BlockTouchListener mBlockListener;
    private NeuralNetwork mNetwork;
    private Model mModel;
    private LineDataSet mLineData;
    private View mLowerBg;
    private FigureProvider mProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        mToolbar = findViewById(R.id.tool_bar);
        mChart = findViewById(chart);
        mDigitView = findViewById(R.id.digit_view);
        mPredictText = findViewById(predict);
        mRateText = findViewById(R.id.predict_rate);
        mRefresh = findViewById(R.id.refresh);
        mPageLoading = findViewById(R.id.page_loading);
        mLowerBg = findViewById(R.id.lower_bg);

        initToolbar();
        init();
    }

    private void init() {
        mRefresh.setOnClickListener(this);

        mBlockListener = new BlockTouchListener(new BlockTouchListener.Callback() {
            @Override
            public void onBlock() {
                startPredicting();

                Track.getInstance()
                        .logEvent(TrackCons.Play.PREDICT_HAND_WRITE);
            }
        }, 500);

        mDigitView.setOnTouchListener(mBlockListener);

        mPageLoading.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        showLoading();
        initChart(mChart);

        final File[] files = Global.getInstance().getDirs().test.listFiles();
        mProvider = new FigureProvider(files[ThreadLocalRandom.current().nextInt(files.length)]);

        final long id = getIntent().getLongExtra(ID, -1);

        if (id != -1) {
            getWindow().setBackgroundDrawableResource(BG[(int) id % BG.length]);
        }

        Scheduler.Secondary.execute(new Runnable() {
            @Override
            public void run() {
                initNeural(id);
                mProvider.load();
            }
        });
    }

    private void predicted(final Pair<Integer, Double> predict) {
        mBlockListener.release();
        mPredictText.setText(String.valueOf(predict.first));
        mRateText.setText(String.format("%s%%", (int) (predict.second * 100)));
    }

    private void startPredicting() {
        try {
            final Pair<Integer, Double> predicting = mNetwork.predict(mDigitView.getDarkness());
            mDigitView.markTrash();

            predicted(predicting);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLoading() {
        mPageLoading.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        mPageLoading.setVisibility(View.GONE);
    }

    private void initChart(@NonNull LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.setViewPortOffsets(0, 0, 0, 0);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);

        final ActionBar bar = getSupportActionBar();

        if (bar != null) {
            bar.setDisplayShowTitleEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initNeural(final long id) {
        if (id == -1) {
            try {
                final List<Model> list = Global.getInstance()
                        .getSession().getModelDao()
                        .queryBuilder().orderDesc(ModelDao.Properties.CreatedTime)
                        .listLazy();

                if (!list.isEmpty()) {
                    final Model model = list.get(0);
                    refreshPage(model);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            prepareModel(id);
            return;
        }

        showNoData();
    }

    private void showNoData() {
        final Activity that = this;

        Scheduler.Main.execute(new Runnable() {
            @Override
            public void run() {
                hideLoading();

                if (that.isFinishing()) {
                    return;
                }

                new AlertDialog.Builder(that)
                        .setTitle(R.string.text_dialog_no_data_title)
                        .setMessage(R.string.text_dialog_no_data_msg)
                        .setCancelable(false)
                        .setNegativeButton(R.string.text_dialog_no_data_negative, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finishAfterTransition();
                            }
                        }).show();
            }
        });
    }

    private void prepareModel(long id) {
        if (id < 0) {
            showNoData();
            return;
        }

        Model model = null;

        try {
            model = Global.getInstance()
                    .getSession()
                    .getModelDao()
                    .loadByRowId(id);
        } catch (Exception e) {
            e.printStackTrace();
            model = null;
        }

        if (model != null) {
            refreshPage(model);
            return;
        }

        showNoData();
    }

    private void refreshPage(@NonNull Model model) {
        final int[] hiddenSizes = DbHelper.byteArray2IntArray(model.getHiddenSizeBytes());
        List<Double> accuracies = DbHelper.byteArray2DoubleList(model.getAccuracyBytes());

        model.setWeights(DbHelper.byteArray2MatrixArray(model.getWeightBytes()));
        model.setBiases(DbHelper.byteArray2MatrixArray(model.getBiasBytes()));
        model.setHiddenSizes(hiddenSizes == null ? new int[0] : hiddenSizes);
        model.addAllAccuracy(accuracies == null ? new ArrayList<Double>() : accuracies);

        final List<Entry> entries = new ArrayList<>();

        if (accuracies == null) {
            accuracies = new ArrayList<>();
        }

        for (int i = 0, len = accuracies.size(); i < len; ++i) {
            entries.add(new Entry(i, (float) (double) accuracies.get(i)));
        }

        final Pair<Model, List<Entry>> pair = new Pair<>(model, entries);

        Scheduler.Main.execute(new Runnable() {
            @Override
            public void run() {
                refreshNeural(pair.first);
                renderChart(pair.first, pair.second);
                hideLoading();
            }
        });
    }

    @MainThread
    private void refreshNeural(@NonNull Model model) {
        mModel = model;
        mNetwork = new NeuralNetwork(model.getWeights(), model.getBiases());
    }

    @MainThread
    private void renderChart(@NonNull final Model model, @NonNull List<Entry> entries) {
        resetDigit();
        renderModelInfo(model);
        refreshData(entries);
        changeStyle(model.getId(), mChart, mLineData);

        mChart.getData().notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.animateY(500, Easing.EasingOption.EaseOutCubic);
    }

    private void resetDigit() {
        mDigitView.reset();

        final String unable = getString(R.string.text_predict_init);

        mPredictText.setText(unable);
        mRateText.setText(String.format("%s%%", unable));
    }

    private void renderModelInfo(@NonNull Model model) {
        mToolbar.setTitle(String.format("Play %s Model", model.getName()));
    }

    private void refreshData(@NonNull List<Entry> entries) {
        if (mLineData == null) {
            mLineData = new LineDataSet(entries, "Accuracy");

            mLineData.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            mLineData.setAxisDependency(YAxis.AxisDependency.LEFT);
            mLineData.setLineWidth(2F);
            mLineData.setDrawCircleHole(false);
            mLineData.setDrawCircles(false);
            mLineData.setHighlightEnabled(false);
            mLineData.setDrawFilled(true);

            final LineData group = new LineData(mLineData);
            group.setDrawValues(false);

            mChart.setData(group);
            mLowerBg.setAlpha(mLineData.getFillAlpha() / 255F);
        } else {
            mLineData.setValues(entries);
        }
    }

    private void changeStyle(long id, LineChart chart, LineDataSet set) {
        final int index = (int) (id % FG.length);
        final Context context = chart.getContext();

        final int fg = ContextCompat.getColor(context, FG[index]);
        set.setColor(fg);
        set.setFillColor(fg);
        mLowerBg.setBackgroundColor(fg);

        getWindow().setBackgroundDrawableResource(BG[index]);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finishAfterTransition();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater()
                .inflate(R.menu.ac_play_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.select_model:
                showModelListDialog();
                Track.getInstance()
                        .logEvent(TrackCons.Play.CLICK_MODEL_SELECTION);
                return true;

            case R.id.jump_to_detail:
                jumpToDetail();
                Track.getInstance()
                        .logEvent(TrackCons.Play.CLICK_VIEW_DETAIL);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void jumpToDetail() {
        if (mModel == null) {
            return;
        }

        final Intent intent = new Intent(this, ModelDetailActivity.class);
        intent.putExtra(ModelDetailActivity.INTENT_TYPE, ModelDetailActivity.IS_TRAINED);
        intent.putExtra(ModelDetailActivity.TRAINED_ID, mModel.getId());
        startActivity(intent);
    }

    private void showModelListDialog() {
        final List<Model> models = Global.getInstance()
                .getSession().getModelDao()
                .queryBuilder().orderDesc(ModelDao.Properties.CreatedTime).list();

        final List<Map<String, Object>> list = new ArrayList<>();
        final String nameKey = "name";
        final String evaluateKey = "evaluate";

        for (Model model : models) {
            final Map<String, Object> map = new ArrayMap<>();

            map.put(nameKey, model.getName());
            map.put(evaluateKey, String.format("%s%%", (int) (model.getEvaluate() * 100)));

            list.add(map);
        }

        final SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.dialog_model_list,
                new String[]{nameKey, evaluateKey}, new int[]{R.id.name, R.id.evaluate});

        new AlertDialog.Builder(this)
                .setTitle(R.string.text_dialog_model_list_title)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        newModelChosen(models, i);
                    }
                })
                .setNegativeButton(R.string.dialog_negative, null)
                .show();
    }

    private void newModelChosen(@NonNull List<Model> models, int i) {
        final Model newOne = models.get(i);

        if (mModel != null
                && Objects.equals(mModel.getId(), newOne.getId())) {
            return;
        }

        mModel = newOne;
        showLoading();

        Scheduler.Secondary.execute(new Runnable() {
            @Override
            public void run() {
                refreshPage(mModel);
            }
        });
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();

        switch (id) {
            case R.id.refresh:
                refreshDigit();
                Track.getInstance()
                        .logEvent(TrackCons.Play.PREDICT_MNIST);
                break;

            default:
                break;
        }
    }

    private void refreshDigit() {
        final Figure figure = mProvider.next();

        if (figure == null) {
            return;
        }

        final int[] pixels = MNISTUtil.convertByteArray2Bitmap(figure.bytes);
        mDigitView.reset(pixels);
        mDigitView.markTrash();

        startPredicting();
    }

    private static final class BlockTouchListener implements View.OnTouchListener {
        private final Handler mHandler;
        private final Runnable mRunnable;
        private final Callback mCallback;
        private final int mDelayTime;
        private boolean mBlock;
        private boolean mTry2Block;

        BlockTouchListener(@NonNull Callback callback, int delayTime) {
            Precondition.checkArgument(delayTime > 0, "Delay Time should be positive");
            mCallback = Precondition.checkNotNull(callback);
            mDelayTime = delayTime;

            mBlock = false;
            mHandler = new Handler(Looper.getMainLooper());
            mRunnable = new BlockRunnable();
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            final int action = motionEvent.getAction() & MotionEvent.ACTION_MASK;

            if (mBlock) {
                return true;
            }

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (mTry2Block) {
                        mHandler.removeCallbacks(mRunnable);
                        mTry2Block = false;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    mHandler.postDelayed(mRunnable, mDelayTime);
                    mTry2Block = true;
                    break;
            }

            return false;
        }

        void release() {
            mHandler.removeCallbacks(mRunnable);
            mBlock = false;
        }

        private final class BlockRunnable implements Runnable {
            @Override
            public void run() {
                mBlock = true;
                mCallback.onBlock();
            }
        }

        interface Callback {
            void onBlock();
        }
    }
}
