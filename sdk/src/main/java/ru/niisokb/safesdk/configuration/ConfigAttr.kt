package ru.niisokb.safesdk.configuration

/** Атрибут, содержащий информацию о хранимом параметре модуля. */
data class ConfigAttr<T : Any>(val key: String, val type: ValType, val value: T)

/** Возможные типы значений. */
enum class ValType {
    String,
    Int,
    Float,
    Byte,
    ByteArray,
    Boolean
}
