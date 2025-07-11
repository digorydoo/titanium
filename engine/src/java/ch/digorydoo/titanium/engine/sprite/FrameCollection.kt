package ch.digorydoo.titanium.engine.sprite

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.texture.ImageData
import ch.digorydoo.titanium.engine.texture.Texture

/**
 * This class interpretes a given texture as a collection of multiple frames in a regular texture grid.
 */
class FrameCollection {
    var tex: Texture? = null; private set
    val texOffset = MutablePoint2f()
    val frameSize = MutablePoint2f()

    private var numFramesX = 1
    private var numFramesY = 1

    fun setTexture(img: ImageData) {
        tex = App.textures.getOrCreateTexture(img).also {
            setSize(it.width, it.height)
        }
    }

    fun setTexture(img: ImageData, theNumFramesX: Int, theNumFramesY: Int) {
        tex = App.textures.getOrCreateTexture(img)
        setFrameCountAndSize(theNumFramesX, theNumFramesY)
    }

    private fun setSize(width: Int, height: Int) {
        val w = width.toFloat()
        val h = height.toFloat()
        frameSize.set(w, h)
        texOffset.set(0.0f, 0.0f)
        numFramesX = 1
        numFramesY = 1
    }

    private fun setFrameCountAndSize(fx: Int, fy: Int) {
        val texWidth = tex?.width ?: 0
        val texHeight = tex?.height ?: 0
        frameSize.set((texWidth / fx).toFloat(), (texHeight / fy).toFloat())
        texOffset.set(0.0f, 0.0f)
        numFramesX = fx
        numFramesY = fy
    }

    fun setFrame(frame: Int) {
        if (frame < 0 || frame >= numFramesX * numFramesY) {
            throw IllegalArgumentException("Argument out of bounds: frame")
        }

        val fy: Int = frame / numFramesX
        val fx = frame - fy * numFramesX
        val texWidth = tex?.width ?: 0
        val texHeight = tex?.height ?: 0
        texOffset.set((texWidth * fx / numFramesX).toFloat(), (texHeight * fy / numFramesY).toFloat())
    }
}
