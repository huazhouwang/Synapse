package io.whz.synapse.component;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import io.whz.synapse.EventBusIndex;
import io.whz.synapse.element.ChannelCreator;
import io.whz.synapse.element.Global;
import io.whz.synapse.pojo.constant.TrackCons;
import io.whz.synapse.pojo.dao.DaoMaster;
import io.whz.synapse.pojo.dao.DaoSession;
import io.whz.synapse.track.ExceptionHelper;
import io.whz.synapse.track.TimeHelper;
import io.whz.synapse.track.Tracker;

public class App extends Application {
    public static final String TAG = "Synapse";
    private static final String DB_NAME = "global-db";
    private static final String PREFERENCE_NAME = "global-preferences";

    private final Global mGlobal = Global.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();

        TimeHelper.getInstance()
                .start(TrackCons.APP.INITIALIZE);

        configEvenBus();
        configPreferences();
        configGreenDao();
        initTrackEngines();

        createNotificationChannel();
        hookUncaughtExceptionHandler();

        Tracker.getInstance()
                .event(TrackCons.APP.INITIALIZE)
                .put(TrackCons.Key.TIME_USED, TimeHelper.getInstance().stop(TrackCons.APP.INITIALIZE))
                .log();
    }

    private void hookUncaughtExceptionHandler() {
        final Thread.UncaughtExceptionHandler handler = new GlobalExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler());

        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

    private void initTrackEngines() {
        Tracker.getInstance()
                .initialize(getApplicationContext(),
                        mGlobal.getBus());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ChannelCreator.createChannel(this.getApplicationContext());
        }
    }

    private void configGreenDao() {
        final DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(
                getApplicationContext(), DB_NAME);

        final DaoSession session = new DaoMaster(helper.getWritableDb()).newSession();
        mGlobal.setSession(session);
    }

    private void configPreferences() {
        final SharedPreferences preferences = getApplicationContext().getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mGlobal.setPreference(preferences);
    }

    private void configEvenBus() {
        final EventBus bus = EventBus.builder()
                .addIndex(new EventBusIndex())
                .installDefaultEventBus();

        mGlobal.setBus(bus);
    }

    private static class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Nullable
        private final Thread.UncaughtExceptionHandler mDefault;

        GlobalExceptionHandler(@Nullable Thread.UncaughtExceptionHandler handler) {
            mDefault = handler;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            ExceptionHelper.getInstance()
                    .caught(throwable);

            if (mDefault != null) {
                mDefault.uncaughtException(thread, throwable);
            }
        }
    }
}
