package ch.digorydoo.titanium.engine.image

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.rect.Recti

internal interface OvalArtist {
    fun fill(rect: Recti, c: Colour)
    fun draw(rect: Recti, c: Colour)
    fun fillTLQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour)
    fun drawTLQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour)
    fun fillTRQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour)
    fun drawTRQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour)
    fun fillBRQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour)
    fun drawBRQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour)
    fun fillBLQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour)
    fun drawBLQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour)
}
