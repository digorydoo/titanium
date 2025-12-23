package ch.digorydoo.titanium.engine.editor

import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.heightmap.HeightMap
import ch.digorydoo.titanium.engine.heightmap.HeightMapSpawnPt

internal class EditorState {
    enum class EditMode { BRICKS, HEIGHT_MAP }

    var editMode = EditMode.BRICKS; private set
    var shape = BrickShape.BASIC_BLOCK; private set
    var material = BrickMaterial.GREY_CONCRETE; private set
    var heightMap: HeightMap? = null; private set
    var heightMapSpawnPt: HeightMapSpawnPt? = null; private set

    private var observers = mutableListOf<() -> Unit>()

    fun addObserver(onChange: () -> Unit) {
        observers.add(onChange)
    }

    private fun notifyObservers() {
        observers.forEach { it() }
    }

    fun setBricksEditMode() {
        editMode = EditMode.BRICKS
        heightMap = null
        heightMapSpawnPt = null
        notifyObservers()
    }

    fun setHeightMapEditMode(theHeightMap: HeightMap, spawnPt: HeightMapSpawnPt) {
        editMode = EditMode.HEIGHT_MAP
        heightMap = theHeightMap
        heightMapSpawnPt = spawnPt
        notifyObservers()
    }

    fun setShape(s: BrickShape) {
        shape = s
        notifyObservers()
    }

    fun setMaterial(m: BrickMaterial) {
        material = m
        notifyObservers()
    }
}
