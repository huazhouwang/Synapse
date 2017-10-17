package io.whz.androidneuralnetwork.component;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import io.whz.androidneuralnetwork.pojo.constant.TrackCons;
import io.whz.androidneuralnetwork.pojo.dao.Model;
import io.whz.androidneuralnetwork.pojo.dao.ModelDao;
import io.whz.androidneuralnetwork.pojo.event.MANEvent;
import io.whz.androidneuralnetwork.pojo.event.ModelDeletedEvent;
import io.whz.androidneuralnetwork.pojo.event.TrainEvent;
import io.whz.androidneuralnetwork.pojo.multiple.binder.PlayViewBinder;
import io.whz.androidneuralnetwork.pojo.multiple.binder.TrainedModelViewBinder;
import io.whz.androidneuralnetwork.pojo.multiple.binder.TrainingModelViewBinder;
import io.whz.androidneuralnetwork.pojo.multiple.binder.WelcomeViewBinder;
import io.whz.androidneuralnetwork.pojo.multiple.item.PlayItem;
import io.whz.androidneuralnetwork.pojo.multiple.item.TrainedModelItem;
import io.whz.androidneuralnetwork.pojo.multiple.item.TrainingModelItem;
import io.whz.androidneuralnetwork.pojo.multiple.item.WelcomeItem;
import io.whz.androidneuralnetwork.track.Track;
import io.whz.androidneuralnetwork.transition.FabTransform;
import io.whz.androidneuralnetwork.util.Precondition;
import me.drakeet.multitype.MultiTypeAdapter;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static io.whz.androidneuralnetwork.R.id.fab;
import static io.whz.androidneuralnetwork.pojo.constant.TrackCons.Main.CLICK_DOWNLOAD;
import static io.whz.androidneuralnetwork.pojo.constant.TrackCons.Main.CLICK_FAB;
import static io.whz.androidneuralnetwork.pojo.constant.TrackCons.Main.CLICK_PLAY;
import static io.whz.androidneuralnetwork.pojo.constant.TrackCons.Main.CLICK_TRAINED;
import static io.whz.androidneuralnetwork.pojo.constant.TrackCons.Main.CLICK_TRAINING;
import static io.whz.androidneuralnetwork.pojo.constant.TrackCons.Main.FAIL_DECOMPRESS;
import static io.whz.androidneuralnetwork.pojo.constant.TrackCons.Main.FAIL_DOWNLOAD;

