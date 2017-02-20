package com.amy.inertia.util;

import android.content.Context;
import android.util.DisplayMetrics;
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

}
