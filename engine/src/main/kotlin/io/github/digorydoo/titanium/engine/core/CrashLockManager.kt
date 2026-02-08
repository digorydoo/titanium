package io.github.digorydoo.titanium.engine.core

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.kutils.utils.Moment
import java.io.File

class CrashLockManager {
    fun detectCrashesAndPutLockFile(): Boolean {
        // The log file should not be set up yet. Anything we log from here should just go to the tty.
        require(Log.logFile == null) { "Log file was set up too early" }
        val lockFile = File(App.assets.pathToCrashLockFile)
        var allGood = false

        try {
            if (lockFile.exists()) {
                lockFile.setLastModified(System.currentTimeMillis()) // touch the lock file
                val logFile = File(App.assets.pathToLogFile)

                if (logFile.exists()) {
                    // The name of the log file will have the date/time when we found the crash (now).
                    // This is the easiest way to ensure that it's very unlikely that the file already exists.
                    val newExt = "-crash-${Moment.now().formatAsZoneAgnosticDateTimeCompact()}.log"
                    val pathWithoutExt = logFile.path.slice(0 ..< logFile.path.length - logFile.extension.length - 1)
                    val movedLogFile = File("$pathWithoutExt$newExt")
                    logFile.renameTo(movedLogFile)
                    Log.warn(TAG, "A recent crash was detected. The old log was moved to: ${movedLogFile.path}")
                } else {
                    Log.warn(TAG, "A recent crash was detected, but no log was found!")
                }
            } else {
                lockFile.writeText("0") // we could write the process id here, but it doesn't matter
                Log.info(TAG, "Created new lock file: ${lockFile.path}")
                allGood = true
            }
        } catch (e: Exception) {
            Log.error(TAG, "Exception while detecting recent crash: ${e.message}")
        }

        return allGood
    }

    fun removeCrashLockFile() {
        File(App.assets.pathToCrashLockFile).delete()
    }

    companion object {
        private val TAG = Log.Tag("CrashLockManager")
    }
}
