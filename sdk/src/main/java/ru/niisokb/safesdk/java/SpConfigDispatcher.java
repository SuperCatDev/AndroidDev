package ru.niisokb.safesdk.java;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import ru.niisokb.safesdk.configuration.ConfigTarget;

@SuppressWarnings("unused")
public class SpConfigDispatcher {
    private static ru.niisokb.safesdk.SpConfigDispatcher instance = ru.niisokb.safesdk.SpConfigDispatcher.INSTANCE;

    public static boolean isInitialized() {
        return instance.isInitialized();
    }

    public static void load(Context context) {
        instance.load(context);
    }

    public static void unload(Context context) {
        instance.unload(context);
    }

    public static void register(ConfigTarget target) {
        instance.register(target);
    }

    public static void updateTargets(Bundle bundle) { instance.updateTargets(bundle, instance.getTargets()); }
}
