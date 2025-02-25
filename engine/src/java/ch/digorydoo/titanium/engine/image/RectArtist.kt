package ch.digorydoo.titanium.engine.image

import ch.digorydoo.kutils.colour.Colour

internal interface RectArtist {
    fun clear(c: Colour)
    fun draw(left: Int, top: Int, right: Int, bottom: Int, c: Colour)
    fun fill(left: Int, top: Int, right: Int, bottom: Int, c: Colour)
}
