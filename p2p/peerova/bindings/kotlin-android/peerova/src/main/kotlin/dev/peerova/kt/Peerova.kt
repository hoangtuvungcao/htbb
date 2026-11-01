// Peerova: direct peer-to-peer networking, end-to-end encrypted. Kotlin API over the Java JNI binding
// (dev.peerova.Peerova + libpeerova_jni + libpeerova). JVM 8+ / Android API 21+.
//
//   PeerovaServer.start(mapOf("port" to 47000)).use { srv ->          // blocks up to ~3 s: not on the main thread
//       PeerovaClient.connect(srv.ticket).use { cli ->                // ticket or 6-character connect code
//           val ev = cli.poll(10_000) as PeerovaEvent.Connected
//           cli.send(ev.peer, "hello")
//       }
//   }
//
// Threading: send/poll/disconnect are thread-safe; run one poll loop per node; never close a node while
// another thread is inside poll/send on it (close() stops a startPolling loop itself).
// Errors: native failures throw PeerovaException (English message "E_NAME: ...", stable `code`); a closed node throws
// IllegalStateException.
package dev.peerova.kt

import dev.peerova.Peerova
import java.io.Closeable
import java.util.concurrent.Executor

/** Largest payload, in bytes, of an unreliable send; larger unreliable sends are rejected. */
const val MAX_UNRELIABLE_PAYLOAD: Int = Peerova.MAX_UNRELIABLE_PAYLOAD

/** @return the native library version string. */
fun peerovaVersion(): String = Peerova.version()

/** Thrown when a native call fails; message = English text "E_NAME: ...", `code` = stable code (`Peerova.E_*`). */
typealias PeerovaException = Peerova.PeerovaException

/**
 * A network event returned by [PeerovaNode.poll] or delivered to a [PeerovaNode.startPolling] listener.
 * Payloads are JVM copies and may be kept.
 */
sealed class PeerovaEvent {
    /** Peer (connection) id, unsigned 32-bit widened to Long. 0 for [ConnectFailed]. */
    abstract val peer: Long

    /** Server: a client connected. Client: connected to the server. */
    data class Connected(override val peer: Long) : PeerovaEvent()

    /** [peer] left; [reason] is English text "E_NAME: ...", [errorCode] its code (`Peerova.E_CLOSED`, ...). */
    data class Disconnected(override val peer: Long, val reason: String) : PeerovaEvent() {
        val errorCode: Int get() = Peerova.errorCodeOf(reason)
    }

    /**
     * A message from [peer] on [channel] (0..255). [reliable] tells how it was sent. [data] is the raw
     * payload; [text] decodes it as UTF-8.
     */
    class Message(override val peer: Long, val channel: Int, val reliable: Boolean, val data: ByteArray) : PeerovaEvent() {
        val text: String get() = String(data, Charsets.UTF_8)
        override fun toString() = "Message(peer=$peer, channel=$channel, reliable=$reliable, ${data.size} B)"
    }

    /** Client only: the connection attempt failed (timeout, wrong ticket/code, token mismatch, ...). */
    data class ConnectFailed(val reason: String) : PeerovaEvent() {
        override val peer: Long get() = 0
        /** Error code of [reason] (English text "E_TIMEOUT: ..."), e.g. `Peerova.E_TIMEOUT`. */
        val errorCode: Int get() = Peerova.errorCodeOf(reason)
    }

    companion object {
        internal fun from(e: Peerova.Event): PeerovaEvent = when (e.type) {
            Peerova.CONNECTED -> Connected(e.peer)
            Peerova.DISCONNECTED -> Disconnected(e.peer, e.text())
            Peerova.MESSAGE -> Message(e.peer, e.channel, e.reliable, e.data)
            Peerova.CONNECT_FAILED -> ConnectFailed(e.text())
            else -> throw PeerovaException("unknown event type ${e.type}")
        }
    }
}

/**
 * Common base of [PeerovaServer] and [PeerovaClient]: owns one native node. Close it (`use { }`) when done;
 * afterwards every method except [isClosed] / [close] throws [IllegalStateException].
 * [send], [poll], [disconnect] and [peerCount] may be called from any thread.
 */
