package ch.digorydoo.titanium.engine.image

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.rect.Recti

internal interface RoundRectArtist {
    fun draw(r: Recti, xCornerSize: Int, yCornerSize: Int, c: Colour)
    fun fill(r: Recti, xCornerSize: Int, yCornerSize: Int, c: Colour)
}
