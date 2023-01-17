package com.torther.droidcasts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.IBinder;
import android.view.Display;
import android.view.IRotationWatcher;
import android.view.IWindowManager;

import java.lang.reflect.Method;

/** Created by Sean on 5/27/17. */
/* package */ final class DisplayUtil {

    private IWindowManager iWindowManager;

    @SuppressLint("PrivateApi")
    public DisplayUtil() {
        Class<?> serviceManagerClass;

        try {
            serviceManagerClass = Class.forName("android.os.ServiceManager");

            @SuppressLint("DiscouragedPrivateApi") Method getService = serviceManagerClass.getDeclaredMethod("getService", String.class);

            // WindowManager
            Object ws = getService.invoke(null, Context.WINDOW_SERVICE);

            iWindowManager = IWindowManager.Stub.asInterface((IBinder) ws);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * * Retrieves the device actual display size.
     *
     * @return {@link Point}
     */
    Point getCurrentDisplaySize() {
        try {
            Point localPoint = new Point();

            // Resolve the screen resolution for devices with OS version 4.3+
            iWindowManager.getInitialDisplaySize(0, localPoint);

            System.out.println(">>> Dimension: " + localPoint);
            return localPoint;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieve the current orientation of the primary screen.
     * @see android.view.Display#DEFAULT_DISPLAY
     */
    int getScreenRotation() {
        int rotation = 0;

        try {
            Class<?> cls = iWindowManager.getClass();
            try {
                rotation = (Integer) cls.getMethod("getRotation").invoke(iWindowManager);
            } catch (NoSuchMethodException e) {
                rotation =
                        (Integer) cls.getMethod("getDefaultDisplayRotation").invoke(iWindowManager);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(">>> Screen rotation: " + rotation);

        return rotation;
    }

    void setRotateListener(RotateListener listener) {
        try {
            Class<?> clazz = iWindowManager.getClass();

            IRotationWatcher watcher =
                    new IRotationWatcher.Stub() {
                        @Override
                        public void onRotationChanged(int rotation) {
                            if (listener != null) {
                                listener.onRotate(rotation);
                            }
                        }
                    };

            try {
                clazz.getMethod("watchRotation", IRotationWatcher.class, int.class)
                        .invoke(iWindowManager, watcher, Display.DEFAULT_DISPLAY); // 26+

            } catch (NoSuchMethodException ex) {
                clazz.getMethod("watchRotation", IRotationWatcher.class)
                        .invoke(iWindowManager, watcher);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Bitmap rotateBitmap(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        return Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    interface RotateListener {
        void onRotate(int rotate);
    }
}
