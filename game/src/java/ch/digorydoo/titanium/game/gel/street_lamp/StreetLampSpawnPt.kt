package ch.digorydoo.titanium.game.gel.street_lamp

import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.ui.choice.BoolChoice
import ch.digorydoo.titanium.engine.ui.choice.Choice

class StreetLampSpawnPt(raw: Map<String, String>, val kind: Kind): SpawnPt(raw) {
    enum class Kind { TRADITIONAL }

    var lightOn = raw["lightOn"]?.toBoolean() ?: true
    var offDuringDaylight = raw["offDuringDaylight"]?.toBoolean() ?: true
    var flickering = raw["flickering"]?.toBoolean() ?: false

    override fun serialize(): MutableMap<String, String> {
        val result = super.serialize()
        result["lightOn"] = "$lightOn"
        result["offDuringDaylight"] = "$offDuringDaylight"
        result["flickering"] = "$flickering"
        return result
    }

    override fun getEditorChoices(onChange: () -> Unit): MutableList<Choice> {
        val result = super.getEditorChoices(onChange)
        result.addAll(
            listOf(
                BoolChoice("Light on", initialValue = lightOn) {
                    lightOn = it
                    onChange()
                },
                BoolChoice("Off during daylight", initialValue = offDuringDaylight) {
                    offDuringDaylight = it
                    onChange()
                },
                BoolChoice("Flickering", initialValue = flickering) {
                    flickering = it
                    onChange()
                }
            )
        )
        return result
    }

    override fun createGel() =
        StreetLampGel(this)
}

