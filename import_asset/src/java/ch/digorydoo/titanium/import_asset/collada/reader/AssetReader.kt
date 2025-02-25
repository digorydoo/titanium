package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Asset
import org.w3c.dom.Element

class AssetReader(node: Element): XMLTreeReader(node) {
    fun read(): Asset {
        val asset = Asset()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "contributor" -> asset.contributor = ContributorReader(child).read()
                "created" -> asset.created += getChildValue(child)
                "modified" -> asset.modified += getChildValue(child)
                "unit" -> {
                    val assetUnit = AssetUnitReader(child).read()
                    require(assetUnit.name == "meter") { "Unexpected assetUnit.name: ${assetUnit.name}" }
                    require(assetUnit.meter == "1") { "Unexpected assetUnit.meter: ${assetUnit.meter}" }
                }
                "up_axis" -> requireChildValue(child, "Z_UP")
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return asset
    }
}
