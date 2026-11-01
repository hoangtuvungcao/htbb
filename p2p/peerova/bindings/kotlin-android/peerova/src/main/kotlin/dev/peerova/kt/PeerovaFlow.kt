// Optional kotlinx-coroutines integration (needs org.jetbrains.kotlinx:kotlinx-coroutines-core 1.6+).
// Delete this file if your app does not use coroutines.
package dev.peerova.kt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

/**
 * Cold flow of this node's events, polled on [Dispatchers.IO]. Collect it once per node
 * (events are consumed). Cancel the collecting coroutine *before* calling [PeerovaNode.close].
 *
 *     lifecycleScope.launch { node.eventFlow().collect { ev -> ... } }   // collected on Main
 */
fun PeerovaNode.eventFlow(pollTimeoutMs: Int = 50): Flow<PeerovaEvent> = flow {
    while (currentCoroutineContext().isActive && !isClosed) {
        poll(pollTimeoutMs)?.let { emit(it) }
    }
}.flowOn(Dispatchers.IO)

/** Starts a server on [Dispatchers.IO] (start blocks up to ~3 s). */
suspend fun PeerovaServer.Companion.startAsync(config: Map<String, Any?>): PeerovaServer =
    kotlinx.coroutines.withContext(Dispatchers.IO) { PeerovaServer.start(config) }
