package ch.digorydoo.titanium.engine.core

import java.io.File

/**
 * This is the abstract part of the class that deals with game asset paths.
 */
abstract class Assets {
    protected lateinit var assetsDir: String
    protected lateinit var prefsDir: String
    lateinit var pathToSaveGames: String; protected set
    lateinit var pathToLogFile: String; protected set
    lateinit var pathToCrashLockFile: String; protected set

    abstract fun initialize()
    fun pathToFont(name: String) = joinPath(assetsDir, "fonts", name)
    fun pathToGelList(name: String) = joinPath(assetsDir, "gellists", name)
    fun pathToMesh(name: String) = joinPath(assetsDir, "mesh", name)
    fun pathToPlayfield(name: String) = joinPath(assetsDir, "playfields", name)
    fun pathToPrefs(name: String) = joinPath(prefsDir, name)
    fun pathToSaveGame(name: String) = joinPath(pathToSaveGames, name)
    fun pathToShader(name: String) = joinPath(assetsDir, "shaders", name)
    fun pathToSound(name: String) = joinPath(assetsDir, "sounds", name)
    fun pathToTexture(name: String) = joinPath(assetsDir, "textures", name)

    companion object {
        @JvmStatic
        protected fun joinPath(vararg parts: String) = parts.joinToString(File.separator)
    }
}
