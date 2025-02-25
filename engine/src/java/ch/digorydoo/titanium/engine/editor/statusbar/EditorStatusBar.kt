package ch.digorydoo.titanium.engine.editor.statusbar

import ch.digorydoo.kutils.string.toDelimited
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.behaviours.Align.Anchor
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.camera.CameraProps.Mode
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.gel.TextGel
import ch.digorydoo.titanium.engine.i18n.EngineTextId

class EditorStatusBar {
    private var brickShapeGel: TextGel? = null
    private var brickMaterialGel: TextGel? = null
    private var cameraModeGel: TextGel? = null
    private var statsGel: TextGel? = null

    var shape = BrickShape.BASIC_BLOCK; private set
    var material = BrickMaterial.GREY_CONCRETE; private set

    fun setActiveShape(s: BrickShape) {
        shape = s
        brickShapeGel?.text = s.displayText
    }

    fun setActiveMaterial(m: BrickMaterial) {
        material = m
        brickMaterialGel?.text = m.displayText
    }

    fun cameraModeChanged() {
        cameraModeGel?.text = App.camera.mode.displayText
    }

    fun show() {
        require(brickShapeGel == null)
        require(brickMaterialGel == null)
        require(cameraModeGel == null)
        require(statsGel == null)

        brickShapeGel = TextGel(
            shape.displayText,
            alignment = Align.Alignment(anchor = Anchor.TOP_LEFT, marginLeft = 24, marginTop = 16)
        ).also { App.content.add(it, LayerKind.UI_BELOW_DLG) }

        brickMaterialGel = TextGel(
            material.displayText,
            alignment = Align.Alignment(anchor = Anchor.TOP_LEFT, marginLeft = 216, marginTop = 16)
        ).also { App.content.add(it, LayerKind.UI_BELOW_DLG) }

        cameraModeGel = TextGel(
            App.camera.mode.displayText,
            alignment = Align.Alignment(anchor = Anchor.TOP_CENTRE, marginTop = 16)
        ).also { App.content.add(it, LayerKind.UI_BELOW_DLG) }

        statsGel = TextGel(
            "",
            alignment = Align.Alignment(anchor = Anchor.BOTTOM_LEFT, marginLeft = 24, marginBottom = 16)
        ).also { App.content.add(it, LayerKind.UI_BELOW_DLG) }

        updateStats()
    }

    fun hide() {
        brickShapeGel?.setZombie()
        brickMaterialGel?.setZombie()
        cameraModeGel?.setZombie()
        statsGel?.setZombie()

        brickShapeGel = null
        brickMaterialGel = null
        cameraModeGel = null
        statsGel = null

        App.camera.mode = Mode.SMART
    }

    fun updateStats() {
        val p = App.bricks.totalNumPositions.toDelimited()
        val n = App.bricks.totalNumNormals.toDelimited()
        val tc = App.bricks.totalNumTexCoords.toDelimited()
        val sv = App.bricks.numSubVolumes.toDelimited()
        statsGel?.text = "P: $p, N: $n, TC: $tc, SV: $sv"
    }

    fun didSave() {
        // The first frame after save may take a bit longer for some reason, so we postpone the snackbar a bit.
        App.runAtEndOfFrame {
            brickMaterialGel?.rotate()
            App.dlg.showSnackbar(EngineTextId.EDITOR_FILE_SAVED)
        }
    }

    fun didFailToPaste() {
        brickMaterialGel?.shake()
        App.dlg.showSnackbar(EngineTextId.EDITOR_CANNOT_PASTE)
    }

    fun didFailToUndo() {
        brickMaterialGel?.shake()
        App.dlg.showSnackbar(EngineTextId.EDITOR_CANNOT_UNDO)
    }

    fun didFailToRedo() {
        brickMaterialGel?.shake()
        App.dlg.showSnackbar(EngineTextId.EDITOR_CANNOT_REDO)
    }
}
