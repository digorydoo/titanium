package ch.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.kutils.utils.Moment
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.FileMarker.*
import ch.digorydoo.titanium.engine.image.ImageData
import ch.digorydoo.titanium.engine.state.StateManager.SerializedState
import java.io.File

class SaveGameFileWriter private constructor(
    private val stream: MyDataOutputStream,
    private val state: SerializedState,
) {
    abstract class Summary {
        abstract val fileName: String
        abstract val sceneTitle: String
        abstract val saveDate: String
        val saveDateLocalized get() = Moment.fromString(saveDate)?.formatDateTimeLocalized() ?: ""
        abstract val screenshot: ImageData?
    }

    private fun write(summary: Summary) {
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
        state.point3fs.forEach { (key, pt) ->
            stream.writeUInt16(key.value)
            stream.write(pt)
        }
        stream.writeUInt16(0)

        stream.write(END_SAVE_GAME)
    }

    companion object {
        const val PREFIX = "savegame-" // must be lowercase (see list())
        const val POSTFIX = ".dat" // must be lowercase
        private const val MAX_NUM_SAVEGAMES = 10 // because building the list of screenshots would become too slow

        fun getNewFileName(): String {
            val stamp = Moment().formatRevDateTimeForFileName()
            return "${PREFIX}$stamp${POSTFIX}"
        }

        fun write(summary: Summary, state: SerializedState) {
            val path = App.assets.pathToSaveGame(summary.fileName)
            Log.info("SaveGameFileWriter: Writing $path")

            val file = File(path)
            MyDataOutputStream.use(file) {
                SaveGameFileWriter(it, state).write(summary)
            }

            // Also delete the oldest savegame when the list becomes too long
            try {
                SaveGameFileReader.listFiles()
                    .sorted()
                    .reversed() // latest savegames first
                    .takeIf { it.size > MAX_NUM_SAVEGAMES }
                    ?.last()
                    ?.let {
                        Log.info("Deleting old savegame: ${it.name}")
                        it.delete()
                    }
            } catch (e: Exception) {
                Log.error("Failed to delete old savegame: ${e.message}")
            }
        }
    }
}
