package ch.digorydoo.titanium.game.gel.street_lamp

import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.ui.dialogue.DlgDef
import io.github.digorydoo.kstruct.KstructBuilder
import io.github.digorydoo.kstruct.KstructMap

class StreetLampSpawnPt(raw: KstructMap, val kind: Kind): SpawnPt(raw) {
    enum class Kind { TRADITIONAL }

    var lightOn = raw["lightOn"]?.booleanOrNull() ?: true
    var offDuringDaylight = raw["offDuringDaylight"]?.booleanOrNull() ?: true
    var flickering = raw["flickering"]?.booleanOrNull() ?: false

    override fun serialiseSpecific(builder: KstructBuilder) {
        builder.apply {
            set("lightOn", lightOn)
            set("offDuringDaylight", offDuringDaylight)
            set("flickering", flickering)
        }
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

    override fun createGel() = StreetLampGel(this)
}

