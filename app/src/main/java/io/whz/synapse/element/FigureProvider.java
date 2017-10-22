package io.whz.synapse.element;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

import io.whz.synapse.neural.MNISTUtil;
import io.whz.synapse.pojo.neural.Figure;
import io.whz.synapse.util.Precondition;

public class FigureProvider {
    private final File mFigureFile;
    private final ThreadLocalRandom mRandom;
    @Nullable private Figure[] mFigures;

    public FigureProvider(@NonNull File file) {
        Precondition.checkNotNull(file);

        mFigureFile = file;
        mRandom = ThreadLocalRandom.current();
    }

    public void load() {
        mFigures = MNISTUtil.readFigures(mFigureFile);
    }

    @Nullable
    public Figure next() {
        if (mFigures == null
                || mFigures.length == 0) {
            return null;
        }

        return mFigures[mRandom.nextInt(mFigures.length)];
    }
}
