package ch.digorydoo.titanium.engine.scene

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.titanium.engine.core.GameTime.Companion.RELTIME0200
import ch.digorydoo.titanium.engine.core.GameTime.Companion.RELTIME0600
import ch.digorydoo.titanium.engine.core.GameTime.Companion.RELTIME1200
import ch.digorydoo.titanium.engine.core.GameTime.Companion.RELTIME1800
import ch.digorydoo.titanium.engine.core.GameTime.Companion.RELTIME2200
import ch.digorydoo.titanium.engine.sky.SunDirection

open class Lighting(
    open val ambientLight: Colour, // will be multiplied by texture (NOT added on top!)
    open val diffuseLight: Colour,
    open val skyColour1: Colour, // near sun
    open val skyColour2: Colour, // opposite sun
    open val sunColour: Colour,
    open val sunDir: SunDirection,
    open val sunOpacity: Float, // 0=occluded
    open val hazyColour: Colour,
    open val haziness: Float, // 0=not hazy, 1=dense fog
    open val texIntensity: Float, // 1=texture have full contrast; 0=texture does nothing
    open val shadowIntensity: Float, // 1=strong shadows; 0=no shadows
    open val brightness: Float, // 1=fully bright
    open val contrast: Float, // 1=no change
) {
    protected constructor(): this(
        Colour.black,
        Colour.black,
        Colour.black,
        Colour.black,
        Colour.black,
        SunDirection.sunAtNoon,
        1.0f,
        Colour.black,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f
    )

    companion object {
        val fineDay0200 = Lighting(
            ambientLight = Colour(0.30f, 0.30f, 0.35f),
            diffuseLight = Colour(0.0f, 0.0f, 0.0f),
            skyColour1 = Colour(0.24f, 0.24f, 0.21f),
            skyColour2 = Colour(0.12f, 0.12f, 0.16f),
            sunColour = Colour(1.0f, 0.8f, 0.6f),
            sunDir = SunDirection.fromRelTime(RELTIME0200),
            sunOpacity = 0.32f,
            hazyColour = Colour(0.12f, 0.12f, 0.15f),
            haziness = 0.40f,
            texIntensity = 0.8f,
            shadowIntensity = 0.0f,
            brightness = 1.0f,
            contrast = 1.0f,
        )
        val fineDay0600 = Lighting(
            ambientLight = Colour(0.98f, 0.98f, 0.99f),
            diffuseLight = Colour(0.54f, 0.54f, 0.54f),
            skyColour1 = Colour(0.77f, 0.77f, 0.98f),
            skyColour2 = Colour(0.59f, 0.59f, 0.80f),
            sunColour = Colour(1.0f, 1.0f, 0.95f),
            sunDir = SunDirection.fromRelTime(RELTIME0600),
            sunOpacity = 0.32f,
            hazyColour = Colour(0.68f, 0.68f, 0.88f),
            haziness = 0.42f,
            texIntensity = 0.8f,
            shadowIntensity = 0.1f,
            brightness = 1.0f,
            contrast = 1.0f,
        )
        val fineDay1200 = Lighting(
            ambientLight = Colour(0.99f, 0.99f, 0.99f),
            diffuseLight = Colour(0.64f, 0.64f, 0.64f),
            skyColour1 = Colour(0.61f, 0.61f, 1.0f),
            skyColour2 = Colour(0.79f, 0.79f, 1.0f),
            sunColour = Colour.white,
            sunDir = SunDirection.fromRelTime(RELTIME1200),
            sunOpacity = 0.3f, // because skyColour1 is already quite bright
            hazyColour = Colour(0.62f, 0.62f, 0.78f),
            haziness = 0.05f,
            texIntensity = 0.7f,
            shadowIntensity = 0.3f,
            brightness = 1.0f,
            contrast = 1.0f,
        )
        val fineDay1800 = Lighting(
            ambientLight = Colour(0.98f, 0.98f, 0.99f),
            diffuseLight = Colour(0.55f, 0.55f, 0.55f),
            skyColour1 = Colour(0.59f, 0.59f, 0.98f),
            skyColour2 = Colour(0.77f, 0.77f, 0.98f),
            sunColour = Colour(1.0f, 1.0f, 0.95f),
            sunDir = SunDirection.fromRelTime(RELTIME1800),
            sunOpacity = 0.32f,
            hazyColour = Colour(0.55f, 0.55f, 0.71f),
            haziness = 0.06f,
            texIntensity = 0.8f,
            shadowIntensity = 0.1f,
            brightness = 1.0f,
            contrast = 1.0f,
        )
        val fineDay2200 = Lighting(
            ambientLight = Colour(0.32f, 0.32f, 0.35f),
            diffuseLight = Colour(0.0f, 0.0f, 0.0f),
            skyColour1 = Colour(0.26f, 0.24f, 0.21f),
            skyColour2 = Colour(0.12f, 0.12f, 0.16f),
            sunColour = Colour(1.0f, 0.8f, 0.6f),
            sunDir = SunDirection.fromRelTime(RELTIME2200),
            sunOpacity = 0.32f,
            hazyColour = Colour(0.12f, 0.12f, 0.15f),
            haziness = 0.20f,
            texIntensity = 0.8f,
            shadowIntensity = 0.0f,
            brightness = 1.0f,
            contrast = 1.0f,
        )
    }
}
