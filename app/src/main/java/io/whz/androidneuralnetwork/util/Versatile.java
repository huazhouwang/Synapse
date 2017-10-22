package io.whz.androidneuralnetwork.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;

import io.whz.androidneuralnetwork.element.Global;
import io.whz.androidneuralnetwork.pojo.neural.Model;

public class Versatile {
    private static final String DEMO_MODEL_FILE = "demo.model";

    /**
     * Solve TransitionManager leak problem
     */
    public static void removeActivityFromTransitionManager(Activity activity) {
        final Class transitionManagerClass = TransitionManager.class;

        try {
            final Field runningTransitionsField = transitionManagerClass.getDeclaredField("sRunningTransitions");

            if (runningTransitionsField == null) {
                return;
            }

            runningTransitionsField.setAccessible(true);

            //noinspection unchecked
            final ThreadLocal<WeakReference<ArrayMap<ViewGroup, ArrayList<Transition>>>> runningTransitions
                    = (ThreadLocal<WeakReference<ArrayMap<ViewGroup, ArrayList<Transition>>>>)
                    runningTransitionsField.get(transitionManagerClass);

            if (runningTransitions == null
                    || runningTransitions.get() == null
                    || runningTransitions.get().get() == null) {
                return;
            }

            final ArrayMap map = runningTransitions.get().get();
            final View decorView = activity.getWindow().getDecorView();

            if (map.containsKey(decorView)) {
                map.remove(decorView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public static void writeModel2File(@NonNull Model model) {
        ObjectOutputStream objectOutputStream = null;

        try {
            final File file = new File(Global.getInstance().getDirs().root, DEMO_MODEL_FILE);

            if (file.exists()) {
                file.delete();
            }

            final FileOutputStream outputStream = new FileOutputStream(file);
            objectOutputStream = new ObjectOutputStream(outputStream);

            objectOutputStream.writeObject(model);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Nullable
    public static Model readModelFromAssert(@NonNull Context context) throws IOException, ClassNotFoundException {
        Precondition.checkNotNull(context);

        final AssetManager manager = context.getAssets();

        if (manager == null) {
            return  null;
        }

        ObjectInputStream objectInputStream = null;

        try {
            final InputStream inputStream = manager.open(DEMO_MODEL_FILE);
            objectInputStream = new ObjectInputStream(inputStream);

            return (Model) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw e;
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
