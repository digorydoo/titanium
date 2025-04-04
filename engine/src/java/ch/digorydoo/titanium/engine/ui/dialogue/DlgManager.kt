package ch.digorydoo.titanium.engine.ui.dialogue

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.i18n.ITextId
import ch.digorydoo.titanium.engine.sound.EngineSampleId
import ch.digorydoo.titanium.engine.ui.SnackbarGel
import ch.digorydoo.titanium.engine.ui.choice.Choice
import ch.digorydoo.titanium.engine.ui.choice.TextChoice

@Suppress("unused", "MemberVisibilityCanBePrivate")
class DlgManager {
    private var activeDlg: Dialogue? = null
    val hasActiveDlg get() = activeDlg != null

    fun showMessage(textId: ITextId) {
        showMessage(App.i18n.getString(textId))
    }

    fun showMessage(msg: String) {
        val dlg = DlgBuilder.makeDialogue(questionText = msg)
        App.sound.play(EngineSampleId.MSG_SHOW)
        show(dlg)
    }

    fun showTwoWayDlg(
        question: ITextId,
        confirm: ITextId = EngineTextId.YES,
        deny: ITextId = EngineTextId.NO,
        playSoundOnOpen: Boolean = true,
        playSoundOnDismiss: Boolean = true,
        onConfirm: () -> Unit,
    ) {
        showTwoWayDlg(question, confirm, deny, playSoundOnOpen, playSoundOnDismiss, onConfirm, onDeny = null)
    }

    fun showTwoWayDlg(
        question: ITextId,
        confirm: ITextId = EngineTextId.YES,
        deny: ITextId = EngineTextId.NO,
        playSoundOnOpen: Boolean = true,
        playSoundOnDismiss: Boolean = true,
        onConfirm: () -> Unit,
        onDeny: (() -> Unit)?,
    ) {
        showChoices(
            listOf(
                TextChoice(confirm, onConfirm),
                TextChoice(deny) { onDeny?.invoke() }
            ),
            initHilitedIdx = 0,
            questionText = App.i18n.getString(question),
            lastItemIsDismiss = true,
            playSoundOnOpen = playSoundOnOpen,
            playSoundOnDismiss = playSoundOnDismiss,
        )
    }

    fun showTwoWayDlg(
        questionText: String,
        confirm: ITextId = EngineTextId.YES,
        deny: ITextId = EngineTextId.NO,
        playSoundOnOpen: Boolean = true,
        playSoundOnDismiss: Boolean = true,
        onConfirm: () -> Unit,
        onDeny: (() -> Unit)?,
    ) {
        showChoices(
            listOf(
                TextChoice(confirm, onConfirm),
                TextChoice(deny) { onDeny?.invoke() }
            ),
            initHilitedIdx = 0,
            questionText = questionText,
            lastItemIsDismiss = true,
            playSoundOnOpen = playSoundOnOpen,
            playSoundOnDismiss = playSoundOnDismiss,
        )
    }

    fun showChoices(
        choices: List<Choice>,
        initHilitedIdx: Int = 0, // can be set to -1 if none is to be initially highlighted
        questionText: String = "",
        lastItemIsDismiss: Boolean = false,
        playSoundOnOpen: Boolean = true,
        playSoundOnDismiss: Boolean = true,
    ) {
        val dlg = DlgBuilder.makeDialogue(
            choices = choices,
            initHilitedIdx = initHilitedIdx,
            questionText = questionText,
            lastItemIsDismiss = lastItemIsDismiss,
            playSoundOnDismiss = playSoundOnDismiss,
        )

        if (playSoundOnOpen) {
            App.sound.play(EngineSampleId.CHOICES_SHOW)
        }

        show(dlg)
    }

    private fun show(dlg: Dialogue) {
        dismiss()
        activeDlg = dlg
        dlg.onShow()
    }

    fun showSnackbar(textId: ITextId) =
        showSnackbar(App.i18n.getString(textId))

    fun showSnackbar(text: String) {
        SnackbarGel.create(text).also { App.content.add(it, LayerKind.UI_ABOVE_DLG) }
    }

    fun handle() {
        activeDlg?.handle()
    }

    fun dismiss(dlg: Dialogue) {
        if (activeDlg != dlg) {
            Log.error(TAG, "Cannot remove dlg $dlg, because another is active: $activeDlg")
            return
        }
        activeDlg = null
        dlg.onDismiss()
        System.gc() // now seems a good time
    }

    fun dismiss() {
        activeDlg?.let { dismiss(it) }
    }

    companion object {
        private val TAG = Log.Tag("DlgManager")
    }
}
