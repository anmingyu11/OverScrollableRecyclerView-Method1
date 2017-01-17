package com.amy.library;

import android.util.Log;

import java.util.Locale;

public class LogUtil {
    private static boolean ENABLE_DEBUG = true;
    public static String TAG = " AMY ";

    public static void setTAG(String tag) {
        TAG = tag;
    }

    public static void enableDebug(boolean enable) {
        ENABLE_DEBUG = enable;
    }

    private static String getTag() {
        StackTraceElement[] trace = new Throwable().fillInStackTrace()
                .getStackTrace();
        String callingClass = "";
        for (int i = 2; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(LogUtil.class)) {
                callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass
                        .lastIndexOf('.') + 1);
                break;
            }
        }
        return callingClass;
    }

    private static String buildMessage(String msg) {
        StackTraceElement[] trace = new Throwable().fillInStackTrace()
                .getStackTrace();
        String caller = "";
        for (int i = 2; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(LogUtil.class)) {
                caller = trace[i].getMethodName();
                break;
            }
        }
        return String.format(Locale.US, "[%d] %s: %s", Thread.currentThread()
                .getId(), caller, msg);
    }

    public static void v(String msg) {
        if (ENABLE_DEBUG) {
            Log.v(getTag() + TAG, buildMessage(msg));
        }
    }

    public static void d(String msg) {
        if (ENABLE_DEBUG) {
            Log.d(getTag() + TAG, buildMessage(msg));
        }
    }

    public static void i(String msg) {
        if (ENABLE_DEBUG) {
            Log.i(getTag() + TAG, buildMessage(msg));
        }
    }

    public static void w(String msg) {
        if (ENABLE_DEBUG) {
            Log.w(getTag() + TAG, buildMessage(msg));
        }
    }

    public static void e(String msg) {
        if (ENABLE_DEBUG) {
            Log.e(getTag() + TAG, buildMessage(msg));
        }
    }

    public static void printTraceStack(String msg) {
        if (ENABLE_DEBUG) {
            Exception e = new Exception();
            StackTraceElement[] steArray = e.getStackTrace();
            Log.e(TAG, "---------------- Stack Trace ---------------");
            for (StackTraceElement ste : steArray) {
                Log.e(TAG, ste.toString());
            }
        }
    }

    public static long currentTimeMillis() {
        long time = System.currentTimeMillis();// ms
        return time;
    }

    public static void duration(String msg, long start, long end) {
        Log.e(TAG, buildMessageThread(msg) + " , duration: " + (end - start) + "ms");
    }

    private static String buildMessageThread(String msg) {
        StackTraceElement[] trace = new Throwable().fillInStackTrace()
                .getStackTrace();
        String caller = "";
        for (int i = 2; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(LogUtil.class)) {
                caller = trace[i].getMethodName();
                break;
            }
        }
        Thread thread = Thread.currentThread();
        return String.format(Locale.US, "%s (%s): %s",
                thread.toString(),
                caller,
                msg);
    }

}
