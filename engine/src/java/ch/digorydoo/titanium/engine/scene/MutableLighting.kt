package ch.digorydoo.titanium.engine.scene

import ch.digorydoo.kutils.colour.MutableColour
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.core.GameTime.Companion.RELTIME0200
import ch.digorydoo.titanium.engine.core.GameTime.Companion.RELTIME0600
import ch.digorydoo.titanium.engine.core.GameTime.Companion.RELTIME1200
import ch.digorydoo.titanium.engine.core.GameTime.Companion.RELTIME1800
import ch.digorydoo.titanium.engine.core.GameTime.Companion.RELTIME2200
import ch.digorydoo.titanium.engine.sky.MutableSunDirection

class MutableLighting private constructor(
    override val ambientLight: MutableColour,
    override val diffuseLight: MutableColour,
    override val skyColour1: MutableColour,
    override val skyColour2: MutableColour,
    override val sunColour: MutableColour,
    override val sunDir: MutableSunDirection,
    override var sunOpacity: Float,
    override val hazyColour: MutableColour,
    override var haziness: Float,
    override var texIntensity: Float,
    override var shadowIntensity: Float,
    override var brightness: Float,
    override var contrast: Float,
): Lighting() {
    constructor(other: Lighting): this(
        ambientLight = MutableColour(other.ambientLight),
        diffuseLight = MutableColour(other.diffuseLight),
        skyColour1 = MutableColour(other.skyColour1),
        skyColour2 = MutableColour(other.skyColour2),
        sunColour = MutableColour(other.sunColour),
        sunDir = MutableSunDirection(other.sunDir),
        sunOpacity = other.sunOpacity,
        hazyColour = MutableColour(other.hazyColour),
        haziness = other.haziness,
        texIntensity = other.texIntensity,
        shadowIntensity = other.shadowIntensity,
        brightness = other.brightness,
        contrast = other.contrast,
    )

    fun set(other: Lighting) {
        ambientLight.set(other.ambientLight)
        diffuseLight.set(other.diffuseLight)
        skyColour1.set(other.skyColour1)
        skyColour2.set(other.skyColour2)
        sunColour.set(other.sunColour)
        sunDir.set(other.sunDir)
        sunOpacity = other.sunOpacity
        hazyColour.set(other.hazyColour)
        haziness = other.haziness
        texIntensity = other.texIntensity
        shadowIntensity = other.shadowIntensity
        brightness = other.brightness
        contrast = other.contrast
    }

    fun adaptToStoryTime() {
        // FIXME Log.info("MutableLighting: adaptToStoryTime")
        adaptToRelTime(App.time.storyTimeProgress)
    }

    fun adaptToRelTime(relTime: Float) {
        val lgtLo: Lighting
        val lgtHi: Lighting
        val tLo: Float
        val tHi: Float

        when {
            relTime < RELTIME0200 -> {
                lgtLo = fineDay2200
                lgtHi = fineDay0200
                tLo = RELTIME2200 - 1.0f // negative
                tHi = RELTIME0200
            }
            relTime < RELTIME0600 -> {
                lgtLo = fineDay0200
                lgtHi = fineDay0600
                tLo = RELTIME0200
                tHi = RELTIME0600
            }
            relTime < RELTIME1200 -> {
                lgtLo = fineDay0600
                lgtHi = fineDay1200
                tLo = RELTIME0600
                tHi = RELTIME1200
            }
            relTime < RELTIME1800 -> {
                lgtLo = fineDay1200
                lgtHi = fineDay1800
                tLo = RELTIME1200
                tHi = RELTIME1800
            }
            relTime < RELTIME2200 -> {
                lgtLo = fineDay1800
                lgtHi = fineDay2200
                tLo = RELTIME1800
                tHi = RELTIME2200
            }
            else -> {
                lgtLo = fineDay2200
                lgtHi = fineDay0200
                tLo = RELTIME2200
                tHi = 1.0f + RELTIME0200
            }
        }

        val between = (relTime - tLo) / (tHi - tLo) // 0..1

        ambientLight.setMixed(lgtLo.ambientLight, lgtHi.ambientLight, between)
        diffuseLight.setMixed(lgtLo.diffuseLight, lgtHi.diffuseLight, between)
        skyColour1.setMixed(lgtLo.skyColour1, lgtHi.skyColour1, between)
        skyColour2.setMixed(lgtLo.skyColour2, lgtHi.skyColour2, between)
        sunColour.setMixed(lgtLo.sunColour, lgtHi.sunColour, between)
        sunOpacity = lerp(lgtLo.sunOpacity, lgtHi.sunOpacity, between)
        hazyColour.setMixed(lgtLo.hazyColour, lgtHi.hazyColour, between)
        haziness = lerp(lgtLo.haziness, lgtHi.haziness, between)
        texIntensity = lerp(lgtLo.texIntensity, lgtHi.texIntensity, between)
        shadowIntensity = lerp(lgtLo.shadowIntensity, lgtHi.shadowIntensity, between)

        // For now, brightness and contrast are interpolated as well,
        // but later this will be removed, because they will be used differently
        // (fade outs, flashs of lightning, etc.)
        brightness = lerp(lgtLo.brightness, lgtHi.brightness, between)
        contrast = lerp(lgtLo.contrast, lgtHi.contrast, between)

        // sunDir is always computed from relTime, ignoring lgtLo and lgtHi
        sunDir.setFromRelTime(relTime)
    }
}
