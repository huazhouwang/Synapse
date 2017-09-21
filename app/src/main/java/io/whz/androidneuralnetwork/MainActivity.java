package io.whz.androidneuralnetwork;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.whz.androidneuralnetwork.components.Global;
import io.whz.androidneuralnetwork.components.Preferences;
import io.whz.androidneuralnetwork.events.MANEvent;
import io.whz.androidneuralnetwork.multiple.binders.DataSetViewBinder;
import io.whz.androidneuralnetwork.multiple.items.DataSetItem;
import io.whz.androidneuralnetwork.types.Dirs;
import io.whz.androidneuralnetwork.utils.FileUtils;
import me.drakeet.multitype.MultiTypeAdapter;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 0x01;
    private static final String TAG = App.TAG + "-MainActivity";

    private final MultiTypeAdapter mAdapter = new MultiTypeAdapter();
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;

    private final DataSetItem mDataSetItem = new DataSetItem();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkDownloadManager();
        checkExternalStorage();

        mRecyclerView = findViewById(R.id.rv);
        mFab = findViewById(R.id.fab);

        registerItems();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void checkDownloadManager() {
        if (getSystemService(DOWNLOAD_SERVICE) == null) {
            // TODO: 18/09/2017 没有 DownloadManager ,提醒后退出
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void checkExternalStorage() {
        if (Global.getInstance().isRootDirSet()){
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

    public Set<String> getUnreadyFiles() {
        final Set<String> unreadyFiles = new HashSet<>(Arrays.asList(Global.getInstance().getDataSet()));

        final File downloadDir = new File(Global.getInstance().getRootDir(), Dirs.DOWNLOAD);
        FileUtils.makeDirs(downloadDir);

        final File[] files = downloadDir.listFiles();

        if (files != null && files.length != 0) {
            for (File file : files) {
                unreadyFiles.remove(file.getName());
            }
        }

        return unreadyFiles;
    }

    private void solveData() {
        isAnyDataUnready();
    }

    private boolean isAnyDataUnready() {
        final Set<String> files = getUnreadyFiles();

        // TODO: 18/09/2017 或许可以把 dataSetItem 全局变量拿掉
        mDataSetItem.change(files.isEmpty() ? DataSetItem.READY : DataSetItem.UNREADY);
        notifyAdapterChange();

        return files.isEmpty();
    }

    private void notifyAdapterChange() {
        final List<Object> items = new ArrayList<>();

        items.add(mDataSetItem);

        mAdapter.setItems(items);
        mAdapter.notifyDataSetChanged();
    }

    private void showFirstTip() {
        final String key = "first_time_v1";

        if (!Preferences.getInstance()
                .getBoolean(key, true)) {
            return;
        }

        Preferences.getInstance()
                .edit()
                .putBoolean(key, false)
                .apply();


        // TODO: 16/09/2017 show tip dialog
    }

    private void registerItems() {
        mAdapter.register(DataSetItem.class, new DataSetViewBinder());
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
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNormalEvent(MANEvent<?> event) {
        final int what = event.what;

        switch (what) {
            case MANEvent.CLICK_DOWNLOAD:
                requestDownload();
                break;

            case MANEvent.DOWNLOAD_COMPLETE:
                handleDownloadComplete(event);
                break;

            case MANEvent.DECOMPRESS_COMPLETE:
                handleDecompressComplete(event);
                break;

            default:
                break;
        }
    }

    private void handleDownloadComplete(MANEvent<?> event) {
        final boolean success = event.obj != null ? (Boolean) event.obj : false;
        mDataSetItem.change(success ? DataSetItem.READY : DataSetItem.UNREADY);
        notifyAdapterChange();
    }

    private void handleDecompressComplete(MANEvent<?> event) {
        final boolean success = event.obj != null ? (Boolean) event.obj : false;
        mDataSetItem.change(success ? DataSetItem.READY : DataSetItem.UNREADY);
        notifyAdapterChange();
        Snackbar.make(mRecyclerView, "Success", Snackbar.LENGTH_SHORT).show();
    }

    private void requestDownload() {
        final Intent intent = new Intent(this, MainService.class);
        intent.putExtra(MainService.ACTION_KEY, MainService.ACTION_DOWNLOAD);

        startService(intent);
    }
}
