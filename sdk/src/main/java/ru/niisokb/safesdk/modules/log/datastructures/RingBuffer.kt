package ru.niisokb.safesdk.modules.log.datastructures

import android.util.Log
import arrow.core.Try
import arrow.core.orNull
import java.io.File
import java.io.FileDescriptor
import java.io.RandomAccessFile
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

/**
 * File ring buffer for [String] elements.
 *
 * File header format:
 * [4 bytes] Head
 * [4 bytes] Version
 * [8 bytes] Global index
 * [4 bytes] Element size (Bytes)
 * [4 bytes] Capacity (Elements)
 *
 * Element header format:
 * [1 byte] bit#0 -- validity, bit#1 -- first element
 *
 * Element content string is a zero-terminated character string. It is prepended with its global
 * index converted to string format.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
internal class RingBuffer(val file: File, val capacity: Int = 1000) {
    companion object {
        private const val TAG = "RingBuffer"
        /** File head */
        private const val HEAD = "RBF "
        /** File format version */
        private const val VERSION = 2
        /** Maximum size of the content string */
        private const val CONTENT_STRING_SIZE = 1022
        /** File header: head + version + global index + elements size + capacity */
        private const val FILE_HEADER_SIZE = HEAD.length + 4 + 8 + 4 + 4
        /** Element: header + global index + string + terminator */
        private const val ELEMENT_SIZE = CONTENT_STRING_SIZE + 2
        /** Offset from the beginning of the file to global index field */
        private const val GLOBAL_INDEX_POSITION = 8L // global persistent index for the next log message

        private const val FIRST_FLAG: Byte = 0b10
        private const val VALID_FLAG: Byte = 0b01
    }

    val size get() = bufferHolder?.size ?: 0

    private val fileSize = (FILE_HEADER_SIZE + ELEMENT_SIZE * capacity).toLong()
    private var bufferHolder: BufferHolder? = openFile(file)
    private val globalIndex: Long get() = bufferHolder?.globalIndex ?: 1L

    /** Removes all of the elements from the buffer. */
    fun clear() {
        bufferHolder = newFile(file, globalIndex)
    }

    /** Inserts a specified element into the buffer, evicting the head element if the buffer is full. */
    fun add(element: String) {
        val bufferHolder = getValidBufferHolder() ?: return

        // Do not alter the state until I/O is successful
        val buffer = bufferHolder.buffer
        var first = bufferHolder.first
        var last = bufferHolder.last
        var sz = bufferHolder.size
        var globalIndex = bufferHolder.globalIndex

        // Prepend the content string with its global index
        val withIndex = "$globalIndex $element"

        // Truncate and copy element to output array
        val truncated = withIndex.substring(0, withIndex.length.coerceAtMost(CONTENT_STRING_SIZE))
        val output = ByteArray(ELEMENT_SIZE + 1)  // element + header of the next element
        System.arraycopy(truncated.toByteArray(), 0, output, 1, truncated.length)

        // Update headers of current and next element
        if (first == last && sz > 0) {
            // Buffer is full, evicting
            output[0] = VALID_FLAG
            output[ELEMENT_SIZE] = VALID_FLAG or FIRST_FLAG
            first = last.nextIndex()
            sz--
        } else if (first == last && sz == 0) {
            // Buffer is empty
            output[0] = VALID_FLAG or FIRST_FLAG
        } else if (first == last.nextIndex()) {
            // Writing to the last empty slot
            output[0] = VALID_FLAG
            output[ELEMENT_SIZE] = VALID_FLAG or FIRST_FLAG
        } else {
            // Writing to partially filled buffer
            output[0] = VALID_FLAG
        }
        val index = last
        last = last.nextIndex()

        Try {
            // Write result to file
            buffer.seek(index.indexToPosition())
            if (index == capacity - 1) {
                // Traverse edge
                buffer.write(output.sliceArray(0 until output.size - 1))
                buffer.seek(FILE_HEADER_SIZE.toLong())
                buffer.writeByte(output[ELEMENT_SIZE].toInt())
            } else {
                buffer.write(output)
            }

            sz++
            globalIndex++

            // Persist global index
            buffer.seek(GLOBAL_INDEX_POSITION)
            buffer.writeLong(globalIndex)
        }.fold({
            bufferHolder.buffer.close()
            this.bufferHolder = null
        }, {
            this.bufferHolder = BufferHolder(buffer, first, last, sz, globalIndex)
        })
    }

    /** Adds all of the elements in the specified collection to the buffer. */
    fun addAll(elements: Collection<String>) {
        if (elements.size > capacity) {
            val n = elements.size - capacity
            bufferHolder = newFile(file, globalIndex + n) // clear buffer, keep global index
            elements.drop(n).forEach { add(it) }
        } else {
            elements.forEach { add(it) }
        }
    }

    /** Retrieves and removes the head of the buffer. */
    fun remove(): String? {
        val bufferHolder = getValidBufferHolder() ?: return null

        val buffer = bufferHolder.buffer
        var first = bufferHolder.first
        var sz = bufferHolder.size

        if (sz == 0) {
            throw NoSuchElementException("Trying to remove from empty buffer")
        }

        return Try {
            val pos = first.indexToPosition()

            // Update current header
            buffer.seek(pos)
            buffer.writeByte(0)

            // Read element
            val input = ByteArray(ELEMENT_SIZE - 1)
            buffer.read(input)

            // Get string length
            val zero: Byte = 0
            var len = 0
            while (input[len] != zero) {
                len++
            }

            // Extract string
            val element = String(input.sliceArray(0 until len))

            // Update header of the next element
            val newHeader = (if (sz == 1) 0 else VALID_FLAG) or FIRST_FLAG
            first = first.nextIndex()
            buffer.seek(first.indexToPosition())
            buffer.writeByte(newHeader.toInt())

            // Decrement buffer size
            sz--

            element
        }.fold({
            buffer.close()
            this.bufferHolder = null
            null
        }, {
            this.bufferHolder = BufferHolder(buffer, first, bufferHolder.last, sz, bufferHolder.globalIndex)
            it
        })
    }

    /** Retrieves and removes [n] or all remaining elements of the buffer. */
    fun removeMany(n: Int): List<String> {
        val bufferHolder = getValidBufferHolder() ?: return listOf()
        val elements = mutableListOf<String>()
        val k = n.coerceAtMost(bufferHolder.size)
        repeat(k) {
            remove()?.let { elements.add(it) }
        }

        return elements
    }

    /** Check if file descriptor is valid, and reopen file if BFD. */
    private fun getValidBufferHolder(): BufferHolder? {
        if (bufferHolder?.fd?.valid() != true) bufferHolder = openFile(file)
        return bufferHolder
    }

    /** Tries to open file, checks if buffer is valid, if not then tries to create a new file. */
    private fun openFile(file: File): BufferHolder? = Try {
        if (!file.exists() || file.isDirectory) return@Try null

        val buffer = RandomAccessFile(file, "rwd")

        Try {
            if (!validateBuffer(buffer)) throw IllegalStateException("Buffer validation failed")

            // Locate first element
            var first = 0
            var sz = 0
            buffer.seek(first.indexToPosition())
            while (first < capacity && !buffer.readByte().isFirst()) {
                buffer.seek((++first).indexToPosition())
            }

            // Locate last element
            var last = first
            buffer.seek(last.indexToPosition())
            if (buffer.readByte().isValid()) {
                do {
                    sz++  // count elements number
                    last = last.nextIndex()
                    buffer.seek(last.indexToPosition())
                } while (buffer.readByte().isValid() && last != first)
            }

            // Read global index
            buffer.seek(GLOBAL_INDEX_POSITION)
            val globalIndex = buffer.readLong()

            BufferHolder(buffer, first, last, sz, globalIndex)
        }.fold({ buffer.close(); throw it }, { it })

    }.fold({ e ->
        Log.e(TAG, "[openFile] Failed with $e")
        null
    }, { it }) ?: newFile(file)

    private fun validateBuffer(buffer: RandomAccessFile): Boolean {
        val errorPrefix = "Buffer validation failed:"

        return Try {
            val headBytes = ByteArray(HEAD.length)
            buffer.read(headBytes)
            val head = String(headBytes)
            if (HEAD != head) {
                Log.w(TAG, "$errorPrefix Invalid header (expected '$HEAD', found '$head')")
                return@Try false
            }
            val version = buffer.readInt()
            if (VERSION != version) {
                Log.w(TAG, "$errorPrefix Version mismatch (expected $VERSION, found $version)")
                return@Try false
            }

            buffer.readLong() // global index

            val elementSize = buffer.readInt()
            if (ELEMENT_SIZE != elementSize) {
                Log.w(TAG, "$errorPrefix Wrong element size (expected $ELEMENT_SIZE, found $elementSize)")
                return@Try false
            }
            val foundCapacity = buffer.readInt()
            if (capacity != foundCapacity) {
                Log.w(TAG, "$errorPrefix Wrong capacity (expected $capacity, found $foundCapacity)")
                return@Try false
            }

            // Validate file size
            val fileSize = buffer.length()
            if (this.fileSize != fileSize) {
                Log.w(TAG, "$errorPrefix Wrong file size (expected ${this.fileSize}, found $fileSize)")
                return@Try false
            }

            // Validate first element
            var firstElementIndex = -1
            repeat(capacity) {
                buffer.seek(it.indexToPosition())
                if (buffer.readByte().isFirst()) {
                    if (firstElementIndex != -1) {
                        return@Try false
                    }
                    firstElementIndex = it
                }
            }
            if (firstElementIndex == -1) {
                return@Try false
            }

            // Validate buffer consistency
            var cursor = firstElementIndex.indexToPosition()
            var len: Long = 0
            while (true) {
                buffer.seek(cursor)
                cursor = cursor.incrementCursor()
                if (buffer.readByte().isValid() && cursor.positionToIndex() != firstElementIndex) len++ else break
            }
            while (cursor.positionToIndex() != firstElementIndex) {
                buffer.seek(cursor)
                cursor = cursor.incrementCursor()
                if (buffer.readByte().isValid()) {
                    return@Try false
                }
            }

            return@Try true
        }.fold({ e ->
            Log.e(TAG, "[validateBuffer] Failed with $e")
            false
        }, { it })
    }

    private fun newFile(bufferFile: File, globalIndex: Long = 1L): BufferHolder? = Try {
        if (bufferFile.exists()) {
            bufferFile.delete()
        }

        bufferFile.createNewFile()
        bufferFile.setReadable(true, true)
        bufferFile.setWritable(true, true)

        val buffer = RandomAccessFile(bufferFile, "rwd")

        Try {
            // Allocate space
            buffer.seek(fileSize - 1)
            buffer.writeByte(0)
            buffer.seek(0)

            // Write header
            buffer.writeBytes(HEAD)
            buffer.writeInt(VERSION)
            buffer.writeLong(globalIndex) // new file resets global index
            buffer.writeInt(ELEMENT_SIZE)
            buffer.writeInt(capacity)

            // Write first element header
            buffer.seek(FILE_HEADER_SIZE.toLong())
            buffer.writeByte(FIRST_FLAG.toInt())

            BufferHolder(buffer, 0, 0, 0, globalIndex)
        }.fold({ buffer.close(); throw it }, { it })

    }.fold({ e ->
        Log.e(TAG, "[newFile] Failed with $e")
        null
    }, { it })

    private fun Byte.isFirst() = this and FIRST_FLAG == FIRST_FLAG

    private fun Byte.isValid() = this and VALID_FLAG == VALID_FLAG

    private fun Int.nextIndex(): Int = (this + 1) % capacity

    private fun Long.incrementCursor(): Long = this.positionToIndex().nextIndex().indexToPosition()

    private fun Long.positionToIndex(): Int = ((this - FILE_HEADER_SIZE) / ELEMENT_SIZE).toInt()

    private fun Int.indexToPosition(): Long = (FILE_HEADER_SIZE + (this * ELEMENT_SIZE)).toLong()

    private data class BufferHolder(val buffer: RandomAccessFile, val first: Int, val last: Int, val size: Int, val globalIndex: Long) {
        val fd: FileDescriptor? get() = Try { buffer.fd }.orNull()
    }
}
