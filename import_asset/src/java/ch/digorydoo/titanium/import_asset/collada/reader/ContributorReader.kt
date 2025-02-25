package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Contributor
import org.w3c.dom.Element

class ContributorReader(node: Element): XMLTreeReader(node) {
    fun read(): Contributor {
        val contributor = Contributor()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "author" -> contributor.author += getChildValue(child)
                "authoring_tool" -> contributor.authoringTool += getChildValue(child)
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return contributor
    }
}
