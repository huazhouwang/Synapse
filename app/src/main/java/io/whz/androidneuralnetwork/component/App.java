package io.whz.androidneuralnetwork.component;

import android.app.Application;
import android.os.Build;

import org.greenrobot.eventbus.EventBus;

import io.whz.androidneuralnetwork.EventBusIndex;
import io.whz.androidneuralnetwork.element.ChannelCreator;
import io.whz.androidneuralnetwork.element.Global;
import io.whz.androidneuralnetwork.element.Preference;
import io.whz.androidneuralnetwork.pojo.dao.DaoMaster;
import io.whz.androidneuralnetwork.pojo.dao.DaoSession;

public class App extends Application {
    public static final String TAG = "Synapse";
    private static final String DB_NAME = "global-db";

    @Override
    public void onCreate() {
        super.onCreate();

        configEvenBus();
        configPreferences();
        configGreenDao();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ChannelCreator.createChannel(this.getApplicationContext());
        }
    }

    private void configGreenDao() {
        final DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(
                getApplicationContext(), DB_NAME, null);

        final DaoMaster master = new DaoMaster(helper.getWritableDb());
        final DaoSession session = master.newSession();

        Global.getInstance()
                .setSession(session);
    }

    private void configPreferences() {
        Preference.initialize(getApplicationContext());
    }

    private void configEvenBus() {
        EventBus.builder()
                .addIndex(new EventBusIndex())
                .installDefaultEventBus();
    }
}
