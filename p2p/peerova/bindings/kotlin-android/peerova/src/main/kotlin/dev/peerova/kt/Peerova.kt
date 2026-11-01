// peerova — direct P2P networking (no relay, E2E encrypted). Kotlin API over the Java JNI binding
// (bindings/java: dev.peerova.Peerova + libpeerova_jni + libpeerova). JVM 8+ / Android API 21+.
//
//   PeerovaServer.start(mapOf("port" to 47000)).use { srv ->
//       PeerovaClient.connect(srv.ticket).use { cli ->
//           val ev = cli.poll(10_000) as PeerovaEvent.Connected
//           cli.send(ev.peer, "hello")
//       }
//   }
package dev.peerova.kt

import dev.peerova.Peerova
import java.io.Closeable
import java.util.concurrent.Executor

/** Max payload of an unreliable datagram. */
const val MAX_UNRELIABLE_PAYLOAD: Int = Peerova.MAX_UNRELIABLE_PAYLOAD

/** Native library version. */
fun peerovaVersion(): String = Peerova.version()

typealias PeerovaException = Peerova.PeerovaException

/** A network event. Payloads are JVM copies (the C buffer is valid only until the next poll). */
sealed class PeerovaEvent {
    /** Peer (connection) id, unsigned 32-bit widened to Long. 0 for [ConnectFailed]. */
    abstract val peer: Long

    /** Server: a client connected. Client: connected to the server. */
    data class Connected(override val peer: Long) : PeerovaEvent()

    data class Disconnected(override val peer: Long, val reason: String) : PeerovaEvent()

    class Message(override val peer: Long, val channel: Int, val reliable: Boolean, val data: ByteArray) : PeerovaEvent() {
        val text: String get() = String(data, Charsets.UTF_8)
        override fun toString() = "Message(peer=$peer, channel=$channel, reliable=$reliable, ${data.size} B)"
    }

    data class ConnectFailed(val reason: String) : PeerovaEvent() {
        override val peer: Long get() = 0
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

/** Common base of [PeerovaServer] and [PeerovaClient]. Thread-safe for send/poll. */
abstract class PeerovaNode internal constructor(private val node: Peerova.Node) : Closeable {
    @Volatile private var loop: PollLoop? = null

    /** Waits up to [timeoutMs] (0 = non-blocking) for an event; null if none. */
    fun poll(timeoutMs: Int = 0): PeerovaEvent? = node.poll(timeoutMs)?.let(PeerovaEvent::from)

    /** Lazily yields events until [idleMs] passes without one. */
    fun events(idleMs: Int = 1000): Sequence<PeerovaEvent> = generateSequence { poll(idleMs) }

    /**
     * reliable=true: ordered + retransmitted per channel (0..255).
     * reliable=false: datagram (may be lost/reordered), max [MAX_UNRELIABLE_PAYLOAD] bytes.
     * Throws [PeerovaException] (e.g. unknown peer).
     */
    fun send(peer: Long, data: ByteArray, channel: Int = 0, reliable: Boolean = true) =
        node.send(peer, data, channel, reliable)

    fun send(peer: Long, text: String, channel: Int = 0, reliable: Boolean = true) =
        node.send(peer, text.toByteArray(Charsets.UTF_8), channel, reliable)

    /** Like [send] but returns false instead of throwing. */
    fun trySend(peer: Long, data: ByteArray, channel: Int = 0, reliable: Boolean = true): Boolean =
        node.trySend(peer, data, 0, data.size, channel, reliable)

    fun disconnect(peer: Long) = node.disconnect(peer)

    val peerCount: Long get() = node.peerCount()

    val isClosed: Boolean get() = node.isClosed

    /**
     * Starts a background thread that polls and calls [listener] for every event.
     * [executor] chooses where the listener runs (null = on the poll thread). On Android pass
     * `ContextCompat.getMainExecutor(context)` (or `Executor { Handler(Looper.getMainLooper()).post(it) }`)
     * to receive events on the UI thread. Only one loop per node; [close] stops it.
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

class PeerovaServer private constructor(private val srv: Peerova.Server) : PeerovaNode(srv) {
    /** Share with clients (`pvt_...`). */
    val ticket: String get() = srv.ticket()

    companion object {
        /** Starts a server; blocks up to ~3 s until the ticket is ready — call off the main thread. */
        @JvmStatic
        fun start(configJson: String? = null): PeerovaServer = PeerovaServer(Peerova.Server.start(configJson))

        /** Config keys as in peerova.h: port, key_file, license, upnp, token, max_ccu, ... */
        @JvmStatic
        fun start(config: Map<String, Any?>): PeerovaServer = start(toJson(config))
    }
}

class PeerovaClient private constructor(cli: Peerova.Client) : PeerovaNode(cli) {
    companion object {
        /** Returns immediately; wait for [PeerovaEvent.Connected] / [PeerovaEvent.ConnectFailed]. */
        @JvmStatic
        fun connect(ticket: String, configJson: String? = null): PeerovaClient =
            PeerovaClient(Peerova.Client.connect(ticket, configJson))

        /** Config keys: token, timeout_ms, supernodes, force_tcp. */
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
