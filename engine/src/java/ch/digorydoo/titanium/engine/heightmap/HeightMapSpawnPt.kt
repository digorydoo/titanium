package ch.digorydoo.titanium.engine.heightmap

import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.ui.dialogue.DlgDef
import io.github.digorydoo.kstruct.KstructBuilder
import io.github.digorydoo.kstruct.KstructMap

class HeightMapSpawnPt(raw: KstructMap): SpawnPt(raw) {
    var filename = raw["f"]?.stringOrNull() ?: ""; private set
    var smooth = raw["smooth"]?.booleanOrNull() ?: false; private set

    override fun serialiseSpecific(builder: KstructBuilder) {
        builder.apply {
            set("f", filename)
            set("smooth", smooth)
        }
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
