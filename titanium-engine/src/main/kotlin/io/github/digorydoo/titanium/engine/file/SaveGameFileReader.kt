package io.github.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.file.KDataInputStream
import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.utils.toByteBuffer
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.file.FileMarker.*
import io.github.digorydoo.titanium.engine.file.SaveGameFileWriter.Companion.POSTFIX
import io.github.digorydoo.titanium.engine.file.SaveGameFileWriter.Companion.PREFIX
import io.github.digorydoo.titanium.engine.file.SaveGameFileWriter.Summary
import io.github.digorydoo.titanium.engine.state.StateManager.MutableSerializedState
import io.github.digorydoo.titanium.engine.texture.ImageData
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File

class SaveGameFileReader private constructor(
    private val stream: KDataInputStream<FileMarker>,
    private val state: MutableSerializedState,
) {
    private var sceneTitle = ""
    private var saveDate = ""
    private var screenshot: ImageData? = null

    private fun read(summaryOnly: Boolean) {
        stream.readExpected(BEGIN_SAVE_GAME)
        var finished = false

        while (!finished) {
            val marker = stream.readMarker()

            when (marker) {
                SCENE_TITLE -> sceneTitle = stream.readUTF8()
                SAVE_DATE -> saveDate = stream.readUTF8()
                SCREENSHOT -> readScreenshot()
                END_OF_SUMMARY -> if (summaryOnly) finished = true
                INT_VALUES -> readIntValues()
                FLOAT_VALUES -> readFloatValues()
                POINT3F_VALUES -> readVector3fValues()
                END_SAVE_GAME -> finished = true
                else -> throw Exception("Unexpected marker in savegame file: $marker")
            }
        }
    }

    private fun readIntValues() {
        while (true) {
            val usKey = stream.readUShort16()
            if (usKey == 0.toUShort()) return
            val key = App.state.getStateId(usKey)
            val value = stream.readInt32()

            if (key == null) {
                Log.warn(TAG, "Ignoring key $usKey, because no matching StateId was found!")
            } else {
                state.ints[key] = value
            }
        }
    }

    private fun readFloatValues() {
        while (true) {
            val usKey = stream.readUShort16()
            if (usKey == 0.toUShort()) return
            val key = App.state.getStateId(usKey)
            val value = stream.readFloat()

            if (key == null) {
                Log.warn(TAG, "Ignoring key $usKey, because no matching StateId was found!")
            } else {
                state.floats[key] = value
            }
        }
    }

    private fun readVector3fValues() {
        while (true) {
            val usKey = stream.readUShort16()
            if (usKey == 0.toUShort()) return
            val key = App.state.getStateId(usKey)
            val value = stream.readVector3f()

            if (key == null) {
                Log.warn(TAG, "Ignoring key $usKey, because no matching StateId was found!")
            } else {
                state.vector3fs[key] = value
            }
        }
    }

    private fun readScreenshot() {
        val width = stream.readUInt16().toInt()
        val height = stream.readUInt16().toInt()
        val arr = stream.readByteArray()
        val buf = arr.toByteBuffer()
        screenshot = ImageData(buf, ImageData.Type.RGB8, width, height)
    }

    companion object {
        private val TAG = Log.Tag("SaveGameFileReader")

        fun listFiles(): List<File> =
            (File(App.assets.pathToSaveGames).listFiles()?.toList() ?: emptyList<File>())
                .filter { file ->
                    file.name.lowercase().let { it.startsWith(PREFIX) && it.endsWith(POSTFIX) }
                }

        fun anyFiles() = listFiles().isNotEmpty()

        fun listSummaries(): List<Summary> =
            listFiles()
                .sorted() // SaveGameFileWriter adds reverse-format timestamp to filenames
                .reversed() // the latest files first
                .mapNotNull { file ->
                    var summary: Summary?
                    try {
                        summary = readSummary(file.name)
                    } catch (e: Exception) {
                        Log.warn(TAG, "Failed to load summary: ${file.name}\n   ${e.message}")
                        summary = null
                    }
                    summary
                }

        private fun readSummary(fileName: String): Summary {
            val path = App.assets.pathToSaveGame(fileName)
            Log.info(TAG, "Reading summary from $path")

            return File(path).inputStream()
                .let { BufferedInputStream(it) }
                .let { DataInputStream(it) }
                .use {
                    val stream = KDataInputStream(it, FileMarker::fromUShort)
                    val state = MutableSerializedState()
                    val reader = SaveGameFileReader(stream, state)
                    reader.read(summaryOnly = true)
                    object: Summary() {
                        override val fileName = fileName
                        override val sceneTitle = reader.sceneTitle
                        override val saveDate = reader.saveDate
                        override val screenshot = reader.screenshot
                    }
                }
        }

        fun readContent(fileName: String): MutableSerializedState {
            val path = App.assets.pathToSaveGame(fileName)
            Log.info(TAG, "Reading content from $path")

            return File(path).inputStream()
                .let { BufferedInputStream(it) }
                .let { DataInputStream(it) }
                .use {
                    val stream = KDataInputStream(it, FileMarker::fromUShort)
                    val state = MutableSerializedState()
                    val reader = SaveGameFileReader(stream, state)
                    reader.read(summaryOnly = false)
                    state
                }
        }
    }
}
