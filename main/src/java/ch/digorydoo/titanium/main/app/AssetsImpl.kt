package ch.digorydoo.titanium.main.app

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.BuildConfig
import ch.digorydoo.titanium.engine.core.Assets
import java.io.File
import java.net.URLDecoder

// This class needs to stay in main, because engine gets worked into a jar, which would break accessing assets directly
class AssetsImpl: Assets() {
    private class DistRoot(val path: String, val hasAssetsInSubdir: Boolean)

    override fun initialize() {
        val distRootDir = (determineDistRootDir() ?: DistRoot(".", false))
        assetsDir = determineAssetsDir(distRootDir)
        val appDataDir = determineAppDataDir()
        val localAppDataDir = determineLocalAppDataDir(appDataDir)
        val userHomeDir = determineUserHomeDir()
        val documentsDir = determineDocumentsDir(userHomeDir)
        prefsDir = determinePrefsDir(localAppDataDir, documentsDir, userHomeDir)
        pathToSaveGames = prefsDir
        pathToLogFile = joinPath(prefsDir, "${PROJECT_NAME}.log")
        pathToCrashLockFile = joinPath(prefsDir, "${PROJECT_NAME}.lock")
    }

    companion object {
        private const val PROJECT_NAME = "titanium"
        private const val FULLY_QUALIFIED_NAME = "ch.digorydoo.$PROJECT_NAME"

        private fun mkdirOrNull(path: String): String? {
            val f = File(path)
            if (f.exists()) return path

            try {
                f.mkdir()
                return path
            } catch (_: Exception) {
                return null
            }
        }

        private fun determineDistRootDir(): DistRoot? {
            val url = AssetsImpl::class.java.getResource("AssetsImpl.class")

            if (url == null) {
                Log.error("Cannot find class")
                return null
            }

            val protocol = url.protocol

            if (protocol == null || protocol.isEmpty()) {
                Log.error("URL to class has no protocol: $url")
                return null
            }

            val head: String
            val tail: String
            val hasAssetsInSubdir: Boolean

            when {
                protocol.equals("file", ignoreCase = true) -> {
                    // We come here when the app was launched from within IDEA.
                    head = ""
                    tail = "/classes/kotlin/main/ch/digorydoo/$PROJECT_NAME/main/app/AssetsImpl.class"
                    hasAssetsInSubdir = true
                }
                protocol.equals("jar", ignoreCase = true) -> {
                    // We come here when the app was bundled through jpackage (make-bundle.sh).
                    head = "file:"
                    tail = "/main.jar!/ch/digorydoo/$PROJECT_NAME/main/app/AssetsImpl.class"
                    hasAssetsInSubdir = false
                }
                else -> {
                    Log.error("URL to class has unsupported protocol: $url")
                    return null
                }
            }

            if (!url.path.startsWith(head)) {
                Log.error("URL of class does not start with expected head: $url\nExpected: $head")
                return null
            }

            if (!url.path.endsWith(tail)) {
                Log.error("URL of class does not end with expected tail: $url\nExpected: $tail")
                return null
            }

            // Removing the expected head and tail should give us the absolute path to the dist root directory.
            var path = url.path.slice(head.length ..< url.path.length - tail.length)

            // Java encodes special characters in URLs, so we need to decode them.
            path = URLDecoder.decode(path, "UTF-8")

            if (BuildConfig.isWindows()) {
                // The path we got from the URL is standardised, but we need a Windows file path.
                path = path.replace("//", "/").replace("/", "\\")

                // When URL was "file://C:/blah", we have now "\C:\blah", so remove the leading backslash.
                if (path.startsWith("\\") && path.length > 2 && path[2] == ':') {
                    path = path.slice(1 ..< path.length)
                }
            }

            return DistRoot(path, hasAssetsInSubdir)
        }

        private fun determineAssetsDir(distRoot: DistRoot): String {
            val path = when (distRoot.hasAssetsInSubdir) {
                true -> joinPath(distRoot.path, "assets")
                false -> distRoot.path
            }

            val file = File(path)

            if (!file.isDirectory) {
                Log.error("Not a directory: $path")
            }

            return file.path
        }

        private fun determineAppDataDir(): String? {
            return if (!BuildConfig.isWindows()) null
            else System.getenv("AppData")?.takeIf { it.isNotEmpty() && File(it).exists() }
        }

        private fun determineLocalAppDataDir(appDataDir: String?): String? {
            if (!BuildConfig.isWindows()) return null
            var path = System.getenv("LocalAppData")
            if (path != null && path.isNotEmpty() && File(path).exists()) return path
            path = "$appDataDir${File.separator}..${File.separator}Local"
            if (File(path).exists()) return path
            return null
        }

        private fun determineUserHomeDir(): String? {
            val path = System.getProperty("user.home")
            if (path != null && path.isNotEmpty() && File(path).exists()) return path
            return null
        }

        private fun determineDocumentsDir(userHomeDir: String?): String? {
            if (userHomeDir == null) return null
            val path = joinPath(userHomeDir, "Documents")
            if (File(path).exists()) return path
            return null
        }

        private fun determinePrefsDir(localAppDataDir: String?, documentsDir: String?, userHomeDir: String?): String =
            when {
                BuildConfig.isWindows() -> {
                    localAppDataDir?.let { mkdirOrNull(joinPath(it, FULLY_QUALIFIED_NAME)) }
                        ?: documentsDir?.let { mkdirOrNull(joinPath(it, PROJECT_NAME)) }
                        ?: mkdirOrNull("C:\\$PROJECT_NAME")
                        ?: "C:"
                }
                else -> {
                    userHomeDir?.let { joinPath(it, "Library", "Preferences") }
                        ?.takeIf { File(it).exists() }
                        ?.let { mkdirOrNull(joinPath(it, FULLY_QUALIFIED_NAME)) }
                        ?: userHomeDir?.let { mkdirOrNull(joinPath(it, ".$PROJECT_NAME")) }
                        ?: mkdirOrNull("/tmp/$PROJECT_NAME")
                        ?: "/tmp"
                }
            }
    }
}
