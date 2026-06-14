package io.github.digorydoo.titanium.engine.intermission

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.engine.camera.CameraInputMode
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.core.MainThreadDispatcher
import io.github.digorydoo.titanium.engine.i18n.ITextId
import io.github.digorydoo.titanium.engine.intermission.IntermissionManager.IntermissionAlreadyRunningException
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgDef
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgItemDef
import io.github.digorydoo.titanium.engine.ui.dialogue.SuspendingOnSelect
import kotlinx.coroutines.*
import kotlin.coroutines.resume

class Intermission internal constructor() {
    class DlgCancelledException: Exception("The dialogue was cancelled")
    class IntermissionAlreadyPausedException: Exception("An effect has already been scheduled")
    class CancellablePartAlreadyRunningException: Exception("A cancellable part is already running")

    internal var waitingFor: (() -> Unit)? = null; private set

    private val scope = CoroutineScope(MainThreadDispatcher() + SupervisorJob())
    private var mainJob: Job? = null
    private var cancellable: Job? = null
    internal var canCancel = false; private set // extra flag needed, see below

    internal fun begin(lambda: suspend Intermission.() -> Unit): Job {
        App.process.requireMainThread()
        if (mainJob != null) throw IntermissionAlreadyRunningException()
        return scope
            .launch {
                // The scope must have our MainThreadDispatcher, so this should be the main thread.
                App.process.requireMainThread()
                lambda()
                // Even if the lambda involved a different thread by calling withContext(), we should be back by now.
                App.process.requireMainThread()
            }
            .also { mainJob = it }
    }

    suspend fun cancellable(lambda: suspend Intermission.() -> Unit) {
        App.process.requireMainThread()
        if (canCancel || cancellable != null) throw CancellablePartAlreadyRunningException()

        Log.info(TAG, "About to enter cancellable section")

        // We need an extra flag, because launch executes immediately, thus cancellable won't be set until the
        // coroutine either pauses or completes!
        canCancel = true

        // Camera input mode stays the same when the intermission starts. This is important for conversations; players
        // may want to adjust the camera during a dialogue. However, a cancellable section indicates a cutscene, so we
        // set inputMode to OFF during a cutscene, and restore it once we're out again. (If the cutscene changes other
        // values of the camera, it's the intermission's responsibility to restore them later. We can't assume those
        // settings should be the same after a cutscene.)
        val camera = App.camera
        val origInputMode = camera.inputMode

        val job = scope.launch {
            Log.info(TAG, "Inside cancellable section")
            App.process.requireMainThread()
            camera.inputMode = CameraInputMode.OFF

            sleep(0f) // skip one frame to let GameHUD update its internal state
            lambda()

            App.process.requireMainThread()
            Log.info(TAG, "Cancellable section about to end")
        }

        cancellable = job
        canCancel = true

        job.invokeOnCompletion { exc ->
            App.process.requireMainThread()
            cancellable = null
            canCancel = false
            App.dlg.cancelActiveDlg() // in case there was an active dlg, its paused coroutine is gone
            camera.inputMode = origInputMode

            when (exc) {
                null -> Log.info(TAG, "Cancellable section ended")
                is CancellationException -> Log.info(TAG, "Job was cancelled")
                else -> Log.error(TAG, "Cancellable section crashed: $exc")
            }
        }

        // The nested Job is not immediately launched, so we need to join to ensure correct order of execution.
        job.join()
    }

    fun cancelIfCancellable() {
        if (canCancel) {
            // Note that the intermission may continue after the cancellable section, so the following line may not
            // immediately return.
            cancellable?.cancel()
        }
    }

    @Suppress("unused")
    suspend fun showMessage(textId: ITextId) {
        val def = DlgDef.build { this.textId = textId }
        showDlg(def)
    }

    suspend fun showMessage(text: String) {
        val def = DlgDef.build { this.text = text }
        showDlg(def)
    }

    suspend fun showDlg(lambda: DlgDef.() -> Unit): DlgItemDef {
        val def = DlgDef.build(lambda)

        if (def.items.isEmpty()) {
            // We cannot allow this, because we must return an item.
            throw Exception("Dialogue with no items must be called through showMessage")
        }

        val selected = showDlg(def) ?: throw DlgCancelledException()
        return selected
    }

    /**
     * @return null if dialogue hasn't got any items (pure message);
     *   null if dialogue with items was cancelled through App.dlg.cancelActiveDlg();
     *   otherwise the definition of the item that was selected by the user.
     */
    private suspend fun showDlg(def: DlgDef): DlgItemDef? {
        val selectedItem = suspendCancellableCoroutine { cont ->
            val origOnClose = def.onClose

            def.onClose = { selectedItem ->
                origOnClose?.invoke(selectedItem)
                cont.resume(selectedItem)
            }

            App.dlg.showDlg(def)
        }

        if (selectedItem is SuspendingOnSelect) {
            selectedItem.onSelect?.invoke(this)
        }

        return selectedItem
    }

    suspend fun sleep(seconds: Float) {
        val time = App.time
        suspendCancellableCoroutine { cont ->
            val wakeupTime = time.sessionTime + seconds
            waitFor(cont) { time.sessionTime >= wakeupTime }
        }
    }

    suspend fun waitFor(until: () -> Boolean) {
        suspendCancellableCoroutine { cont ->
            waitFor(cont, until)
        }
    }

    private fun waitFor(cont: CancellableContinuation<Boolean>, predicate: () -> Boolean) {
        if (waitingFor != null) throw IntermissionAlreadyPausedException()
        Log.info(TAG, "Waiting for event")
        waitingFor = {
            try {
                if (predicate()) {
                    Log.info(TAG, "Event arrived, continuing")
                    waitingFor = null
                    cont.resume(true)
                }
            } catch (e: Exception) {
                Log.error(TAG, "Intermission crashed while waiting for continuation: $e")
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("Intermission")
    }
}
