package ch.digorydoo.titanium.main.texture

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.font.FontManager.FontName
import ch.digorydoo.titanium.engine.image.ImageData
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.engine.texture.TextureManager
import ch.digorydoo.titanium.main.opengl.checkGLError
import org.lwjgl.opengl.GL13.*
import org.lwjgl.stb.STBImage.stbi_failure_reason
import org.lwjgl.stb.STBImage.stbi_load
import kotlin.math.ceil
import kotlin.math.max

class TextureManagerImpl: TextureManager {
    enum class SamplerUnit(val index: Int) { TEXTURE(0), SHADOW_MAP(1) }

    // FIXME textures stored in this map are retained forever! Should call tex.dangerouslyFree at some point!
    private val textures = mutableMapOf<String, Texture>()

    /**
     * Retrieves or loads a texture from a file. The returned texture always has the shared bit set. The texture is
     * managed by TextureManager and must not be freed by the caller.
     */
    override fun getOrCreateTexture(fname: String): Texture? {
        val entry = textures[fname]

        if (entry != null) {
            return entry
        }

        checkGLError()

        val imgData = loadImage(fname) ?: return null

        val texId = makeTexture()

        if (texId == null) {
            Log.error("TextureManagerImpl: Failed to create GL texture for $fname")
            return null
        }

        val tex = TextureImpl(texId, imgData, fname, shared = true)
        textures[fname] = tex
        return tex
    }

    /**
     * Creates an empty texture to draw into. The caller is responible for freeing it when it's no longer needed! The
     * texture is not maintained by TextureManager, and thus not added to our textures map.
     */
    override fun createTexture(width: Int, height: Int): Texture {
        val texId = makeTexture()
            ?: throw Exception("Failed to create new texture!")

        val imgData = ImageData(ImageData.Type.RGBA8, width, height)
        return TextureImpl(texId, imgData, "", shared = false)
    }

    /**
     * Creates a texture for a line of text. The caller is responible for freeing it when it's no longer needed.
     */
    override fun createTexture(
        text: String,
        width: Int?,
        height: Int?,
        allowNewlines: Boolean,
        lineSpacing: Int, // only applies when allowNewlines is true
        font: FontName,
        padding: Int,
        fgColour: Colour,
        bgColour: Colour,
        otlColour: Colour?,
    ): Texture {
        var w: Int
        var h: Int
        var lineHeight: Int

        val lines = when (allowNewlines) {
            true -> text.split("\n")
            false -> listOf(text)
        }

        if (!allowNewlines && width != null && height != null) {
            // No need to call measureText
            w = width
            h = height
            lineHeight = 0
        } else {
            w = width ?: 0
            h = height ?: (2 * padding + lineSpacing * (lines.size - 1))
            lineHeight = 0

            lines.forEach { line ->
                val sz = App.fonts.measureText(line, font)
                lineHeight = max(lineHeight, ceil(sz.y).toInt())

                if (width == null) {
                    w = max(w, ceil(sz.x).toInt() + 2 * padding)
                }

                if (height == null) {
                    h += ceil(sz.y).toInt()
                }
            }
        }

        return App.textures.createTexture(w, h).apply {
            drawInto {
                clear(bgColour)
                lines.forEachIndexed { i, line ->
                    drawText(line, padding, padding + i * (lineHeight + lineSpacing), fgColour, font, otlColour)
                }
            }
        }
    }

    private fun loadImage(fname: String): ImageData? {
        val path = App.assets.pathToTexture(fname)
        val arrWidth = intArrayOf(0)
        val arrHeight = intArrayOf(0)
        val arrChannels = intArrayOf(0)
        val data = stbi_load(path, arrWidth, arrHeight, arrChannels, 4)

        if (data == null) {
            Log.error("Failed to load texture: ${stbi_failure_reason()}: $path")
            return null
        }

        val width = arrWidth[0]
        val height = arrHeight[0]
        Log.info("TextureManager: $fname: ${width}x${height}")

        return ImageData(data, ImageData.Type.RGBA8, width, height)
    }

    private fun makeTexture(): Int? {
        glActiveTexture(GL_TEXTURE0 + SamplerUnit.TEXTURE.index)
        checkGLError()

        val texId = glGenTextures()

        if (texId < 0) {
            Log.error("genTextureFromData: Failed to generate texture!")
            return null
        }

        checkGLError()

        // Bind the id to the active texture unit.

        glBindTexture(GL_TEXTURE_2D, texId)
        checkGLError()

        // We don't use a pixel buffer object (PBO). Should we?

        // glGenBuffers(1, &m_nPboId)
        // glBindBuffer(GL_PIXEL_UNPACK_BUFFER, m_nPboId)
        // glBufferData(GL_PIXEL_UNPACK_BUFFER, m_nImgWidth * m_nImgHeight * 4 * sizeof (GLubyte), m_pData, GL_STATIC_DRAW)

        // Setup texture parameters

        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0)  // 0 means row length is equal to image width
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)   // 1 means rows are byte-aligned
        checkGLError()

        // We don't use a PBO (see above).

        // glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0)

        return texId
    }
}
