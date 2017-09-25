package io.whz.androidneuralnetwork;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.whz.androidneuralnetwork.components.Dirs;
import io.whz.androidneuralnetwork.components.Global;
import io.whz.androidneuralnetwork.components.Scheduler;
import io.whz.androidneuralnetwork.events.MANEvent;
import io.whz.androidneuralnetwork.pojos.NeuralModel;
import io.whz.androidneuralnetwork.utils.FileUtils;
import io.whz.androidneuralnetwork.utils.MNISTUtils;
import io.whz.androidneuralnetwork.utils.Preconditions;

public class MainService extends Service {
    private static final int FOREGROUND_SERVER = 0x1111;
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
        final NeuralModel model = (NeuralModel) intent.getSerializableExtra(EXTRAS_NEURAL_CONFIG);

        Preconditions.checkNotNull(model);

        // TODO: 25/09/2017
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
        final Dirs dirs = Global.getInstance().getDirs();
        FileUtils.clear(dirs.download, dirs.decompress, dirs.mnist);
    }

    private void downloadFiles() {
        deleteOldFiles();

        final String[] files = Global.getInstance().getDataSet();
        final DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        if (downloadManager == null) {
            return;
        }

        unregisterDownloadReceiver();

        final Uri baseUri = Global.getInstance().getBaseMnistUri();
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
    public void onDestroy() {
        super.onDestroy();

        unregisterDownloadReceiver();
    }

    private void unregisterDownloadReceiver() {
        if (mDownloadReceiver != null) {
            unregisterReceiver(mDownloadReceiver);
            mDownloadReceiver = null;
        }
    }

    private void onDownloadComplete(final boolean success) {
        Scheduler.Main.execute(new Runnable() {
            @Override
            public void run() {
                unregisterDownloadReceiver();

                Global.getInstance()
                        .getBus()
                        .post(new MANEvent<>(MANEvent.DOWNLOAD_COMPLETE, success));

                if (success) {
                    decompress();
                } else {
                    FileUtils.clear(Global.getInstance().getDirs().download);

                    mAction = IDLE;
                    stopSelf();
                }
            }
        });
    }

    private void onDecompressComplete(final boolean success) {
        Scheduler.Main.execute(new Runnable() {
            @Override
            public void run() {
                stopForeground(true);

                if (success) {
                    FileUtils.clear(Global.getInstance().getDirs().decompress);
                } else {
                    FileUtils.clear(Global.getInstance().getDirs().download);
                }

                Global.getInstance()
                        .getBus()
                        .post(new MANEvent<>(MANEvent.DECOMPRESS_COMPLETE, success));

                mAction = IDLE;
                stopSelf();
            }
        });
    }

    private void showDecompressNotification() {
        final Intent intent = new Intent(this, MainActivity.class);
        final PendingIntent pending = PendingIntent.getService(this, 0, intent, 0);

        final Notification.Builder builder = new Notification.Builder(this.getApplicationContext());

        builder.setContentTitle(getString(R.string.text_notification_decompress))
                .setSmallIcon(R.drawable.ic_build_24dp)
                .setTicker(getString(R.string.text_notification_decompress))
                .setWhen(System.currentTimeMillis())
                .setProgress(0, 0, true)
                .setOngoing(true)
                .setContentIntent(pending);

        startForeground(FOREGROUND_SERVER, builder.build());
    }

    private void decompress() {
        final Dirs dirs = Global.getInstance().getDirs();
        FileUtils.makeDirs(dirs.decompress, dirs.mnist);

        final AtomicInteger sign = new AtomicInteger(2);

        Scheduler.Secondary.execute(new DecompressRunnable(sign, true));
        Scheduler.Secondary.execute(new DecompressRunnable(sign, false));

        showDecompressNotification();
    }

    private class DecompressRunnable implements Runnable {
        private static final int QUIT_SIGN = Integer.MIN_VALUE;
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
            final Dirs dirs = Global.getInstance().getDirs();

            final File sourceLabels = new File(dirs.download, mLabelFileName);
            final File targetLabels = new File(dirs.decompress, mLabelFileName.substring(0,
                    mLabelFileName.lastIndexOf('.')) + DECOMPRESSED_LABEL_SUFFIX);

            final File sourceImages = new File(dirs.download, mImageFileName);
            final File targetImages = new File(dirs.decompress, mImageFileName.substring(0,
                    mImageFileName.lastIndexOf('.')) + DECOMPRESSED_IMAGE_SUFFIX);

            final File savedDir = mIsTraining ? dirs.train : dirs.test;
            FileUtils.makeDirs(savedDir);

            List<Integer> labels;

            if (MNISTUtils.gunzip(sourceLabels, targetLabels)
                    && mSign.get() != QUIT_SIGN
                    && MNISTUtils.gunzip(sourceImages, targetImages)
                    && mSign.get() != QUIT_SIGN
                    && !(labels = MNISTUtils.parseLabels(targetLabels)).isEmpty()
                    && mSign.get() != QUIT_SIGN
                    && MNISTUtils.parseImages(targetImages, savedDir, labels)) {
                if (mSign.decrementAndGet() == 0) {
                    MainService.this.onDecompressComplete(true);
                }
            } else if (mSign.get() != QUIT_SIGN) {
                mSign.set(QUIT_SIGN);
                MainService.this.onDecompressComplete(false);
            }
        }
    }

    private class DownloadReceiver extends BroadcastReceiver {
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
                    MainService.this.onDownloadComplete(true);
                }
            } else {
                for (Long id : mIds) {
                    mDownloadManager.remove(id);
                }

                MainService.this.onDownloadComplete(false);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Unsupported");
    }
}
