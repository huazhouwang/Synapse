package io.whz.synapse.element;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import io.whz.synapse.pojo.dao.DaoSession;
import io.whz.synapse.util.Precondition;


public class Global {
    private final Uri mBaseDownloadUri;
    private final String[] mDataSet;

    private final Singleton<EventBus> mBus;
    private final Singleton<DaoSession> mSession;
    private final Singleton<Dir> mDirs;
    private final Singleton<SharedPreferences> mPreference;

    private Global() {
        mBaseDownloadUri = Uri.parse("http://yann.lecun.com/exdb/mnist");
        mDataSet = new String[]{
                "train-images-idx3-ubyte.gz",
                "train-labels-idx1-ubyte.gz",
                "t10k-images-idx3-ubyte.gz",
                "t10k-labels-idx1-ubyte.gz",
        };

        mBus = new Singleton<>();
        mSession = new Singleton<>();
        mDirs = new Singleton<>();
        mPreference = new Singleton<>();
    }

    public String[] getDataSet() {
        return mDataSet;
    }

    public Uri getBaseDownloadUri() {
        return mBaseDownloadUri;
    }

    public void setPreference(@NonNull SharedPreferences preference) {
        mPreference.setAndLock(Precondition.checkNotNull(preference));
    }

    public SharedPreferences getPreference() {
        return mPreference.get();
    }

    public void setBus(@NonNull EventBus bus) {
        mBus.setAndLock(Precondition.checkNotNull(bus));
    }

    public EventBus getBus() {
        return mBus.get();
    }

    public void setSession(@NonNull DaoSession session) {
        mSession.setAndLock(session);
    }

    public DaoSession getSession() {
        return mSession.get();
    }

    public void setRootDir(@NonNull File root) {
        final Dir dirs = new Dir(root);
        mDirs.setAndLock(dirs);
    }

    public Dir getDirs() {
        return mDirs.get();
    }

    public boolean isDirSet() {
        return mDirs.isSet();
    }

    public static Global getInstance() {
        return Holder.sInstance;
    }

    private interface Holder {
        Global sInstance = new Global();
    }
}
