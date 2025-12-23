package ch.digorydoo.titanium.engine.heightmap

import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.ui.choice.BoolChoice
import ch.digorydoo.titanium.engine.ui.choice.Choice

class HeightMapSpawnPt(raw: Map<String, String>): SpawnPt(raw) {
    var filename = raw["f"] ?: ""; private set
    var smooth = raw["smooth"]?.toBoolean() ?: false; private set

    override fun serialize(): MutableMap<String, String> {
        val result = super.serialize()
        result["f"] = filename
        result["smooth"] = "$smooth"
        return result
    }

    override fun getEditorChoices(onChange: () -> Unit): MutableList<Choice> {
        val result = super.getEditorChoices(onChange)
        result.add(
            BoolChoice("Smooth", initialValue = smooth) {
                smooth = it
                onChange()
            }
        )
        return result
    }

    override fun createGel() = HeightMapGel(this)
}
