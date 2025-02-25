package ch.digorydoo.titanium.engine.image

import ch.digorydoo.kutils.rect.Recti

internal interface Blitter {
    fun blit(src: ImageData, dstX: Int, dstY: Int)
    fun blit(src: ImageData, dstX: Int, dstY: Int, colourMultiplier: Float)
    fun blitScaled(src: ImageData, dstX: Int, dstY: Int, dstDrawWidth: Int, dstDrawHeight: Int, antiAliasing: Boolean)
    fun blur3x3()
    fun extendEdge(edgeSize: Int, innerRect: Recti)
}