abstract class PeerovaNode internal constructor(private val node: Peerova.Node) : Closeable {
    @Volatile private var loop: PollLoop? = null

    /**
     * Waits up to [timeoutMs] (0 = non-blocking) for an event; null if none arrived.
     * With a timeout this blocks the calling thread: never on the Android main thread.
     */
    fun poll(timeoutMs: Int = 0): PeerovaEvent? = node.poll(timeoutMs)?.let(PeerovaEvent::from)

    /** Lazily yields events, blocking up to [idleMs] for each; the sequence ends after [idleMs] without one. */
    fun events(idleMs: Int = 1000): Sequence<PeerovaEvent> = generateSequence { poll(idleMs) }

    /**
     * Sends [data] to [peer] on [channel] (0..255).
     * - `reliable = true`: delivered, ordered per channel, may be large — chat, inventory, RPCs, file chunks.
     * - `reliable = false`: one datagram, lowest latency, may be lost or reordered, at most
     *   [MAX_UNRELIABLE_PAYLOAD] bytes — positions, inputs, voice-like realtime state.
     * Channels never block each other.
     * @throws PeerovaException on failure (unknown peer, oversized unreliable payload, ...)
     * @throws IllegalArgumentException if [channel] is not 0..255
     */
    fun send(peer: Long, data: ByteArray, channel: Int = 0, reliable: Boolean = true) =
        node.send(peer, data, channel, reliable)

    /** Sends [text] as UTF-8; same rules as the ByteArray overload. */
    fun send(peer: Long, text: String, channel: Int = 0, reliable: Boolean = true) =
        node.send(peer, text.toByteArray(Charsets.UTF_8), channel, reliable)

    /** Like [send] but returns false instead of throwing on native failures (argument errors still throw). */
    fun trySend(peer: Long, data: ByteArray, channel: Int = 0, reliable: Boolean = true): Boolean =
        node.trySend(peer, data, 0, data.size, channel, reliable)

    /** Closes the connection to [peer]; a [PeerovaEvent.Disconnected] follows. Unknown ids are ignored. */
    fun disconnect(peer: Long) = node.disconnect(peer)

    /** Number of connected peers (client: 0 or 1). */
    val peerCount: Long get() = node.peerCount()

    /** True after [close]. */
    val isClosed: Boolean get() = node.isClosed

    /**
     * Starts a background thread that polls and calls [listener] for every event.
     * [executor] chooses where the listener runs (null = on the poll thread). On Android pass
     * `ContextCompat.getMainExecutor(context)` (or `Executor { Handler(Looper.getMainLooper()).post(it) }`)
     * to receive events on the UI thread. Only one loop per node; [close] stops it.
     * @param pollTimeoutMs how long each internal poll waits; bounds how quickly the loop notices a stop
     * @return a handle whose [PollLoop.close] stops the loop and leaves the node open
     * @throws IllegalStateException if a loop is already running on this node
     */
    @Synchronized
    fun startPolling(executor: Executor? = null, pollTimeoutMs: Int = 50, listener: (PeerovaEvent) -> Unit): PollLoop {
        check(loop == null) { "polling already started" }
        return PollLoop(this, executor, pollTimeoutMs, listener).also { loop = it }
    }

    /** Stops the poll loop (if any, waiting for it to exit) then frees the native node. Idempotent. */
    override fun close() {
        synchronized(this) { loop.also { loop = null } }?.stopAndJoin()
        node.close()
    }

    /** Handle of a running background poll loop. */
    class PollLoop internal constructor(
        private val owner: PeerovaNode,
        private val executor: Executor?,
        private val timeoutMs: Int,
        private val listener: (PeerovaEvent) -> Unit,
    ) : Closeable {
        @Volatile private var running = true
        private val thread = Thread({
            while (running) {
                val ev = try { owner.poll(timeoutMs) } catch (_: IllegalStateException) { break } ?: continue
                if (executor == null) listener(ev) else executor.execute { listener(ev) }
            }
        }, "peerova-poll").apply { isDaemon = true; start() }

        internal fun stopAndJoin() {
            running = false
            if (Thread.currentThread() !== thread) thread.join()
        }

        /** Stops polling (the node stays open). */
        override fun close() {
            synchronized(owner) { if (owner.loop === this) owner.loop = null }
            stopAndJoin()
        }
    }
}

