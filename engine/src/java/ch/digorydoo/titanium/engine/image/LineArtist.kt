package ch.digorydoo.titanium.engine.image

import ch.digorydoo.kutils.colour.Colour

internal interface LineArtist {
    fun draw(x1: Int, y1: Int, x2: Int, y2: Int, c: Colour)
}
