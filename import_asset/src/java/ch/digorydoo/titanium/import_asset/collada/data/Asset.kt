package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class Asset {
    var contributor: Contributor? = null
    var created = ""
    var modified = ""

    override fun toString() =
        indentLines(
            arrayOf(
                "Asset {",
                "contributor = ${indentLines("$contributor")}",
                "created = \"$created\"",
                "modified = \"$modified\"",
                "}"
            )
        )
}
