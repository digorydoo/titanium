package io.github.digorydoo.titanium.engine.mesh

import ch.digorydoo.kutils.matrix.Matrix4f
import ch.digorydoo.kutils.matrix.MutableMatrix4f
import ch.digorydoo.kutils.string.indentLines

class SkelJoint(val invBindMatrix: Matrix4f) {
    val localAnimatedTransform = MutableMatrix4f() // written during rendering
    val skinTransform = MutableMatrix4f() // written during rendering

    override fun toString() =
        indentLines(
            arrayOf(
                "SkelJoint {",
                "invBindMatrix=$invBindMatrix",
                "}",
            )
        )
}
