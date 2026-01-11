package ch.digorydoo.titanium.game.gel.test

import ch.digorydoo.titanium.engine.gel.SpawnPt

class TestSpawnPt(raw: Map<String, String>): SpawnPt(raw) {
    // var rotSpeed = raw["rotSpeed"]?.toFloat() ?: 1.0f

    override fun serialize(): MutableMap<String, String> {
        val result = super.serialize()
        // result["rotSpeed"] = "$rotSpeed"
        return result
    }

    // override fun buildEditorItems(dlgDef: DlgDef, onChange: () -> Unit) {
    //     super.buildEditorItems(dlgDef, onChange)
    //     dlgDef.apply {
    //         itemWithBooleanValue {
    //             text = "Dummy"
    //             initialValue = dummy
    //             this.onChange = {
    //                 dummy = it
    //                 onChange()
    //             }
    //         }
    //     }
    // }

    override fun createGel() =
        TestGel(this)
}
