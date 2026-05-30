package io.github.digorydoo.titanium.game.gel.test

import io.github.digorydoo.titanium.engine.gel.SpawnPt
import io.github.digorydoo.kstruct.KstructBuilder
import io.github.digorydoo.kstruct.KstructMap

class TestSpawnPt(raw: KstructMap): SpawnPt(raw) {
    // var rotSpeed = raw["rotSpeed"]?.toFloat() ?: 1.0f

    override fun serialiseSpecific(builder: KstructBuilder) {
        // builder.apply {
        //     set("rotSpeed", rotSpeed)
        // }
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

    override fun createGel() = TestGel(this)
}
