package ru.niisokb.safesdk.java;

public class SpLog {
    private static ru.niisokb.safesdk.SpLog instance
            = ru.niisokb.safesdk.SpLog.INSTANCE;

    public static int v(String tag, String msg) {
        return instance.v(tag, msg);
    }

    public static int d(String tag, String msg) {
        return instance.d(tag, msg);
    }

    public static int i(String tag, String msg) {
        return instance.i(tag, msg);
    }

    public static int w(String tag, String msg) {
        return instance.w(tag, msg);
    }

    public static int e(String tag, String msg) {
        return instance.e(tag, msg);
    }

    public static int wtf(String tag, String msg) {
        return instance.wtf(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return instance.v(tag, msg, tr);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return instance.d(tag, msg, tr);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return instance.i(tag, msg, tr);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return instance.w(tag, msg, tr);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return instance.e(tag, msg, tr);
    }

    public static int wtf(String tag, String msg, Throwable tr) {
        return instance.wtf(tag, msg, tr);
    }
}
