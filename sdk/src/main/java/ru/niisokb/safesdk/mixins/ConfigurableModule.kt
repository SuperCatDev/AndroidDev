package ru.niisokb.safesdk.mixins

import android.os.Bundle
import ru.niisokb.safesdk.SpConfigDispatcher
import ru.niisokb.safesdk.configuration.ConfigAttr
import ru.niisokb.safesdk.configuration.ConfigTarget

interface ConfigurableModule {

    /** Атрибуты настраиваемых параметров модуля. */
    val attrs: List<ConfigAttr<*>>

    /** Приемник бандла со значениями параметров. */
    val dispatch: (Bundle) -> Unit

    /** Регистрация в системе для получения обновлений параметров. */
    fun register() {
        SpConfigDispatcher.register(ConfigTarget(attrs, dispatch))
    }
}
