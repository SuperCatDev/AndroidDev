package ru.niisokb.safesdk.configuration

import ru.niisokb.safesdk.mixins.ObservableProperty

/** Информация о приложении, использующем SDK. */
object AppInfo : ObservableProperty<AppInfoContent> {
    override val callbacks: MutableSet<(AppInfoContent) -> Unit> = mutableSetOf()

    var packageName: String = "unknown package"
        set(value) {
            field = value
            notifyAll(AppInfoContent(packageName, internalStoragePath))
        }

    var internalStoragePath: String = ""
        set(value) {
            field = value
            notifyAll(AppInfoContent(packageName, internalStoragePath))
        }

    // Используется для включения отладочного режима. Влияет на поведение SpLogHttpsService.
    internal var debug = false
}

data class AppInfoContent(val packageName: String, val internalStoragePath: String)