public class MainActivity extends WrapperActivity implements View.OnClickListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 0x01;
    private static final String TAG = App.TAG + "-MainActivity";

    private final ItemsButler mButler = new ItemsButler();

    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;

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
        mRecyclerView.addOnScrollListener(new OnScrollTracker());
        mRecyclerView.addItemDecoration(new VerticalGap(getResources().getDimensionPixelOffset(R.dimen.list_item_vertical_gap)));
        mFab = findViewById(fab);
        mFab.setOnClickListener(this);

        checkPermissionOrSolveData();
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

    private void checkPermissionOrSolveData() {
        if (Global.getInstance().isDirSet()) {
            solveData();
        } else if (checkStoragePermission()) {
            final File root;

            if ((root = getExternalFilesDir(null)) != null) {
                Global.getInstance().setRootDir(root);
                solveData();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.text_dialog_external_not_found_title)
                        .setMessage(R.string.text_dialog_external_not_found_msg)
                        .setCancelable(false)
                        .setNegativeButton(R.string.text_dialog_finish_action, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        }).show();
            }
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.text_dialog_need_external_title)
                    .setMessage(R.string.text_dialog_need_external_msg)
                    .setCancelable(false)
                    .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (!MainActivity.this.isFinishing()) {
                                requestExternalStoragePermission();
                            }
                        }
                    }).setNegativeButton(R.string.text_dialog_finish_action, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            }).show();
        } else {
            requestExternalStoragePermission();
        }
    }

    private void requestExternalStoragePermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    final File dir =  getExternalFilesDir(null);

                    if (dir != null) {
                        Global.getInstance()
                                .setRootDir(dir);

                        solveData();
                        break;
                    }
                }

                checkPermissionOrSolveData();
                break;

            default:
                break;
        }
    }

    private boolean checkDataReadyRoughly() {
        return Global.getInstance()
                .getPreference()
                .getBoolean(PreferenceKey.IS_DATA_SET_READY, false);
    }

    public boolean isDataSetReady() {
        final boolean ready = checkDataReadyRoughly();

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
        mRecyclerView.addOnScrollListener(new FabVisibleHandler(mFab));
        mFab.show();

        mButler.setWelcome(null)
                .setPlayItem(new PlayItem())
                .setTrainedModes(getAllTrainedModelItems())
                .notifyDataSetChanged();
    }

    @NonNull
    @CheckResult
    private WelcomeItem getDataSetItem(@WelcomeItem.State int state) {
        return new WelcomeItem(state);
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
        mButler.setWelcome(getDataSetItem(WelcomeItem.UNREADY))
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
    public void onModelDeleted(@NonNull ModelDeletedEvent event) {
        final Model model = event.obj;

        if (model == null) {
            return;
        }

        Global.getInstance()
                .getBus()
                .removeStickyEvent(event);

        mButler.setTrainedModes(getAllTrainedModelItems())
                .notifyDataSetChanged();
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, priority = -1, threadMode = ThreadMode.MAIN)
    public void onTraining(@NonNull final TrainEvent event) {
        @TrainEvent.Type final int what = event.what;

        switch (what) {
            case TrainEvent.START:
                mRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final Model model = (Model) event.obj;
                        mButler.setTrainingModel(getTrainingModelItem(model))
                                .notifyDataSetChanged();
                    }
                }, 300);

                mRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.smoothScrollToPosition(0);
                    }
                }, 400);
                break;

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
        @MANEvent.Event final int what = event.what;

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

            case MANEvent.JUMP_TO_PLAY:
                handleJump2Play(event);
                break;

            case MANEvent.JUMP_TO_TRAINED:
                handleJump2Trained(event);
                break;

            case MANEvent.JUMP_TO_TRAINING:
                handleJump2Training(event);
                break;

            default:
                break;
        }
    }

    private void handleJump2Training(MANEvent event) {
        final Intent intent = new Intent(this, ModelDetailActivity.class);
        intent.putExtra(ModelDetailActivity.INTENT_TYPE, ModelDetailActivity.IS_TRAINING);

        startActivity(intent);

        Track.getInstance()
                .logEvent(CLICK_TRAINING);
    }

    private void handleJump2Trained(MANEvent event) {
        final Long id = (Long) event.obj;

        if (id == null) {
            return;
        }

        final Intent intent = new Intent(this, ModelDetailActivity.class);
        intent.putExtra(ModelDetailActivity.INTENT_TYPE, ModelDetailActivity.IS_TRAINED);
        intent.putExtra(ModelDetailActivity.TRAINED_ID, id);

        startActivity(intent);

        Track.getInstance()
                .logEvent(CLICK_TRAINED);
    }

    private void handleJump2Play(MANEvent event) {
        final Intent intent = new Intent(this, PlayActivity.class);

        startActivity(intent);

        Track.getInstance()
                .logEvent(CLICK_PLAY);
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
            mButler.setWelcome(getDataSetItem(WelcomeItem.UNREADY))
                    .notifyDataSetChanged();
            res = R.string.text_download_error;

            Track.getInstance()
                    .logEvent(FAIL_DOWNLOAD);
        }

        Snackbar.make(mRecyclerView, res, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void handleDecompressComplete(MANEvent event) {
        final boolean success = event.obj != null ? (Boolean) event.obj : false;
        @StringRes final int res;

        if (success) {
            mButler.setWelcome(new WelcomeItem(WelcomeItem.READY))
                    .notifyDataSetChanged();

            mRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!MainActivity.this.isFinishing()) {
                        solveData();
                    }
                }
            }, 1500);

            res = R.string.text_decompress_success;
        } else {
            mButler.setWelcome(getDataSetItem(WelcomeItem.UNREADY))
                    .notifyDataSetChanged();
            res = R.string.text_decompress_error;

            Track.getInstance()
                    .logEvent(FAIL_DECOMPRESS);
        }

        Snackbar.make(mRecyclerView, res, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void requestDownload() {
        mButler.setWelcome(getDataSetItem(WelcomeItem.WAITING))
                .notifyDataSetChanged();

        final Intent intent = new Intent(this, MainService.class);
        intent.putExtra(MainService.ACTION_KEY, MainService.ACTION_DOWNLOAD);

        startService(intent);

        Track.getInstance()
                .logEvent(CLICK_DOWNLOAD);
    }

    private void showDownloadRequestDialog(@NonNull final Activity that) {
        final ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo network = manager.getActiveNetworkInfo();

        if (network == null
                || !network.isConnected()) {
            new AlertDialog.Builder(that)
                    .setTitle(R.string.text_dialog_network_unavailable)
                    .setMessage(R.string.text_dialog_need_wifi_msg)
                    .setPositiveButton(R.string.text_dialog_download_setting_network, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            toSetting();
                        }
                    }).show();
        } else if (network.getType() == ConnectivityManager.TYPE_WIFI) {
            requestDownload();
        } else {
            new AlertDialog.Builder(that)
                    .setTitle(R.string.text_dialog_need_wifi_title)
                    .setMessage(R.string.text_dialog_need_wifi_msg)
                    .setPositiveButton(R.string.text_dialog_download_direct, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (that.isFinishing()) {
                                return;
                            }

                            requestDownload();
                        }
                    }).setNegativeButton(R.string.text_dialog_download_setting_network, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    toSetting();
                }
            }).show();
        }
    }

    private void toSetting() {
        try {
            final Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isWifi() {
        final ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo network = manager.getActiveNetworkInfo();

        return network != null
                && network.getType() == ConnectivityManager.TYPE_WIFI
                && network.isAvailable();
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();

        switch (id) {
            case fab:
                startNeuralNetworkConfig(view);
                Track.getInstance()
                        .logEvent(CLICK_FAB);
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

    private static final class ItemsButler {
        private final RecyclerView.Adapter mAdapter;
        private final List<Object> mOldItems;
        private final List<Object> mCurItems;
        private final DiffUtil.Callback mDiffCallback;

        private WelcomeItem mWelcome;
        private PlayItem mPlayItem;
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
            adapter.register(WelcomeItem.class, new WelcomeViewBinder());
            adapter.register(TrainedModelItem.class, new TrainedModelViewBinder());
            adapter.register(TrainingModelItem.class, new TrainingModelViewBinder());
            adapter.register(PlayItem.class, new PlayViewBinder());
        }

        RecyclerView.Adapter getAdapter() {
            return mAdapter;
        }

        ItemsButler setWelcome(@Nullable WelcomeItem item) {
            mWelcome = item;
            return this;
        }

        ItemsButler setPlayItem(@Nullable PlayItem item) {
            mPlayItem = item;
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
            mWelcome = null;
            mTrainedModes = null;
            return this;
        }

        private void solveItemChange() {
            mOldItems.clear();
            mOldItems.addAll(mCurItems);
            mCurItems.clear();

            if (mWelcome != null) {
                mCurItems.add(mWelcome);
            }

            if (mTrainingModel != null) {
                mCurItems.add(mTrainingModel);
            }

            if (mPlayItem != null) {
                mCurItems.add(mPlayItem);
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

    private static final class FabVisibleHandler extends RecyclerView.OnScrollListener {
        private static final int RESPOND_RANGE = 10;

        private final FloatingActionButton mFab;

        FabVisibleHandler(@NonNull FloatingActionButton fab) {
            mFab = Precondition.checkNotNull(fab);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (Math.abs(dy) > RESPOND_RANGE) {
                if (dy < 0
                        && !mFab.isShown()) {
                    mFab.show();
                } else if (dy > 0
                        && mFab.isShown()){
                    mFab.hide();
                }
            }
        }
    }

    private static final class OnScrollTracker extends RecyclerView.OnScrollListener {
        private final Track mTrack = Track.getInstance();

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState != RecyclerView.SCROLL_STATE_IDLE
                    || !(recyclerView.getLayoutManager() instanceof LinearLayoutManager)) {
                return;
            }

            final LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();

            final int totalItemCount = recyclerView.getAdapter().getItemCount();
            final int lastVisibleItemPosition = manager.findLastVisibleItemPosition();
            final int visibleItemCount = recyclerView.getChildCount();

            if (visibleItemCount > 0
                    && lastVisibleItemPosition == totalItemCount - 1) {
                mTrack.logEvent(TrackCons.Main.SCROLL_BOTTOM);
            }
        }
    }
}
