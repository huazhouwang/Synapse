package io.whz.androidneuralnetwork.component;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.whz.androidneuralnetwork.R;
import io.whz.androidneuralnetwork.element.ChannelCreator;
import io.whz.androidneuralnetwork.element.Dir;
import io.whz.androidneuralnetwork.element.Global;
import io.whz.androidneuralnetwork.element.Scheduler;
import io.whz.androidneuralnetwork.neural.DataSet;
import io.whz.androidneuralnetwork.neural.MNISTUtil;
import io.whz.androidneuralnetwork.neural.NeuralNetwork;
import io.whz.androidneuralnetwork.neural.TrainCallback;
import io.whz.androidneuralnetwork.pojo.constant.PreferenceKey;
import io.whz.androidneuralnetwork.pojo.dao.Model;
import io.whz.androidneuralnetwork.pojo.event.MANEvent;
import io.whz.androidneuralnetwork.pojo.event.MSNEvent;
import io.whz.androidneuralnetwork.pojo.event.TrainEvent;
import io.whz.androidneuralnetwork.util.DbHelper;
import io.whz.androidneuralnetwork.util.FileUtil;
import io.whz.androidneuralnetwork.util.Precondition;
import io.whz.androidneuralnetwork.util.StringFormatUtil;

public class MainService extends Service {
//    private static final int FOREGROUND_SERVER = 0x1111;

    private static final int RQC_DECOMPRESS = 0x01;
    private static final int RQC_TRAIN_START = 0x01 << 1;
    private static final int RQC_TRAIN_INTERRUPT = 0x01 << 2;

    private static final String TAG = App.TAG + "-MainService";

    public static final String EXTRAS_NEURAL_CONFIG = "extras_neural_config";

    public static final String ACTION_KEY = "action_key";
    private static final int IDLE = 0x00;
    public static final int ACTION_DOWNLOAD = 0x01;
    public static final int ACTION_TRAIN = 0x01 << 1;
    public static final int ACTION_TEST = 0x01 << 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({IDLE, ACTION_DOWNLOAD, ACTION_TRAIN, ACTION_TEST})
    private @interface Action {}

    @Action
    private int mAction = IDLE;

    private BroadcastReceiver mDownloadReceiver;
    private AtomicBoolean mTrainInterruptSign;// TODO: 28/09/2017 interrupt
    private NotificationCompat.Builder mTrainNotifyBuilder;
    private NotificationManager mNotifyManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        @Action final int action = intent.getIntExtra(ACTION_KEY, -1);

