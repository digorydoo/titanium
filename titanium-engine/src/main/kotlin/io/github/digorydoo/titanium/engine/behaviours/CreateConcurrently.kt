package io.github.digorydoo.titanium.engine.behaviours

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.Behaviour
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import kotlinx.coroutines.Job

class CreateConcurrently(private val gel: GraphicElement, private val delegate: Delegate): Behaviour {
    interface Delegate {
        /**
         * Called inside a coroutine. Do not call any GL functions from here. Avoid modifying the gel; apply the
         * changes in onJobDone instead!
         */
        suspend fun onJobStart()

        /**
         * Called in the main thread after onDoConcurrently has finished.
         */
        fun onJobDone()
    }

    private var job: Job? = null
    val isPending get() = job != null

    internal fun start(onDone: () -> Unit) {
        val sceneTicket = App.content.sceneTicket

        require(job == null)
        job = App.process.runAsyncIO {
            var caught: Exception? = null

            try {
                delegate.onJobStart()
            } catch (e: Exception) {
                caught = e
            }

            App.process.runAtEndOfFrame {
                // We're back in the main thread.
                job = null // important, because GelLayer indirectly calls our isPending as a sanity check

                if (caught != null) {
                    Log.error(TAG, "Gel $gel crashed in CreateConcurrently: ${caught.message}\n$caught}")
                    gel.setZombie()
                } else if (gel.zombie) {
                    // This is not necessarily a bug since it just means someone else must have set it to zombie.
                    Log.info(TAG, "Not adding $gel to layer since it went zombie while loading concurrently")
                } else if (sceneTicket != App.content.sceneTicket) {
                    Log.info(TAG, "Not adding $gel to layer since the scene has changed")
                    gel.setZombie()
                } else {
                    delegate.onJobDone()
                    onDone() // tell the caller (GraphicElement) it can now add this gel to the layer
                }
            }
        }
    }

    fun cancel() = job?.cancel()

    companion object {
        private val TAG = Log.Tag("CreateConcurrently")
    }
}
