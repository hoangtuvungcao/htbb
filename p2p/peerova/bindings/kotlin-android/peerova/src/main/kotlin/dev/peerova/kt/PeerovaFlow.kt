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
 * Cold flow of this node's events, polled on [Dispatchers.IO]; events are emitted in the collector's context.
 * Collect it once per node (events are consumed, so two collectors would split them) and do not combine it
 * with [PeerovaNode.startPolling]. Cancel the collecting coroutine *before* calling [PeerovaNode.close].
 * @param pollTimeoutMs how long each internal poll waits; bounds how quickly cancellation is noticed
 *
 *     lifecycleScope.launch { node.eventFlow().collect { ev -> ... } }   // collected on Main
 */
fun PeerovaNode.eventFlow(pollTimeoutMs: Int = 50): Flow<PeerovaEvent> = flow {
    while (currentCoroutineContext().isActive && !isClosed) {
        poll(pollTimeoutMs)?.let { emit(it) }
    }
}.flowOn(Dispatchers.IO)

/**
 * Starts a server on [Dispatchers.IO] (start blocks up to ~3 s), so it is safe to call from the main thread.
 * @throws PeerovaException if the server cannot start
 */
suspend fun PeerovaServer.Companion.startAsync(config: Map<String, Any?>): PeerovaServer =
    kotlinx.coroutines.withContext(Dispatchers.IO) { PeerovaServer.start(config) }
