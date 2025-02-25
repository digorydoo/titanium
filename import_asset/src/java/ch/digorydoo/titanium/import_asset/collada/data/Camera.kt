package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Camera {
    var id = ""
    var name = ""
    var optics: CameraOptics? = null
    var extra: CameraExtra? = null

    override fun toString() =
        indentLines(
            arrayOf(
                "Camera {",
                "id = \"$id\"",
                "name = \"$name\"",
                "optics = ${indentLines("$optics")}",
                "extra = ${indentLines("$extra")}",
                "}",
            )
        )
}
