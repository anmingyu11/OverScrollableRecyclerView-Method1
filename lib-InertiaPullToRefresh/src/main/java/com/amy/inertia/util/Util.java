package com.amy.inertia.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;

public class Util {

    public static int getMax(int[] arr) {
        int max = arr[0];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
            }
        }
        return max;
    }

    public static int getMaxAbs(int[] arr) {
        int max = arr[0];
        for (int i = 0; i < arr.length; i++) {
            if (Math.abs(arr[i]) > Math.abs(max)) {
                max = arr[i];
            }
        }
        return max;
    }

    public static String spellArray(int[] arr) {
        String arrString = "";
        for (int i = 0; i < arr.length; i++) {
            arrString += " [" + arr[i] + "] ";
            if (i == 4) {
                arrString += "\n";
            }
        }
        return arrString;
    }

    /**
     * From Guava lib of google.
     * return reference if it is not null.
     *
     * @param reference
     * @param <T>
     * @return
     */
    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        } else {
            return reference;
        }
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static MotionEvent fakeAMotionEventForOverScrollFooter(MotionEvent e) {
        return MotionEvent.obtain(e.getDownTime(), e.getEventTime(), MotionEvent.ACTION_DOWN, e.getX(), e.getY(), e.getMetaState());
    }

    public static MotionEvent fakeAMotionEventForOverScrollHeader(MotionEvent e) {
        return MotionEvent.obtain(e);
    }

    public static void printTouchInfo(MotionEvent e, float transY) {
        LogUtil.i("Y : " + e.getY() + transY);
        LogUtil.i("RawY : " + e.getRawY());
    }

    public static void printTouchInfo(MotionEvent e) {
        LogUtil.i("Y : " + e.getY());
        LogUtil.i("RawY : " + e.getRawY());
    }

    public static void printTouchInfo(float y, float rawY, float translatioY) {
        LogUtil.i("Y : " + y);
        LogUtil.i("translationY : " + translatioY);
        //LogUtil.i("rawY : " + rawY);
        //float y_rawY = y - rawY;
        //LogUtil.i("y - rawY : " + y_rawY);
        //float Y = y + y - rawY;
        //LogUtil.i("realY : " + Y);
    }

}
