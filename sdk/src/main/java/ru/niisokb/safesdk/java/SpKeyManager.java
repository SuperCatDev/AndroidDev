package ru.niisokb.safesdk.java;

import android.annotation.SuppressLint;
import android.os.Build;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.RequiresApi;

/**
 * Предоставляет функции шифрования.
 */
public class SpKeyManager {
    public static String MODE_ECB = ru.niisokb.safesdk.SpKeyManager.MODE_ECB;
    public static String MODE_CBC = ru.niisokb.safesdk.SpKeyManager.MODE_CBC;

    private static ru.niisokb.safesdk.SpKeyManager instance =
            ru.niisokb.safesdk.SpKeyManager.INSTANCE;

    @NotNull
    public static String getCurrentMode() {
        return instance.getCurrentMode();
    }

    @SuppressLint("GetInstance")
    @RequiresApi(Build.VERSION_CODES.M)
    public static void setEncryptionMode(String mode) {
        instance.setEncryptionMode(mode);
    }

    /**
     * Шифрует [ByteArray] и возвращает его.
     */
    @NotNull
    public static byte[] encrypt(byte[] data) {
        return instance.encrypt(data);
    }

    /**
     * Дешифрует [ByteArray] и возвращает его.
     */
    @NotNull
    public static byte[] decrypt(byte[] data) {
        return instance.decrypt(data);
    }
}
