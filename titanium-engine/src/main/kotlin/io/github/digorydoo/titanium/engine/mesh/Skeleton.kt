package io.github.digorydoo.titanium.engine.mesh

import ch.digorydoo.kutils.matrix.Matrix4f
import ch.digorydoo.kutils.string.indentLines

class Skeleton(val id: Int, val bindShapeMatrix: Matrix4f, val joints: List<SkelJoint>) {
    override fun toString() =
        indentLines(
            arrayOf(
                "Skeleton {",
                "id=$id",
                "bindShapeMatrix=$bindShapeMatrix",
                "joints = [${indentLines(joints.joinToString(", "))}]",
                "}",
            )
        )
}
