package io.github.digorydoo.titanium.engine.editor

import ch.digorydoo.kutils.string.toDelimited
import io.github.digorydoo.titanium.engine.behaviours.Align
import io.github.digorydoo.titanium.engine.behaviours.Align.Anchor
import io.github.digorydoo.titanium.engine.camera.CameraDirectingMode
import io.github.digorydoo.titanium.engine.camera.CameraInputMode
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.editor.EditorState.EditMode
import io.github.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import io.github.digorydoo.titanium.engine.gel.TextGel
import io.github.digorydoo.titanium.engine.i18n.EngineTextId

internal class EditorHUD(private val state: EditorState) {
    private var editModeGel: TextGel? = null
    private var brickShapeGel: TextGel? = null
    private var brickMaterialGel: TextGel? = null
    private var cameraModeGel: TextGel? = null
    private var statsGel: TextGel? = null

    init {
        state.addObserver { stateChanged() }
    }

    private fun stateChanged() {
        editModeGel?.text = state.editMode.toString()
        brickShapeGel?.text = state.shape.displayText
        brickMaterialGel?.text = state.material.displayText

        when (state.editMode) {
            EditMode.BRICKS -> {
                brickShapeGel?.show()
                brickMaterialGel?.show()
            }
            EditMode.HEIGHT_MAP -> {
                brickShapeGel?.hide()
                brickMaterialGel?.hide()
            }
        }
    }

    fun cameraModeChanged() {
        cameraModeGel?.text = App.camera.directingMode.displayText
    }

    fun show() {
        require(editModeGel == null)
        require(brickShapeGel == null)
        require(brickMaterialGel == null)
        require(cameraModeGel == null)
        require(statsGel == null)

        editModeGel = TextGel(
            state.editMode.toString(),
            alignment = Align.Alignment(anchor = Anchor.TOP_LEFT, marginLeft = 24, marginTop = 16)
        ).also { it.onCreate(LayerKind.UI_BELOW_DLG) }

        brickShapeGel = TextGel(
            state.shape.displayText,
            alignment = Align.Alignment(anchor = Anchor.TOP_LEFT, marginLeft = 216, marginTop = 16)
        ).also { it.onCreate(LayerKind.UI_BELOW_DLG) }

        brickMaterialGel = TextGel(
            state.material.displayText,
            alignment = Align.Alignment(anchor = Anchor.TOP_LEFT, marginLeft = 480, marginTop = 16)
        ).also { it.onCreate(LayerKind.UI_BELOW_DLG) }

        cameraModeGel = TextGel(
            App.camera.directingMode.displayText,
            alignment = Align.Alignment(anchor = Anchor.TOP_CENTRE, marginTop = 16)
        ).also { it.onCreate(LayerKind.UI_BELOW_DLG) }

        statsGel = TextGel(
            "",
            alignment = Align.Alignment(anchor = Anchor.BOTTOM_LEFT, marginLeft = 24, marginBottom = 16)
        ).also { it.onCreate(LayerKind.UI_BELOW_DLG) }

        updateStats()
        App.camera.inputMode = CameraInputMode.FULLY_CONTROLLABLE
    }

    fun hide() {
        editModeGel?.setZombie()
        brickShapeGel?.setZombie()
        brickMaterialGel?.setZombie()
        cameraModeGel?.setZombie()
        statsGel?.setZombie()

        editModeGel = null
        brickShapeGel = null
        brickMaterialGel = null
        cameraModeGel = null
        statsGel = null

        App.camera.directingMode = CameraDirectingMode.SMART
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
        App.process.runAtEndOfFrame {
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
