package io.github.digorydoo.titanium.engine.intermission

import ch.digorydoo.kutils.utils.Log
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.intermission.IntermissionManager.IntermissionAlreadyRunningException
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgDef
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class Intermission(private val scope: CoroutineScope) {
    class IntermissionAlreadyPausedException: Exception("An effect has already been scheduled")
    class CancellablePartAlreadyRunningException: Exception("A cancellable part is already running")

    internal var waitingFor: (() -> Unit)? = null; private set

    private var mainJob: Job? = null
    private var cancellable: Job? = null
    internal var hasCancellable = false; private set // extra flag needed, see below

    internal fun begin(lambda: suspend Intermission.() -> Unit): Job {
        if (mainJob != null) throw IntermissionAlreadyRunningException()
        return scope
            .launch {
                // Since our scope is Unconfined, launch uses the current thread. We better make sure, because GL
                // operations must always happen on the same thread.
                require(Thread.currentThread().id == App.mainThreadId)
                lambda()
            }
            .also { mainJob = it }
    }

    suspend fun cancellable(lambda: suspend Intermission.() -> Unit) {
        if (hasCancellable || cancellable != null) throw CancellablePartAlreadyRunningException()

        Log.info(TAG, "About to enter cancellable section")

        // We need an extra flag, because launch executes immediately, thus cancellable won't be set until the
        // coroutine either pauses or completes!
        hasCancellable = true

        val job = scope.launch {
            Log.info(TAG, "Inside cancellable section")
            require(Thread.currentThread().id == App.mainThreadId)
            lambda()
            Log.info(TAG, "Cancellable section about to end")
        }

        cancellable = job
        hasCancellable = true

        job.invokeOnCompletion { exc ->
            cancellable = null
            hasCancellable = false

            if (exc != null) {
                Log.error(TAG, "Cancellable section crashed: $exc")
            } else {
                Log.info(TAG, "Cancellable section ended")
            }
        }

        // The nested Job is not immediately launched, so we need to join to ensure correct order of execution.
        job.join()
    }

    suspend fun <Id> showDlg(lambda: DlgDef<Id>.() -> Unit): Id? = suspendCancellableCoroutine { cont ->
        val def = DlgDef.build(lambda)
        val origOnClose = def.onClose

        def.onClose = { selectedItem ->
            origOnClose?.invoke(selectedItem)
            cont.resume(selectedItem?.id)
        }

        App.dlg.showDlg(def)
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
