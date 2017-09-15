package io.whz.androidneuralnetwork.utils;

import android.support.v4.util.LruCache;
import android.util.Log;

import io.whz.androidneuralnetwork.pojos.Digit;

public class BatchCache extends LruCache<String, Digit[]> {
    private static final String TAG = "BatchCache";
    private static final int MAX_SIZE = (int) (Runtime.getRuntime().maxMemory() / 8);

    @Override
    protected int sizeOf(String key, Digit[] value) {
        if (value != null && value.length > 0) {
            return value.length * value[0].colors.length * 8;
        }

        return super.sizeOf(key, value);
    }

    @Override
    public void trimToSize(int maxSize) {
        super.trimToSize(maxSize);
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, Digit[] oldValue, Digit[] newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);
        Log.i(TAG, "entryRemoved: " + key);
    }

    private BatchCache() {
        super(MAX_SIZE);
        Log.i(TAG, "BatchCache: MAX_SIZE:" + MAX_SIZE);
    }

    public static LruCache<String, Digit[]> getInstance() {
        return Holder.sInstance;
    }

    private interface Holder {
        LruCache<String, Digit[]> sInstance = new BatchCache();
    }
}
