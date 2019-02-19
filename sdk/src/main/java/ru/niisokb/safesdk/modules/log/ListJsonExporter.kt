package ru.niisokb.safesdk.modules.log

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import ru.niisokb.safesdk.configuration.AppInfo
import java.io.*

/**
 * Сериализует и экспортирует в файл список строк.
 *
 * Планируется применять при экспорте файлового буфера на внешнее хранилище.
 */
internal object ListJsonExporter {
    private const val FILE_NAME = "SpLogCache.json"
    private val jsonAdapter: JsonAdapter<List<String>>

    init {
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        jsonAdapter = moshi.adapter(type)
    }

    fun store(data: List<String>) {
        val cacheFile = File(AppInfo.internalStoragePath, FILE_NAME).apply {
            delete()
            createNewFile()
        }
        val outputStream = FileOutputStream(cacheFile)

        outputStream.write(jsonAdapter.toJson(data).toByteArray())
    }

    fun get(): List<String> {
        val cacheFile = File(AppInfo.internalStoragePath, FILE_NAME)

        if (!cacheFile.exists()) {
            return emptyList()
        }

        val input = BufferedReader(InputStreamReader(FileInputStream(cacheFile)))
        var line: String?
        val buffer = StringBuffer()

        do {
            line = input.readLine()
            buffer.append(line)
        } while (line?.isNotEmpty() == true)

        val result = jsonAdapter.fromJson(buffer.toString())

        return result.toList()
    }
}