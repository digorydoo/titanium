package ch.digorydoo.titanium.engine.texture.texel_manip

import ch.digorydoo.kutils.colour.Colour

internal interface LineArtist {
    fun draw(x1: Int, y1: Int, x2: Int, y2: Int, c: Colour)
}