        if (isIdle()) {
            switch (action) {
                case ACTION_DOWNLOAD:
                    downloadFiles();
                    break;

                case ACTION_TRAIN:
                    handleTraining(intent);
                    break;

                case ACTION_TEST:
                    break;

                case IDLE:
                default:
                    throw new UnsupportedOperationException();
            }

            mAction = action;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void handleTraining(@NonNull Intent intent) {
        final Model model = (Model) intent.getSerializableExtra(EXTRAS_NEURAL_CONFIG);
        Precondition.checkNotNull(model);

        interruptTraining();

        mTrainInterruptSign = new AtomicBoolean(false);
        Scheduler.Secondary.execute(new TrainRunnable(model, mTrainInterruptSign));
    }

    private void reset() {
        stopForeground(true);
        mAction = IDLE;
        stopSelf();
    }

    private boolean isIdle() {
        final String rep;

        switch (mAction) {
            case IDLE:
                return true;
                
            case ACTION_DOWNLOAD:
                rep = getString(R.string.text_wait_for_download);
                break;

            case ACTION_TEST:
                rep = getString(R.string.text_wait_for_test);
                break;

            case ACTION_TRAIN:
                rep = getString(R.string.text_wait_for_train);
                break;

            default:
                return false;
        }

        sendRejectMsg(rep);
        return false;
    }

    private void sendRejectMsg(@NonNull String msg) {
        Global.getInstance()
                .getBus()
                .post(new MANEvent<>(MANEvent.REJECT_MSG, msg));
    }

    private void deleteOldFiles() {
        final Dir dir = Global.getInstance().getDirs();
        FileUtil.clear(dir.download, dir.decompress, dir.mnist);
    }

    private void downloadFiles() {
        deleteOldFiles();

        final String[] files = Global.getInstance().getDataSet();
        final DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        if (downloadManager == null) {
            return;
        }

        unregisterDownloadReceiver();

        final Uri baseUri = Global.getInstance().getBaseDownloadUri();
        final File downloadDir = Global.getInstance().getDirs().download;
        final Set<Long> ids = new HashSet<>();

        for (String file : files) {
            final Uri uri = Uri.withAppendedPath(baseUri, file);
            final DownloadManager.Request request = new DownloadManager.Request(uri);

            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                    | DownloadManager.Request.NETWORK_WIFI);
            request.setVisibleInDownloadsUi(true);
            request.setDestinationUri(Uri.fromFile(new File(downloadDir, file)));

            ids.add(downloadManager.enqueue(request));
        }

        mDownloadReceiver = new DownloadReceiver(downloadManager, ids);
        registerReceiver(mDownloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Global.getInstance().getBus()
                .register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterDownloadReceiver();

        Global.getInstance().getBus()
                .unregister(this);
    }

    private void unregisterDownloadReceiver() {
        if (mDownloadReceiver != null) {
            unregisterReceiver(mDownloadReceiver);
            mDownloadReceiver = null;
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNormalEvent(MSNEvent event) {
        final int what = event.what;

        switch (what) {
            case MSNEvent.DOWNLOAD_COMPLETE:
                handleDownloadComplete(event);
                break;

            case MSNEvent.DECOMPRESS_COMPLETE:
                handleDecompressComplete(event);
                break;

            case MSNEvent.TRAIN_INTERRUPT:
                handleInterruptRequest(event);
                break;

            default:
                break;
        }
    }

    private void handleInterruptRequest(@NonNull MSNEvent event) {
        interruptTraining();
    }

    private void interruptTraining() {
        if (mTrainInterruptSign != null) {
            mTrainInterruptSign.set(true);
        }
    }

    private void handleDownloadComplete(@NonNull MSNEvent event) {
        final boolean success = event.obj != null ? (Boolean) event.obj : false;
        unregisterDownloadReceiver();

        Global.getInstance()
                .getBus()
                .post(new MANEvent<>(MANEvent.DOWNLOAD_COMPLETE, success));

        if (success) {
            decompress();
        } else {
            reset();
        }
    }

    private void handleDecompressComplete(@NonNull MSNEvent event) {
        final boolean success = event.obj != null ? (Boolean) event.obj : false;

        if (success) {
            final Dir dir = Global.getInstance().getDirs();
            FileUtil.clear(dir.download, dir.decompress);

            markDataSetReadyNow();
        }

        Global.getInstance()
                .getBus()
                .post(new MANEvent<>(MANEvent.DECOMPRESS_COMPLETE, success));

        reset();
    }

    private void markDataSetReadyNow() {
        Global.getInstance()
                .getPreference()
                .edit()
                .putBoolean(PreferenceKey.IS_DATA_SET_READY, true)
                .apply();
    }

    private void showDecompressNotification() {
        final Intent intent = new Intent(this, MainActivity.class);
        final PendingIntent pending = PendingIntent.getActivity(this, RQC_DECOMPRESS, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ChannelCreator.CHANNEL_ID);

        builder.setContentTitle(getString(R.string.text_notification_decompress))
                .setSmallIcon(R.drawable.notify_icon)
                .setColor(ContextCompat.getColor(this, R.color.color_accent))
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setProgress(0, 0, true)
                .setContentIntent(pending);

        startForeground(RQC_DECOMPRESS, builder.build());
    }

    private void decompress() {
        final Dir dir = Global.getInstance().getDirs();
        FileUtil.makeDirs(dir.decompress, dir.mnist);

        final AtomicInteger sign = new AtomicInteger(2);

        Scheduler.Secondary.execute(new DecompressRunnable(sign, true));
        Scheduler.Secondary.execute(new DecompressRunnable(sign, false));

        showDecompressNotification();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTraining(@NonNull TrainEvent event) {
        @TrainEvent.Type final int what = event.what;

        switch (what) {
            case TrainEvent.START:
                handleTrainStartEvent(event);
                break;

            case TrainEvent.UPDATE:
                handleTrainUpdateEvent(event);
                break;

            case TrainEvent.COMPLETE:
                handleTrainCompleteEvent(event);
                break;

            case TrainEvent.EVALUATE:
                handleStartEvaluate();
                break;

            case TrainEvent.ERROR:
                handleTrainError(event);
                break;

            case TrainEvent.INTERRUPTED:
                handleInterrupted(event);
                break;
        }
    }

    private void handleInterrupted(TrainEvent event) {
        reset();
    }

    private void handleStartEvaluate() {
        mTrainNotifyBuilder.setContentText("Evaluating")
                .setProgress(0, 0, true);
        mTrainNotifyBuilder.mActions.clear();

        mNotifyManager.notify(RQC_TRAIN_START, mTrainNotifyBuilder.build());
    }

    private void handleTrainError(TrainEvent event) {
        reset();
    }

    private void handleTrainCompleteEvent(TrainEvent event) {
        final Model model = (Model) event.obj;

        if (model == null) {
            return;
        }

        final long id = model.getId();
        final String time = StringFormatUtil.formatTimeUsed(model.getTimeUsed());
        final String accuracy = StringFormatUtil.formatPercent(model.getEvaluate());

        mTrainNotifyBuilder = null;

        final Intent intent = new Intent(this, ModelDetailActivity.class);
        intent.putExtra(ModelDetailActivity.INTENT_TYPE, ModelDetailActivity.IS_TRAINED);
        intent.putExtra(ModelDetailActivity.TRAINED_ID, model.getId());

        final PendingIntent pending = PendingIntent.getActivity(this, (int)id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ChannelCreator.CHANNEL_ID);

        builder.setContentTitle(String.format(getString(R.string.template_train_complete_title), model.getName()))
                .setContentText(String.format(getString(R.string.template_train_complete_content), time, accuracy))
                .setSmallIcon(R.drawable.notify_icon)
                .setColor(ContextCompat.getColor(this, R.color.color_accent))
                .setVibrate(new long[]{1000, 1000, 1000})
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setLights(Color.CYAN, 1000, 500)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setFullScreenIntent(pending, true);

        mNotifyManager.notify((int) id, builder.build());
        reset();
    }

    private void handleTrainStartEvent(@NonNull TrainEvent event) {
        final Model model = (Model) event.obj;

        if (model == null) {
            return;
        }

        final Intent intent = new Intent(this, ModelDetailActivity.class);
        intent.putExtra(ModelDetailActivity.INTENT_TYPE, ModelDetailActivity.IS_TRAINING);

        final PendingIntent pending = PendingIntent.getActivity(this, RQC_TRAIN_START, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final Intent interruptIntent = new Intent(this, ModelDetailActivity.class);
        interruptIntent.putExtra(ModelDetailActivity.INTENT_TYPE, ModelDetailActivity.IS_TRAINING);
        interruptIntent.putExtra(ModelDetailActivity.INTERRUPT_ACTION, true);

        final PendingIntent interruptPending = PendingIntent.getActivity(this, RQC_TRAIN_INTERRUPT, interruptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mTrainNotifyBuilder = new NotificationCompat.Builder(this, ChannelCreator.CHANNEL_ID);

        mTrainNotifyBuilder.setContentTitle(String.format(getString(R.string.template_train_start_title), model.getName()))
                .setSmallIcon(R.drawable.notify_icon)
                .setColor(ContextCompat.getColor(this, R.color.color_accent))
                .setWhen(System.currentTimeMillis())
                .setProgress(model.getEpochs(), 0, false)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(pending)
                .addAction(R.drawable.ic_block_24dp, getString(R.string.text_train_interrupt), interruptPending);

        startForeground(RQC_TRAIN_START, mTrainNotifyBuilder.build());
    }

    private void handleTrainUpdateEvent(@NonNull TrainEvent event) {
        final Model model = (Model) event.obj;

        if (model == null) {
            return;
        }

        mTrainNotifyBuilder.setContentText(String.format(
                Locale.getDefault(), getString(R.string.template_train_update_content),
                String.valueOf(model.getStepEpoch()), StringFormatUtil.formatPercent(model.getLastAccuracy())))
                .setProgress(model.getEpochs(), model.getStepEpoch(), false);

        mNotifyManager.notify(RQC_TRAIN_START, mTrainNotifyBuilder.build());
    }

    private static class DecompressRunnable implements Runnable {
        private static final int QUIT_SIGN = -1;
        private static final String DECOMPRESSED_LABEL_SUFFIX = ".label";
        private static final String DECOMPRESSED_IMAGE_SUFFIX = ".image";
        private final String mImageFileName;
        private final String mLabelFileName;
        private final boolean mIsTraining;
        private final AtomicInteger mSign;

        private DecompressRunnable(AtomicInteger sign, boolean isTraining) {
            mSign = sign;

            final String[] files = Global.getInstance().getDataSet();

            if (isTraining) {
                mImageFileName = files[0];
                mLabelFileName = files[1];
            } else {
                mImageFileName = files[2];
                mLabelFileName = files[3];
            }

            mIsTraining = isTraining;
        }

        @Override
        public void run() {
            final Dir dir = Global.getInstance().getDirs();

            final File sourceLabels = new File(dir.download, mLabelFileName);
            final File targetLabels = new File(dir.decompress, mLabelFileName.substring(0,
                    mLabelFileName.lastIndexOf('.')) + DECOMPRESSED_LABEL_SUFFIX);

            final File sourceImages = new File(dir.download, mImageFileName);
            final File targetImages = new File(dir.decompress, mImageFileName.substring(0,
                    mImageFileName.lastIndexOf('.')) + DECOMPRESSED_IMAGE_SUFFIX);

            final File savedDir = mIsTraining ? dir.train : dir.test;
            FileUtil.makeDirs(savedDir);

            List<Integer> labels;

            if (MNISTUtil.gunzip(sourceLabels, targetLabels)
                    && mSign.get() != QUIT_SIGN
                    && MNISTUtil.gunzip(sourceImages, targetImages)
                    && mSign.get() != QUIT_SIGN
                    && !(labels = MNISTUtil.parseLabels(targetLabels)).isEmpty()
                    && mSign.get() != QUIT_SIGN
                    && MNISTUtil.parseImages(targetImages, savedDir, labels)) {
                if (mSign.decrementAndGet() == 0) {
                    Global.getInstance().getBus()
                            .post(new MSNEvent<>(MSNEvent.DECOMPRESS_COMPLETE, true));
                }
            } else if (mSign.get() != QUIT_SIGN) {
                mSign.set(QUIT_SIGN);
                Global.getInstance().getBus()
                        .post(new MSNEvent<>(MSNEvent.DECOMPRESS_COMPLETE, false));
            }
        }
    }

    private static class DownloadReceiver extends BroadcastReceiver {
        private final DownloadManager mDownloadManager;
        private Set<Long> mIds;

        private DownloadReceiver(@NonNull DownloadManager downloadManager, Set<Long> ids) {
            mDownloadManager = downloadManager;
            mIds = ids;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final long downloadedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            final DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadedId);

            final Cursor cursor = mDownloadManager.query(query);

            if (cursor.moveToFirst() &&
                    DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(
                            cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                mIds.remove(downloadedId);

                if (mIds.isEmpty()) {
                    Global.getInstance().getBus()
                            .post(new MSNEvent<>(MSNEvent.DOWNLOAD_COMPLETE, true));
                }
            } else {
                for (Long id : mIds) {
                    mDownloadManager.remove(id);
                }

                Global.getInstance().getBus()
                        .post(new MSNEvent<>(MSNEvent.DOWNLOAD_COMPLETE, false));
            }
        }
    }

    private static class TrainRunnable implements Runnable, TrainCallback {
        private final Model mModel;
        private final AtomicBoolean mInterruptSign;
        private final EventBus mEventBus;
        private long mStartTime;
        private NeuralNetwork mNeural;

        private TrainRunnable(@NonNull Model model, @NonNull AtomicBoolean breakSign) {
            mModel = model;
            mInterruptSign = breakSign;
            mEventBus = Global.getInstance().getBus();
        }

        @Override
        public void run() {
            final File[] files = allotFiles(mModel.getDataSize());
            final int len;

            if ((len = files.length) == 0) {
                mEventBus.postSticky(new TrainEvent<>(TrainEvent.ERROR, R.string.text_error_lack_training_data));
                return;
            }

            final DataSet training = new DataSet(Arrays.copyOf(files, len - 1));
            final DataSet validation = new DataSet(files[len - 1]);
            final DataSet test = new DataSet(Global.getInstance().getDirs().test.listFiles());

            mNeural = new NeuralNetwork(mModel.getHiddenSizes());
            mNeural.train(mModel.getEpochs(), mModel.getLearningRate(), training, validation, test, this);
        }

        @Override
        public void onStart() {
            mEventBus.postSticky(new TrainEvent<>(TrainEvent.START, mModel));
            mStartTime = System.currentTimeMillis();
        }

        @Override
        public boolean onUpdate(final int epoch, final double accuracy) {
            if (mInterruptSign.get()) {
                return false;
            }

            mModel.addAccuracy(accuracy);
            mModel.setStepEpoch(epoch);

            mEventBus.postSticky(new TrainEvent<>(TrainEvent.UPDATE, mModel));

            return true;
        }

        @Override
        public void onTrainComplete() {
            if (mInterruptSign.get()) {
                mEventBus.postSticky(new TrainEvent<>(TrainEvent.INTERRUPTED));
            } else {
                final long timeUsed = System.currentTimeMillis() - mStartTime;
                mModel.setTimeUsed(timeUsed);

                mEventBus.postSticky(new TrainEvent<>(TrainEvent.EVALUATE));
            }
        }

        @Override
        public void onEvaluateComplete(double evaluate) {
            mModel.setEvaluate(evaluate);
            mModel.setCreatedTime(System.currentTimeMillis());
            mModel.setWeights(mNeural.getWeights());
            mModel.setBiases(mNeural.getBiases());

            try {
                final long id = saveModel(mModel);
                mModel.setId(id);

                mEventBus.postSticky(new TrainEvent<>(TrainEvent.COMPLETE, mModel));
            } catch (Exception e) {
                e.printStackTrace();

                mEventBus.postSticky(new TrainEvent<>(TrainEvent.ERROR, R.string.text_error_fail_to_save_model));
            }
        }

        private long saveModel(@NonNull Model model) {
            final Model model2Saved = new Model();

            model2Saved.setName(model.getName());
            model2Saved.setLearningRate(model.getLearningRate());
            model2Saved.setEpochs(model.getEpochs());
            model2Saved.setDataSize(model.getDataSize());
            model2Saved.setTimeUsed(model.getTimeUsed());
            model2Saved.setEvaluate(model.getEvaluate());
            model2Saved.setCreatedTime(model.getCreatedTime());

            model2Saved.setHiddenSizeBytes(DbHelper.convert2ByteArray(model.getHiddenSizes()));
            model2Saved.setAccuracyBytes(DbHelper.convert2ByteArray(model.getAccuracies()));
            model2Saved.setWeightBytes(DbHelper.convert2ByteArray(model.getWeights()));
            model2Saved.setBiasBytes(DbHelper.convert2ByteArray(model.getBiases()));

            return Global.getInstance()
                    .getSession()
                    .getModelDao()
                    .insert(model2Saved);
        }
    }

    private static File[] allotFiles(int trainingSize) {
        int fileNum = (int) Math.floor((double) trainingSize / MNISTUtil.PRE_FILE_SIZE);
        fileNum = Math.min(fileNum, MNISTUtil.MAX_TRAINING_SIZE / MNISTUtil.PRE_FILE_SIZE);

        final List<File> fileList = Arrays.asList(Global.getInstance().getDirs().train.listFiles());
        fileNum = Math.min(fileNum, fileList.size());

        Collections.shuffle(fileList);
        final File[] files = new File[fileList.size()];
        fileList.toArray(files);

        return Arrays.copyOf(files, fileNum);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Unsupported");
    }
}
