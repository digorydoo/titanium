package ch.digorydoo.titanium.engine.heightmap

import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.ui.dialogue.DlgDef

class HeightMapSpawnPt(raw: Map<String, String>): SpawnPt(raw) {
    var filename = raw["f"] ?: ""; private set
    var smooth = raw["smooth"]?.toBoolean() ?: false; private set

    override fun serialize(): MutableMap<String, String> {
        val result = super.serialize()
        result["f"] = filename
        result["smooth"] = "$smooth"
        return result
    }

    override fun buildEditorItems(dlgDef: DlgDef<Unit>, onChange: () -> Unit) {
        super.buildEditorItems(dlgDef, onChange)
        dlgDef.apply {
            itemWithBooleanValue {
                text = "Smooth"
                initialValue = smooth
                this.onChange = {
                    smooth = it
                    onChange()
                }
            }
        }
    }

    override fun createGel() = HeightMapGel(this)
}
