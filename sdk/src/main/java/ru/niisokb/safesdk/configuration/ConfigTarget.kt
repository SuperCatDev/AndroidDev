package ru.niisokb.safesdk.configuration

import android.os.Bundle

data class ConfigTarget(val attrs: List<ConfigAttr<*>>, val dispatch: (Bundle) -> Unit)
