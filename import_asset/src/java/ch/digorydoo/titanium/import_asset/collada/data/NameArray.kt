package ch.digorydoo.titanium.import_asset.collada.data

import ch.digorydoo.kutils.string.indentLines

class NameArray {
    var id = ""
    var count = 0
    val names = mutableListOf<String>()

    override fun toString() =
        indentLines(
            arrayOf(
                "NameArray {",
                "id = \"$id\"",
                "count = $count",
                "names = [${names.joinToString(", ")}]",
                "}",
            )
        )
}
