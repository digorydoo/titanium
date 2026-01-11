package ch.digorydoo.titanium.game.gel.street_lamp

import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.ui.dialogue.DlgDef

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

    override fun buildEditorItems(dlgDef: DlgDef<Unit>, onChange: () -> Unit) {
        super.buildEditorItems(dlgDef, onChange)
        dlgDef.apply {
            itemWithBooleanValue {
                text = "Light on"
                initialValue = lightOn
                this.onChange = {
                    lightOn = it
                    onChange()
                }
            }
            itemWithBooleanValue {
                text = "Off during daylight"
                initialValue = offDuringDaylight
                this.onChange = {
                    offDuringDaylight = it
                    onChange()
                }
            }
            itemWithBooleanValue {
                text = "Flickering"
                initialValue = flickering
                this.onChange = {
                    flickering = it
                    onChange()
                }
            }
        }
    }

    override fun createGel() =
        StreetLampGel(this)
}

