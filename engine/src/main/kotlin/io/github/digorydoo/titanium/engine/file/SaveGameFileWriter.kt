package io.github.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.file.KDataOutputStream
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.file.FileMarker.*
import io.github.digorydoo.titanium.engine.state.StateManager.SerializedState
import io.github.digorydoo.titanium.engine.texture.ImageData
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File

class SaveGameFileWriter private constructor(private val stream: KDataOutputStream<FileMarker>) {
    abstract class Summary {
        abstract val fileName: String
        abstract val sceneTitle: String
        abstract val screenshot: ImageData?
        abstract val saveDate: String // yyyy-MM-dd HH:mm:ss
    }

    private fun write(summary: Summary, state: SerializedState) {
        stream.write(BEGIN_SAVE_GAME)
        stream.write(SCENE_TITLE, summary.sceneTitle)
        stream.write(SAVE_DATE, summary.saveDate)

        summary.screenshot?.let { screenshot ->
            stream.write(SCREENSHOT)
            stream.writeUInt16(screenshot.width)
            stream.writeUInt16(screenshot.height)
            stream.write(screenshot.buf)
        }

        stream.write(END_OF_SUMMARY)

        stream.write(INT_VALUES)
        state.ints.forEach { (key, value) ->
            stream.writeUInt16(key.value)
            stream.writeInt32(value)
        }
        stream.writeUInt16(0)

        stream.write(FLOAT_VALUES)
        state.floats.forEach { (key, value) ->
            stream.writeUInt16(key.value)
            stream.writeFloat(value)
        }
        stream.writeUInt16(0)

        stream.write(POINT3F_VALUES)
        state.vector3fs.forEach { (key, vec) ->
            stream.writeUInt16(key.value)
            stream.write(vec)
        }
        stream.writeUInt16(0)

        stream.write(END_SAVE_GAME)
    }

    companion object {
        private val TAG = Log.Tag("SaveGameFileWriter")

        const val PREFIX = "savegame-" // must be lowercase (see list())
        const val POSTFIX = ".dat" // must be lowercase
        private const val MAX_NUM_SAVEGAMES = 10 // because building the list of screenshots would become too slow

        fun getNewFileName(): String {
            val stamp = Moment.now().formatAsZoneAgnosticDateTimeCompact()
            return "${PREFIX}$stamp${POSTFIX}"
        }

        fun write(summary: Summary, state: SerializedState) {
            val path = App.assets.pathToSaveGame(summary.fileName)
            Log.info(TAG, "Writing $path")

            File(path).outputStream()
                .let { BufferedOutputStream(it) }
                .let { DataOutputStream(it) }
                .use { SaveGameFileWriter(KDataOutputStream(it)).write(summary, state) }

            deleteOldSavegames()
        }

        private fun deleteOldSavegames() {
            try {
                SaveGameFileReader.listFiles()
                    .sorted()
                    .reversed() // latest savegames first
                    .takeIf { it.size > MAX_NUM_SAVEGAMES }
                    ?.last()
                    ?.let {
                        Log.info(TAG, "Deleting old savegame: ${it.name}")
                        it.delete()
                    }
            } catch (e: Exception) {
                Log.error(TAG, "Failed to delete old savegame: ${e.message}")
            }
        }
    }
}
