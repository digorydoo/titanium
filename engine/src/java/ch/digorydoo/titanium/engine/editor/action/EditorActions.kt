package ch.digorydoo.titanium.engine.editor.action

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.camera.CameraProps.Mode
import ch.digorydoo.titanium.engine.editor.BrickSelection
import ch.digorydoo.titanium.engine.editor.EditorHUD
import ch.digorydoo.titanium.engine.editor.EditorState
import ch.digorydoo.titanium.engine.editor.HeightMapSelection
import ch.digorydoo.titanium.engine.editor.UndoStack
import ch.digorydoo.titanium.engine.editor.cursor.CursorGelHolder
import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.heightmap.HeightMap
import ch.digorydoo.titanium.engine.heightmap.HeightMapGel
import ch.digorydoo.titanium.engine.heightmap.HeightMapSpawnPt
import ch.digorydoo.titanium.engine.scene.Lighting

internal class EditorActions(
    cursor: CursorGelHolder,
    hud: EditorHUD,
    brickSelection: BrickSelection,
    heightMapSelection: HeightMapSelection,
    state: EditorState,
    undoStack: UndoStack,
) {
    private val bricks = EditBrickActions(hud, brickSelection, state, undoStack)
    private val general = EditGeneralActions(cursor, hud, brickSelection)
    private val spawnPts = EditSpawnPtActions(brickSelection)
    private val heightMaps = EditHeightMapActions(this, brickSelection, cursor, heightMapSelection, state)

    fun setActiveShape(shape: BrickShape) {
        bricks.setActiveShape(shape)
    }

    fun setActiveMaterial(mat: BrickMaterial) {
        bricks.setActiveMaterial(mat)
    }

    fun setShapeAndMaterialOfSelectedBricks() {
        bricks.setShapeAndMaterialOfSelectedBricks()
    }

    fun removeSelectedBricks() {
        bricks.removeSelectedBricks()
    }

    fun applyShapeToSelectedBricks() {
        bricks.applyShapeToSelectedBricks()
    }

    fun applyMaterialToSelectedBricks() {
        bricks.applyMaterialToSelectedBricks()
    }

    fun rotateSelection() {
        bricks.rotateSelection()
    }

    fun pickShapeMaterial() {
        bricks.pickShapeMaterial()
    }

    fun setCameraMode(mode: Mode) {
        general.setCameraMode(mode)
    }

    fun setCameraModeTopDown() {
        general.setCameraModeTopDown()
    }

    fun switchCameraTarget(backwards: Boolean) {
        general.switchCameraTarget(backwards)
    }

    fun printInfo() {
        general.printInfo()
    }

    fun saveToFile() {
        general.saveToFile()
    }

    fun movePlayerToCursorPos() {
        general.movePlayerToCursorPos()
    }

    fun setLighting(lgt: Lighting?) {
        general.setLighting(lgt)
    }

    fun setStoryTime(hours: Int, minutes: Int) {
        general.setStoryTime(hours, minutes)
    }

    fun jumpToPrevSpawnPt() {
        spawnPts.jumpToPrevSpawnPt()
    }

    fun jumpToNextSpawnPt() {
        spawnPts.jumpToNextSpawnPt()
    }

    fun addNewSpawnPt(spawnObjType: String, rotation: Float = 0.0f) {
        spawnPts.addNewSpawnPt(spawnObjType, rotation)
    }

    fun addAnotherSpawnPt() {
        spawnPts.addAnotherSpawnPt()
    }

    fun jumpToSpawnPt(pt: SpawnPt) {
        spawnPts.jumpToSpawnPt(pt)
    }

    fun deleteSpawnPt(pt: SpawnPt) {
        spawnPts.deleteSpawnPt(pt)
    }

    fun moveSpawnPt(pt: SpawnPt, newPos: Point3f) {
        spawnPts.moveSpawnPt(pt, newPos)
    }

    fun spawnPtChanged(pt: SpawnPt) {
        spawnPts.spawnPtChanged(pt)
    }

    fun addNewHeightMap(xsize: Float, ysize: Float, numSamplesX: Int, numSamplesY: Int) {
        heightMaps.addNewHeightMap(xsize, ysize, numSamplesX, numSamplesY)
    }

    fun didAddNewHeightMap(spawnPt: HeightMapSpawnPt) {
        spawnPts.didAddNewHeightMap(spawnPt)
    }

    fun setHeightMapEditMode(heightMap: HeightMap, spawnPt: HeightMapSpawnPt) {
        heightMaps.setHeightMapEditMode(heightMap, spawnPt)
    }

    fun setBricksEditMode() {
        heightMaps.setBricksEditMode()
    }

    fun modifyHeightOfSelectedSamples(deltaZ: Float) {
        heightMaps.modifyHeightOfSelectedSamples(deltaZ)
    }

    fun resampleHeightMap(heightMap: HeightMap, gel: HeightMapGel, numSamplesX: Int, numSamplesY: Int) {
        heightMaps.resampleHeightMap(heightMap, gel, numSamplesX, numSamplesY)
    }
}
