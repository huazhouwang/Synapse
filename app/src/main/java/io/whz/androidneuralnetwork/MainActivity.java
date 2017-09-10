package io.whz.androidneuralnetwork;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.whz.androidneuralnetwork.utils.Batches;
import io.whz.androidneuralnetwork.utils.FileUtils;
import io.whz.androidneuralnetwork.utils.MNISTUtils;
import io.whz.androidneuralnetwork.utils.Scheduler;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static io.whz.androidneuralnetwork.R.id.test;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_STORAGE_PERMISSION_CODE = 0x110;

    private static final String[] MNIST_TYPE = {
            "Train",
            "Test"
    };
    // TODO: 03/09/2017 改成类似 DataType 形式
    private static final String[] DIRS = {
            "Download",
            "Decompress",
            "Mnist",
            "Trained",
    };

    private static final String MNIST_BASE_URL = "http://yann.lecun.com/exdb/mnist";

    private static final String[] MNIST_FILES = {
            "train-images-idx3-ubyte.gz",
            "train-labels-idx1-ubyte.gz",
            "t10k-images-idx3-ubyte.gz",
            "t10k-labels-idx1-ubyte.gz"
    };

    private final Map<Long, String> mEnqueue2Path = new ArrayMap<>();
    private DownloadManager mDownloadManager;
    private BroadcastReceiver mReceiver;
    private String mFilesDir;
    private ImageView imageView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.download_mnist).setOnClickListener(this);
        findViewById(R.id.decompress).setOnClickListener(this);
        findViewById(R.id.train).setOnClickListener(this);
        findViewById(test).setOnClickListener(this);
        findViewById(R.id.clear).setOnClickListener(this);

        imageView = (ImageView) findViewById(R.id.image);
        textView = (TextView) findViewById(R.id.text);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION_CODE);
    }

    private List<String> getInCompletelyDownloaded() {
        final List<String> list = new ArrayList<>(Arrays.asList(MNIST_FILES));
        final File downloadDir = new File(getOrInitFilesDir(), DIRS[0]);

        if (!downloadDir.exists()) {
            return list;
        }

        for (File file : downloadDir.listFiles()) {
            list.remove(file.getName());
        }

        return list;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION_CODE:
                if (grantResults.length <= 0) {
                    break;
                }

                for (int i = 0, len = grantResults.length; i < len; ++i) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED
                            && Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[i])) {
                        mFilesDir = null;
                        getOrInitFilesDir();
                    }
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterDownloadReceiver();
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();

        switch (id) {
            case R.id.download_mnist:
                downloadMNIST();
                break;

            case R.id.decompress:
                decompress();
                break;

            case R.id.train:
                train();
                break;

            case test:
                test();
                break;

            case R.id.clear:
                clear();
                break;

            default:
                break;
        }
    }

    private void train() {
        final File mnistDir = new File(getOrInitFilesDir(), DIRS[2]);

        Batches batches = new Batches(mnistDir.listFiles());
        batches.reset();

        for (int i = 0; i < 3000; ++i) {
            batches.next();
        }
    }

    private void test() {
        final File mnistDir = new File(getOrInitFilesDir(), DIRS[2]);

        final int n = mnistDir.listFiles().length;
        final byte[] bytes = MNISTUtils.test(mnistDir.listFiles()[(int) (Math.random() * n)]);
        final int[] colors = new int[bytes.length];
        int color;

        for (int i = 0, len = bytes.length; i < len; ++i) {
            color = 0xFF - (0xFF & bytes[i]);
            colors[i] = color << 16 | color << 8 | color | 0xFF000000;
        }

        final Bitmap bitmap = Bitmap.createBitmap(colors, 28, 28, Bitmap.Config.ARGB_8888);

        Scheduler.Main.execute(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    private void decompress() {
        final List<String> list = getInCompletelyDownloaded();

        if (!list.isEmpty()) {
            Toast.makeText(this, "Please download all mnist resources first", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        final String baseDir = getOrInitFilesDir();

        Scheduler.Secondary.execute(new ParseRunnable(
                MNIST_FILES[0], MNIST_FILES[1], baseDir, MNIST_TYPE[0]));

//        Scheduler.Secondary.execute(new ParseRunnable(
//                MNIST_FILES[2], MNIST_FILES[3], baseDir, MNIST_TYPE[1]));
    }

    private static class ParseRunnable implements Runnable {
        private static final String DECOMPRESSED_LABEL_SUFFIX = ".label";
        private static final String DECOMPRESSED_IMAGE_SUFFIX = ".image";
        private final String mImageFile;
        private final String mLabelFile;
        private final String mBaseDir;
        private final String mType;

        private ParseRunnable(String imageFile, String labelFile, String baseDir, String type) {
            mImageFile = imageFile;
            mLabelFile = labelFile;
            mBaseDir = baseDir;
            mType = type;
        }

        @Override
        public void run() {
            final File downloadDir = new File(mBaseDir, DIRS[0]);
            final File decompressDir = new File(mBaseDir, DIRS[1]);
            final File mnistDir = new File(mBaseDir, DIRS[2]);

            FileUtils.makeDirs(decompressDir, mnistDir);

            final File sourceLabels = new File(downloadDir, mLabelFile);
            final File targetLabels = new File(decompressDir, mLabelFile.substring(0, mLabelFile.lastIndexOf('.')) + DECOMPRESSED_LABEL_SUFFIX);
            final File sourceImages = new File(downloadDir, mImageFile);
            final File targetImages = new File(decompressDir, mImageFile.substring(0, mImageFile.lastIndexOf('.')) + DECOMPRESSED_IMAGE_SUFFIX);

            if (!(MNISTUtils.gunzip(sourceLabels, targetLabels)
                    && MNISTUtils.gunzip(sourceImages, targetImages))) {
                throw new IllegalStateException();
            }

            final List<Integer> labels = MNISTUtils.parseLabels(targetLabels);

            MNISTUtils.parseImages(targetImages, mnistDir, mType, labels);
        }
    }

    private String getOrInitFilesDir() {
        if (mFilesDir != null) {
            return mFilesDir;
        }

        File dir = null;

        if (requestStoragePermission()
                && (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && !Environment.isExternalStorageRemovable())) {
            dir = getExternalFilesDir(null);
        }

        if (dir == null) {
            dir = getFilesDir();
        }

        mFilesDir = dir.getPath();

        return mFilesDir;
    }

    private boolean requestStoragePermission() {
        return PermissionChecker.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    }

    private void clear() {
        final File dir = new File(getOrInitFilesDir(), DIRS[2]);

        FileUtils.clear(dir);

        dir.mkdir();
    }

    private void downloadMNIST() {
        if (mDownloadManager == null) {
            mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

            if (mDownloadManager == null) {
                Toast.makeText(this, "Can't find DownloadManger", Toast.LENGTH_SHORT)
                        .show();

                finish();
            }
        }

        final List<String> need2Download = getInCompletelyDownloaded();

        if (need2Download.isEmpty()) {
            Toast.makeText(this, "Has already downloaded all mnist resources", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        unregisterDownloadReceiver();
        mReceiver = new DownloadReceiver();
        registerReceiver(mReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        final File downloadDir = new File(getOrInitFilesDir(), DIRS[0]);
        final Uri baseUri = Uri.parse(MNIST_BASE_URL);

        for (String url : need2Download) {
            final Uri uri = Uri.withAppendedPath(baseUri, url);
            final DownloadManager.Request request = new DownloadManager.Request(uri);

            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            request.setVisibleInDownloadsUi(true);
            request.setDestinationUri(Uri.fromFile(new File(downloadDir, url)));

            final long id = mDownloadManager.enqueue(request);
            mEnqueue2Path.put(id, uri.getPath());
        }
    }

    private class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                return;
            }

            final long downloadedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (downloadedId == -1
                    || !mEnqueue2Path.containsKey(downloadedId)) {
                return;
            }

            final String targetFileName = mEnqueue2Path.get(downloadedId);
            final DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadedId);

            final Cursor cursor = mDownloadManager.query(query);

            if (cursor.moveToFirst()) {
                final int index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);

                if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(index)) {
                    mEnqueue2Path.remove(downloadedId);

                    if (mEnqueue2Path.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Download completely", Toast.LENGTH_SHORT)
                                .show();

                        unregisterDownloadReceiver();
                    } else {
                        Toast.makeText(MainActivity.this, String.format("Downloaded %s", targetFileName), Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        }
    }

    private void unregisterDownloadReceiver() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }
}
