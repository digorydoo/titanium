package io.github.digorydoo.titanium.engine.ui.dialogue

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import io.github.digorydoo.titanium.engine.i18n.ITextId
import io.github.digorydoo.titanium.engine.sound.EngineSampleId
import io.github.digorydoo.titanium.engine.sound.SoundManager.SampleId
import io.github.digorydoo.titanium.engine.ui.SnackbarGel

class DlgManager {
    private var activeDlg: Dialogue? = null
    var isInDlgMode = false; private set
    private var soundOnLeavingDlgMode: SampleId? = null
    private var leaveDlgModeOnNextFrame = false

    /**
     * Installs the given dialogue as the new active dialogue. This method should be called from Intermission only.
     * Consumers should open all dialogues through App.intermission.begin { } to make it clear that the dialogue will
     * be handled asynchronously.
     */
    internal fun showDlg(def: DlgDef) {
        require(activeDlg == null) { "A dialogue is already active. Are you using an Intermission as you should?" }

        val dlg = DlgFactory.create(def)

        // If isInDlgMode is still true, this must be a followup dialogue. Don't leave dlg mode just yet!
        leaveDlgModeOnNextFrame = false

        if (!isInDlgMode) {
            isInDlgMode = true

            val soundOnOpen = when {
                App.gameMenu.isShown -> null
                def.suppressSoundsOnShowAndDismiss -> null
                def.items.isEmpty() -> EngineSampleId.MSG_SHOW
                else -> EngineSampleId.CHOICES_SHOW
            }

            soundOnOpen?.let { App.sound.play(it) }

            soundOnLeavingDlgMode = when {
                App.gameMenu.isShown -> null
                def.suppressSoundsOnShowAndDismiss -> null
                def.items.isEmpty() -> EngineSampleId.MSG_DISMISS
                else -> EngineSampleId.CHOICES_DISMISS
            }
        }

        activeDlg = dlg
        dlg.onShow()
    }

    fun showSnackbar(textId: ITextId) =
        showSnackbar(App.i18n.getString(textId))

    fun showSnackbar(text: String) {
        SnackbarGel.create(text).also { it.onCreate(LayerKind.UI_ABOVE_DLG) }
    }

    fun handle() {
        val dlg = activeDlg

        if (dlg != null) {
            if (!isInDlgMode) {
                Log.error(TAG, "A dialogue is active, but we're not in dlg mode!")
                isInDlgMode = true
            }
            if (leaveDlgModeOnNextFrame) {
                Log.error(TAG, "leaveDlgModeOnNextFrame must stay false while a dialogue is active!")
                leaveDlgModeOnNextFrame = false
            }
            dlg.handle()
        } else if (isInDlgMode) {
            // There is no dialogue, but we're still in dialogue mode. We need to wait for one frame, because the
            // item's click handler may open a followup dialogue.
            if (leaveDlgModeOnNextFrame) {
                isInDlgMode = false
                leaveDlgModeOnNextFrame = false
                soundOnLeavingDlgMode?.let { App.sound.play(it) }
                soundOnLeavingDlgMode = null
            } else {
                leaveDlgModeOnNextFrame = true
            }
        }
    }

    /**
     * Called by Dialogue when it was closed.
     */
    internal fun onClose(dlg: Dialogue) {
        if (activeDlg != dlg) {
            Log.error(TAG, "Cannot remove dlg $dlg, because another is active: $activeDlg")
            return
        }
        activeDlg = null
        System.gc() // now seems a good time
    }

    /**
     * If the active dialogue was a message, it will be silently closed. If it was a dialogue with items, the
     * Intermission waiting for its response will throw DlgCancelledException. If there is no active dialogue,
     * this method does nothing.
     */
    fun cancelActiveDlg() {
        activeDlg?.abort()
    }

    companion object {
        private val TAG = Log.Tag("DlgManager")
    }
}
