package io.whz.androidneuralnetwork.component;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;

import org.greenrobot.eventbus.EventBus;

import io.whz.androidneuralnetwork.EventBusIndex;
import io.whz.androidneuralnetwork.element.ChannelCreator;
import io.whz.androidneuralnetwork.element.Global;
import io.whz.androidneuralnetwork.pojo.dao.DaoMaster;
import io.whz.androidneuralnetwork.pojo.dao.DaoSession;

public class App extends Application {
    public static final String TAG = "Synapse";
    private static final String DB_NAME = "global-db";
    private static final String PREFERENCE_NAME = "global-preferences";


    private final Global mGlobal = Global.getInstance();

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
}
