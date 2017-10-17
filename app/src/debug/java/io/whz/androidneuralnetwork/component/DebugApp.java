package io.whz.androidneuralnetwork.component;

import com.squareup.leakcanary.LeakCanary;

public class DebugApp extends App {

    @Override
    public void onCreate() {
        super.onCreate();

        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this);
        }
    }
}
