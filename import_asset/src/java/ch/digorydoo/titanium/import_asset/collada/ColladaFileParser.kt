package ch.digorydoo.titanium.import_asset.collada

import ch.digorydoo.titanium.import_asset.collada.data.ColladaData
import ch.digorydoo.titanium.import_asset.collada.reader.RootReader
import oracle.xml.parser.v2.DOMParser
import java.io.InputStream

class ColladaFileParser {
    fun parse(stream: InputStream): ColladaData {
        val parser = DOMParser()
        parser.setErrorStream(System.err)
        parser.setValidationMode(DOMParser.NONVALIDATING)
        parser.showWarnings(true)
        parser.parse(stream)
        return RootReader(parser.document.documentElement).read()
    }
}
