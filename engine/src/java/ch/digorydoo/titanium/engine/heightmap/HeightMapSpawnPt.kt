package ch.digorydoo.titanium.engine.heightmap

import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.ui.choice.Choice

class HeightMapSpawnPt(raw: Map<String, String>): SpawnPt(raw) {
    var filename = raw["f"] ?: ""

    override fun serialize(): MutableMap<String, String> {
        val result = super.serialize()
        result["f"] = filename
        return result
    }

    override fun getEditorChoices(onChange: () -> Unit): MutableList<Choice> {
        val result = super.getEditorChoices(onChange)
        // result.add(
        //     FloatChoice("Rotation speed", initialValue = rotSpeed) {
        //         rotSpeed = it
        //         onChange()
        //     }
        // )
        return result
    }

    override fun createGel() = HeightMapGel(this)
}
