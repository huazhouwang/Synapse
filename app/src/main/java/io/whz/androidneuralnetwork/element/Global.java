package io.whz.androidneuralnetwork.element;

import android.net.Uri;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import io.whz.androidneuralnetwork.pojo.dao.DaoSession;


public class Global {
    private final Uri mBaseMnistUri;
    private final String[] mDataSet;
    private final Singleton<EventBus> mBus;
    private final Singleton<DaoSession> mSession;
    private final Singleton<Dir> mDirs;

    private Global() {
        mBaseMnistUri = Uri.parse("http://yann.lecun.com/exdb/mnist");
        mBus = new Singleton<>();
        mSession = new Singleton<>();
        mDirs = new Singleton<>();
        mDataSet = new String[]{
                "train-images-idx3-ubyte.gz",
                "train-labels-idx1-ubyte.gz",
                "t10k-images-idx3-ubyte.gz",
                "t10k-labels-idx1-ubyte.gz",
        };
    }

    public String[] getDataSet() {
        return mDataSet;
    }

    public EventBus getBus() {
        if (mBus.get() == null) {
            mBus.bind(EventBus.getDefault());
        }

        return mBus.get();
    }

    public Uri getBaseMnistUri() {
        return mBaseMnistUri;
    }

    public void setSession(@NonNull DaoSession session) {
        mSession.bind(session);
    }

    public DaoSession getSession() {
        return mSession.get();
    }

    public void setRootDir(@NonNull File dir) {

        final Dir dirs = new Dir(dir);
        mDirs.bind(dirs);
    }

    public File getRootDir() {
        return mDirs.get().root;
    }

    public Dir getDirs() {
        return mDirs.get();
    }

    public boolean isRootDirSet() {
        return mDirs.get() != null && mDirs.get().root != null;
    }

    public static Global getInstance() {
        return Holder.sInstance;
    }

    private static class Holder {
        private static final Global sInstance = new Global();
    }
}
