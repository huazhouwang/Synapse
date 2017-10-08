package io.whz.androidneuralnetwork.component;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.whz.androidneuralnetwork.R;
import io.whz.androidneuralnetwork.element.Global;
import io.whz.androidneuralnetwork.element.VerticalGap;
import io.whz.androidneuralnetwork.neural.MNISTUtil;
import io.whz.androidneuralnetwork.pojo.constant.PreferenceKey;
import io.whz.androidneuralnetwork.pojo.dao.Model;
import io.whz.androidneuralnetwork.pojo.dao.ModelDao;
import io.whz.androidneuralnetwork.pojo.event.MANEvent;
import io.whz.androidneuralnetwork.pojo.event.TrainEvent;
import io.whz.androidneuralnetwork.pojo.multiple.binder.DataSetViewBinder;
import io.whz.androidneuralnetwork.pojo.multiple.binder.TrainedModelViewBinder;
import io.whz.androidneuralnetwork.pojo.multiple.binder.TrainingModelViewBinder;
import io.whz.androidneuralnetwork.pojo.multiple.item.DataSetItem;
import io.whz.androidneuralnetwork.pojo.multiple.item.TrainedModelItem;
import io.whz.androidneuralnetwork.pojo.multiple.item.TrainingModelItem;
import io.whz.androidneuralnetwork.transition.FabTransform;
import me.drakeet.multitype.MultiTypeAdapter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 0x01;
    private static final String TAG = App.TAG + "-MainActivity";

    private final ItemsButler mButler = new ItemsButler();

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkDownloadManager()) {
            return;
        }

        mRecyclerView = findViewById(R.id.rv);
        mRecyclerView.setAdapter(mButler.getAdapter());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new VerticalGap(getResources().getDimensionPixelOffset(R.dimen.list_item_vertical_gap)));

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

        requireExternalStorage();
    }

    private boolean checkDownloadManager() {
        if (getSystemService(DOWNLOAD_SERVICE) == null) {
            showNeedDownloadManagerDialog();
            return false;
        }
        
        return true;
    }

    private void showNeedDownloadManagerDialog() {
        final Activity that = this;
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.text_dialog_lack_download_manager_title)
                .setMessage(R.string.text_dialog_lack_download_manager_msg)
                .setNegativeButton(R.string.text_dialog_finish_action, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!that.isFinishing()) {
                            finish();
                        }
                    }
                }).show();
    }

    private void requireExternalStorage() {
        if (Global.getInstance().isDirSet()){
            solveData();
            return;
        }

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                boolean external = false;

                if (grantResults.length > 0) {
                    for (int i = 0, len = grantResults.length; i < len; ++i) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED
                                && Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[i])) {
                            final File dir =  getExternalFilesDir(null);

                            if (dir != null) {
                                Global.getInstance()
                                        .setRootDir(dir);

                                external = true;
                                break;
                            }
                        }
                    }
                }

                if (!external) {
                    Global.getInstance().setRootDir(getCacheDir());
                }

                solveData();
                break;

            default:
                break;
        }
    }

    public boolean isDataSetReady() {
        final boolean ready = Global.getInstance()
                .getPreference()
                .getBoolean(PreferenceKey.IS_DATA_SET_READY, false);

        if (!ready) {
            return false;
        }

        final File[] files = Global.getInstance().getDirs().train.listFiles();

        if (files == null
                || files.length != (MNISTUtil.MAX_TRAINING_SIZE / MNISTUtil.PRE_FILE_SIZE)) {
            Global.getInstance()
                    .getPreference()
                    .edit()
                    .putBoolean(PreferenceKey.IS_DATA_SET_READY, false)
                    .apply();

            return false;
        }

        return true;
    }

    private void solveData() {
        mButler.clear();

        final boolean ready = isDataSetReady();

        if (ready) {
            final LayoutAnimationController controller =
                    AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_from_bottom);

            mRecyclerView.setLayoutAnimation(controller);
            mRecyclerView.scheduleLayoutAnimation();

            dataSetReady();
        } else {
            dataSetUnready();
        }
    }

    private void dataSetReady() {
        mButler.setDataSet(getDataSetItem(DataSetItem.READY))
                .setTrainedModes(getAllTrainedModelItems())
                .notifyDataSetChanged();
    }

    @NonNull
    @CheckResult
    private DataSetItem getDataSetItem(@DataSetItem.State int state) {
        return new DataSetItem(state);
    }

    @CheckResult
    @Nullable
    private List<TrainedModelItem> getAllTrainedModelItems() {
        List<Model> models = null;

        try {
            models = Global.getInstance()
                    .getSession()
                    .getModelDao()
                    .queryBuilder()
                    .orderDesc(ModelDao.Properties.CreatedTime)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (models == null
                || models.isEmpty()) {
            return null;
        }

        final List<TrainedModelItem> items = new ArrayList<>(models.size());

        for (Model model : models) {
            items.add(new TrainedModelItem(model));
        }

        return items;
    }

    private void dataSetUnready() {
        mButler.setDataSet(getDataSetItem(DataSetItem.UNREADY))
                .notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault()
                .register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        EventBus.getDefault()
                .unregister(this);
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, priority = -1, threadMode = ThreadMode.MAIN)
    public void onTraining(@NonNull TrainEvent event) {
        @TrainEvent.Type final int what = event.what;

        switch (what) {
            case TrainEvent.START:
            case TrainEvent.UPDATE:
                final Model model = (Model) event.obj;
                mButler.setTrainingModel(getTrainingModelItem(model))
                        .notifyDataSetChanged();
                break;

            case TrainEvent.COMPLETE:
            case TrainEvent.ERROR:
            case TrainEvent.INTERRUPTED:
                mButler.setTrainingModel(getTrainingModelItem(null))
                        .setTrainedModes(getAllTrainedModelItems())
                        .notifyDataSetChanged();

                Global.getInstance()
                        .getBus()
                        .removeStickyEvent(event);
                break;

            case TrainEvent.EVALUATE:
            default:
                break;
        }
    }

    @CheckResult
    @Nullable
    private TrainingModelItem getTrainingModelItem(@Nullable Model model) {
        final TrainingModelItem item;

        if (model == null) {
            item = null;
        } else {
            item = new TrainingModelItem(model);
        }

        return item;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNormalEvent(MANEvent event) {
        final int what = event.what;

        switch (what) {
            case MANEvent.CLICK_DOWNLOAD:
                showDownloadRequestDialog(this);
                break;

            case MANEvent.DOWNLOAD_COMPLETE:
                handleDownloadComplete(event);
                break;

            case MANEvent.DECOMPRESS_COMPLETE:
                handleDecompressComplete(event);
                break;

            case MANEvent.REJECT_MSG:
                handleRejectMsg(event);
                break;

            default:
                break;
        }
    }

    private void handleRejectMsg(MANEvent event) {
        final String text = String.valueOf(event.obj);

        if (TextUtils.isEmpty(text)) {
            return;
        }

        Snackbar.make(mRecyclerView, text, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void handleDownloadComplete(MANEvent event) {
        final boolean success = event.obj != null ? (Boolean) event.obj : false;

        @StringRes int res = R.string.text_download_success;

        if (!success) {
            mButler.setDataSet(getDataSetItem(DataSetItem.UNREADY))
                    .notifyDataSetChanged();
            res = R.string.text_download_error;
        }

        Snackbar.make(mRecyclerView, res, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void handleDecompressComplete(MANEvent event) {
        final boolean success = event.obj != null ? (Boolean) event.obj : false;
        @StringRes final int res;

        if (success) {
            markDataSetReadyNow();
            solveData();
            res = R.string.text_decompress_success;
        } else {
            mButler.setDataSet(getDataSetItem(DataSetItem.UNREADY))
                    .notifyDataSetChanged();
            res = R.string.text_decompress_error;
        }

        Snackbar.make(mRecyclerView, res, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void markDataSetReadyNow() {
        Global.getInstance()
                .getPreference()
                .edit()
                .putBoolean(PreferenceKey.IS_DATA_SET_READY, true)
                .apply();
    }

    private void requestDownload() {
        final Intent intent = new Intent(this, MainService.class);
        intent.putExtra(MainService.ACTION_KEY, MainService.ACTION_DOWNLOAD);

        startService(intent);
    }

    private void showDownloadRequestDialog(@NonNull final Activity that) {
        new AlertDialog.Builder(that)
                .setTitle(R.string.text_dialog_download_title)
                .setMessage(R.string.text_dialog_download_msg)
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (that.isFinishing()) {
                            return;
                        }

                        mButler.setDataSet(getDataSetItem(DataSetItem.PENDING))
                                .notifyDataSetChanged();

                        requestDownload();
                    }
                }).setNegativeButton(R.string.dialog_negative, null)
                .show();
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();

        switch (id) {
            case R.id.fab:
                startNeuralNetworkConfig(view);
                break;

            default:
                break;
        }
    }

    /**
     * Transition animation may cause exception
     */
    private void startNeuralNetworkConfig(@NonNull final View view) {
        view.setClickable(false);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setClickable(true);
            }
        }, 1000);

        try {
            final Intent intent = new Intent(this, NeuralModelActivity.class);

            FabTransform.addExtras(intent,
                    ContextCompat.getColor(this, R.color.color_accent),
                    R.drawable.ic_add_white_24dp);

            final ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                    this, view, getString(R.string.transition_neural_config));

            startActivity(intent, options.toBundle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final class ItemsButler{
        private final RecyclerView.Adapter mAdapter;
        private final List<Object> mOldItems;
        private final List<Object> mCurItems;
        private final DiffUtil.Callback mDiffCallback;

        private DataSetItem mDataSet;
        private TrainingModelItem mTrainingModel;
        private List<TrainedModelItem> mTrainedModes;

        ItemsButler() {
            mOldItems = new ArrayList<>();
            mCurItems = new ArrayList<>();
            mDiffCallback = new DiffCallback();

            final MultiTypeAdapter adapter = new MultiTypeAdapter(mCurItems);
            registerItems(adapter);

            mAdapter = adapter;
        }

        private void registerItems(@NonNull MultiTypeAdapter adapter) {
            adapter.register(DataSetItem.class, new DataSetViewBinder());
            adapter.register(TrainedModelItem.class, new TrainedModelViewBinder());
            adapter.register(TrainingModelItem.class, new TrainingModelViewBinder());
        }

        RecyclerView.Adapter getAdapter() {
            return mAdapter;
        }

        ItemsButler setDataSet(@Nullable DataSetItem dataSet) {
            mDataSet = dataSet;
            return this;
        }

        ItemsButler setTrainingModel(@Nullable TrainingModelItem item) {
            mTrainingModel = item;
            return this;
        }

        ItemsButler setTrainedModes(@Nullable List<TrainedModelItem> items) {
            mTrainedModes = items;
            return this;
        }

        ItemsButler clear() {
            mDataSet = null;
            mTrainedModes = null;
            return this;
        }

        private void solveItemChange() {
            mOldItems.clear();
            mOldItems.addAll(mCurItems);
            mCurItems.clear();

            if (mDataSet != null) {
                mCurItems.add(mDataSet);
            }

            if (mTrainingModel != null) {
                mCurItems.add(mTrainingModel);
            }

            if (mTrainedModes != null
                    && !mTrainedModes.isEmpty()) {
                mCurItems.addAll(mTrainedModes);
            }
        }

        void notifyDataSetChanged() {
            solveItemChange();

            DiffUtil.calculateDiff(mDiffCallback, true)
                    .dispatchUpdatesTo(mAdapter);
        }

        private class DiffCallback extends DiffUtil.Callback {
            @Override
            public int getOldListSize() {
                return mOldItems.size();
            }

            @Override
            public int getNewListSize() {
                return mCurItems.size();
            }

            @Override
            public boolean areItemsTheSame(int i, int i1) {
                return mOldItems.get(i).getClass().equals(mCurItems.get(i1).getClass());
            }

            @Override
            public boolean areContentsTheSame(int i, int i1) {
                return mOldItems.get(i).equals(mCurItems.get(i1));
            }
        }
    }
}
