package io.whz.androidneuralnetwork.component;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.whz.androidneuralnetwork.R;
import io.whz.androidneuralnetwork.element.Global;
import io.whz.androidneuralnetwork.element.Preference;
import io.whz.androidneuralnetwork.pojo.event.MANEvent;
import io.whz.androidneuralnetwork.pojo.multiple.binder.DataSetViewBinder;
import io.whz.androidneuralnetwork.pojo.multiple.item.DataSetItem;
import io.whz.androidneuralnetwork.transition.FabTransform;
import io.whz.androidneuralnetwork.util.FileUtil;
import me.drakeet.multitype.MultiTypeAdapter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
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

        mFab.setOnClickListener(this);

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

    private void notifyAdapterChange() {
        final List<Object> items = new ArrayList<>();

        items.add(mDataSetItem);

        mAdapter.setItems(items);
        mAdapter.notifyDataSetChanged();
    }

    public boolean isDataSetReady() {
        final Set<String> unreadyFiles = new HashSet<>(Arrays.asList(Global.getInstance().getDataSet()));

        final File downloadDir = Global.getInstance().getDirs().download;
        FileUtil.makeDirs(downloadDir);

        final File[] files = downloadDir.listFiles();

        if (files != null && files.length != 0) {
            for (File file : files) {
                unreadyFiles.remove(file.getName());
            }
        }

        return unreadyFiles.isEmpty();
    }

    private void solveData() {
        changeDownloadState(isDataSetReady());
    }

    private void changeDownloadState(boolean isDownloaded) {
        mDataSetItem.change(isDownloaded ? DataSetItem.READY : DataSetItem.UNREADY);
        notifyAdapterChange();
    }

    private void showFirstTip() {
        final String key = "first_time_v1";

        if (!Preference.getInstance()
                .getBoolean(key, true)) {
            return;
        }

        Preference.getInstance()
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
    public void onNormalEvent(MANEvent event) {
        final int what = event.what;

        switch (what) {
            case MANEvent.CLICK_DOWNLOAD:
                showConfirmDialog(this);
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
            mDataSetItem.change(DataSetItem.UNREADY);
            notifyAdapterChange();
            res = R.string.text_download_error;
        }

        Snackbar.make(mRecyclerView, res, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void handleDecompressComplete(MANEvent event) {
        final boolean success = event.obj != null ? (Boolean) event.obj : false;
        @StringRes final int res;

        if (success) {
            mDataSetItem.change(DataSetItem.READY);
            res = R.string.text_decompress_success;
        } else {
            mDataSetItem.change(DataSetItem.UNREADY);
            res = R.string.text_decompress_error;
        }

        notifyAdapterChange();
        Snackbar.make(mRecyclerView, res, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void requestDownload() {
        final Intent intent = new Intent(this, MainService.class);
        intent.putExtra(MainService.ACTION_KEY, MainService.ACTION_DOWNLOAD);

        startService(intent);
    }

    private void showConfirmDialog(@NonNull final Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.text_dialog_download_title)
                .setMessage(R.string.text_dialog_download_msg)
                .setPositiveButton(R.string.text_dialog_download_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (activity.isFinishing()) {
                            return;
                        }

                        mDataSetItem.change(DataSetItem.PENDING);
                        notifyAdapterChange();
                        requestDownload();
                    }
                }).setNegativeButton(R.string.text_dialog_download_negavite, null)
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

    private void startNeuralNetworkConfig(@NonNull View view) {
        final Intent intent = new Intent(this, NeuralModelActivity.class);

        FabTransform.addExtras(intent,
                ContextCompat.getColor(this, R.color.color_accent),
                R.drawable.ic_add_white_24dp);

        final ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                this, view, getString(R.string.transition_neural_config));

        startActivity(intent, options.toBundle());
    }
}
