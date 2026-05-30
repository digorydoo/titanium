package io.github.digorydoo.titanium.import_asset.collada.reader

import io.github.digorydoo.titanium.import_asset.XMLTreeReader
import io.github.digorydoo.titanium.import_asset.collada.data.AssetUnit
import org.w3c.dom.Element

class AssetUnitReader(node: Element): XMLTreeReader(node) {
    fun read(): AssetUnit {
        val assetUnit = AssetUnit()

        checkAttributes(arrayOf("name", "meter"))
        requireChildless()

        assetUnit.name = getMandatoryAttr("name")
        assetUnit.meter = getMandatoryAttr("meter")

        return assetUnit
    }
}
