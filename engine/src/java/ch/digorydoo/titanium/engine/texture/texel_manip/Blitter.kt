package ch.digorydoo.titanium.engine.texture.texel_manip

import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.titanium.engine.texture.ImageData

internal interface Blitter {
    fun blit(src: ImageData, dstX: Int, dstY: Int)
    fun blit(src: ImageData, dstX: Int, dstY: Int, colourMultiplier: Float)
    fun blitScaled(src: ImageData, dstX: Int, dstY: Int, dstDrawWidth: Int, dstDrawHeight: Int, antiAliasing: Boolean)
    fun blur3x3()
    fun extendEdge(edgeSize: Int, innerRect: Recti)
}
