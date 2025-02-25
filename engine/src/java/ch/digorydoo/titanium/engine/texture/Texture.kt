package ch.digorydoo.titanium.engine.texture

interface Texture {
    val width: Int
    val height: Int

    val fileName: String
    val shared: Boolean

    fun apply()
    fun freeRequireUnshared() // will fail if called on a shared texture
    fun dangerouslyFree() // must only be called from TextureManager

    fun drawInto(lambda: ImageData.() -> Unit)
    fun copyAsRGB8(): ImageData
}
