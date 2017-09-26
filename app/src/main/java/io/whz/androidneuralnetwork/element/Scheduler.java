package io.whz.androidneuralnetwork.element;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public enum Scheduler implements IThreadExecutor {
    Main(new MainThread()), Secondary(new SecondaryThread());

    private final IThreadExecutor mExecutor;

    Scheduler(IThreadExecutor executor) {
        mExecutor = executor;
    }

    @Override
    public void execute(Runnable runnable) {
        mExecutor.execute(runnable);
    }

    private static class SecondaryThread implements IThreadExecutor {
        private static int sCoreThreadNum = Runtime.getRuntime().availableProcessors() + 1;
        private final ExecutorService mExecutorService = Executors.newFixedThreadPool(sCoreThreadNum);

        @Override
        public void execute(Runnable runnable) {
            mExecutorService.execute(runnable);
        }
    }

    private static class MainThread implements IThreadExecutor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable runnable) {
            handler.post(runnable);
        }
    }

}

