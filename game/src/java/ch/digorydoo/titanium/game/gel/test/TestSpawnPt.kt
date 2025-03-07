package ch.digorydoo.titanium.game.gel.test

import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.ui.choice.Choice

class TestSpawnPt(raw: Map<String, String>): SpawnPt(raw) {
    // var rotSpeed = raw["rotSpeed"]?.toFloat() ?: 1.0f

    override fun serialize(): MutableMap<String, String> {
        val result = super.serialize()
        // result["rotSpeed"] = "$rotSpeed"
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

    override fun createGel() =
        TestGel(this)
}