/** A server node: accepts clients that connect with its [ticket] or [roomCode]. */
class PeerovaServer private constructor(private val srv: Peerova.Server) : PeerovaNode(srv) {
    /** Ticket (`pvt_...`) clients pass to [PeerovaClient.connect]. Stable for the same `key_file`; safe to share. */
    val ticket: String get() = srv.ticket()

    /**
     * The 6-character connect code (e.g. `"K7Q2XD"`) assigned by the Peerova website, a short alternative to
     * the [ticket]. Only available when the server was started with `"app"` and `"share"` in its config, and
     * only once the website has registered it (shortly after start): read it periodically until non-null.
     * Null if not (yet) available.
     */
    val roomCode: String? get() = srv.roomCode()

    companion object {
        /**
         * Starts a server; blocks up to ~3 s until the ticket is ready — call off the main thread
         * (or use `startAsync`).
         * @param configJson JSON config or null, e.g. `{"port":47000,"key_file":"server.key"}`
         * @throws PeerovaException if the server cannot start (port in use, bad config, invalid license, ...)
         */
        @JvmStatic
        fun start(configJson: String? = null): PeerovaServer = PeerovaServer(Peerova.Server.start(configJson))

        /**
         * Same as the JSON overload, with the config as a map (encoded to JSON). Keys: `port`, `key_file`,
         * `license`, `max_ccu`, `token`, `upnp`, `private`, `app`, `share` (a nested map). See the README.
         */
        @JvmStatic
        fun start(config: Map<String, Any?>): PeerovaServer = start(toJson(config))
    }
}

/** A client node: one connection to one server. */
class PeerovaClient private constructor(cli: Peerova.Client) : PeerovaNode(cli) {
    companion object {
        /**
         * Starts connecting and returns immediately when given a ticket. With a 6-character connect code it first
         * looks the code up on the Peerova website (an HTTPS request on the calling thread — call it off the
         * Android main thread). Then wait for [PeerovaEvent.Connected] or
         * [PeerovaEvent.ConnectFailed]. Connections are automatic: direct first, with an automatic encrypted
         * fallback route when networks block direct connections.
         * @param ticket the server's ticket (`pvt_...`) or its 6-character connect code
         * @param configJson JSON config or null, e.g. `{"token":"secret","timeout_ms":20000}`
         * @throws IllegalArgumentException if [ticket] is empty
         * @throws PeerovaException if the ticket is malformed or the connect code is unknown/unreachable
         */
        @JvmStatic
        fun connect(ticket: String, configJson: String? = null): PeerovaClient =
            PeerovaClient(Peerova.Client.connect(ticket, configJson))

        /** Same as the JSON overload, with the config as a map. Keys: `token`, `timeout_ms` (default 20000). */
        @JvmStatic
        fun connect(ticket: String, config: Map<String, Any?>): PeerovaClient = connect(ticket, toJson(config))
    }
}

/** Minimal JSON encoder for config maps (String, Number, Boolean, null, List, Map). */
internal fun toJson(v: Any?): String = when (v) {
    null -> "null"
    is String -> buildString {
        append('"')
        for (c in v) when {
            c == '"' -> append("\\\"")
            c == '\\' -> append("\\\\")
            c < ' ' -> append(String.format("\\u%04x", c.code))
            else -> append(c)
        }
        append('"')
    }
    is Boolean, is Int, is Long, is Short, is Byte -> v.toString()
    is Number -> v.toDouble().let { if (it.isFinite()) v.toString() else throw IllegalArgumentException("non-finite number") }
    is Map<*, *> -> v.entries.joinToString(",", "{", "}") { (k, x) -> toJson(k.toString()) + ":" + toJson(x) }
    is Iterable<*> -> v.joinToString(",", "[", "]") { toJson(it) }
    is Array<*> -> v.joinToString(",", "[", "]") { toJson(it) }
    else -> throw IllegalArgumentException("unsupported config value: ${v::class}")
}
