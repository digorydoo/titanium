package ch.digorydoo.titanium.engine.mesh

import ch.digorydoo.titanium.engine.file.MeshFileReader
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.ref.SoftReference

class MeshManager {
    // A SoftReference is like a WeakReference, but stays in memory as long as the JVM does not run out of memory.
    private val cache = mutableMapOf<String, SoftReference<Mesh>>()
    private val mutex = Mutex()

    suspend fun getOrLoadMeshAsync(filename: String): Mesh {
        return mutex.withLock {
            cache[filename]?.get() ?: MeshFileReader.readFile(filename).also { cache[filename] = SoftReference(it) }
        }
    }
}
