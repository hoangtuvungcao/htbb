// How to run on the JVM (from bindings/kotlin-android, after ./test.sh has built build/peerova-kt.jar):
//   kotlinc -cp ../java/build/peerova.jar:build/peerova-kt.jar -d build/chat-demo.jar examples/ChatDemo.kt
//   CP=../java/build/peerova.jar:build/peerova-kt.jar:build/chat-demo.jar:<kotlinc>/lib/kotlin-stdlib.jar
//   java -Djava.library.path=../java/build -cp $CP ChatDemoKt server [port] [key_file]
//   java -Djava.library.path=../java/build -cp $CP ChatDemoKt client <TICKET or CONNECT CODE>
//
// Tiny chat + position-sync demo. The server prints its ticket and forwards chat lines (reliable, channel 0)
// and positions (unreliable, channel 1) to the other clients, using a background poll loop (startPolling).
// The client sends its position 20 times per second and every stdin line as chat; "/quit" or end of input
// disconnects cleanly. On Android the same calls apply: run start/connect off the main thread and pass
// ContextCompat.getMainExecutor(context) to startPolling to get events on the UI thread.
import dev.peerova.kt.PeerovaClient
import dev.peerova.kt.PeerovaEvent
import dev.peerova.kt.PeerovaServer
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.exitProcess

const val CH_CHAT = 0      // reliable: ordered, never lost
const val CH_POSITION = 1  // unreliable: latest state wins, may be dropped

fun main(args: Array<String>) {
    when {
        args.getOrNull(0) == "server" -> server(args.getOrNull(1)?.toInt() ?: 47000, args.getOrNull(2) ?: "server.key")
        args.getOrNull(0) == "client" && args.size >= 2 -> client(args[1])
        else -> { System.err.println("usage: ChatDemoKt server [port] [key_file] | ChatDemoKt client <TICKET or CODE>"); exitProcess(2) }
    }
}

fun server(port: Int, keyFile: String) {
    val stopped = CountDownLatch(1)
    PeerovaServer.start(mapOf("port" to port, "key_file" to keyFile)).use { srv ->
        println("ticket: ${srv.ticket}")
        val peers = ConcurrentHashMap.newKeySet<Long>()
        var positions = 0
        // Listener runs on the poll thread (no executor). On Android pass a main-thread Executor instead.
        srv.startPolling { ev ->
            when (ev) {
                is PeerovaEvent.Connected -> { peers += ev.peer; println("peer ${ev.peer} joined (${srv.peerCount} online)") }
                is PeerovaEvent.Disconnected -> { peers -= ev.peer; println("peer ${ev.peer} left: ${ev.reason}") }
                is PeerovaEvent.Message -> when (ev.channel) {
                    CH_CHAT -> {
                        val line = "peer ${ev.peer}: ${ev.text}"
                        println(line)
                        for (p in peers) if (p != ev.peer) srv.trySend(p, line.toByteArray(), CH_CHAT, reliable = true)
                    }
                    CH_POSITION -> {
                        if (++positions % 20 == 0) {
                            val b = ByteBuffer.wrap(ev.data)
                            println("peer ${ev.peer} position %.1f,%.1f ($positions updates)".format(b.float, b.float))
                        }
                        for (p in peers) if (p != ev.peer) srv.trySend(p, ev.data, CH_POSITION, reliable = false)
                    }
                    else -> {}
                }
                is PeerovaEvent.ConnectFailed -> {}
            }
        }
        // Ctrl+C / SIGTERM: release the main thread; use{} then closes the server (which stops the poll loop).
        val hookDone = CountDownLatch(1)
        Runtime.getRuntime().addShutdownHook(thread(start = false) { stopped.countDown(); hookDone.await(5, TimeUnit.SECONDS) })
        stopped.await()
        srv.close()
        println("server stopped")
        hookDone.countDown()
    }
}

fun client(ticketOrCode: String) {
    PeerovaClient.connect(ticketOrCode, mapOf("timeout_ms" to 20_000)).use { cli ->
        val server = when (val ev = generateSequence { cli.poll(500) }.first { it !is PeerovaEvent.Message }) {
            is PeerovaEvent.Connected -> ev.peer
            else -> { System.err.println("connect failed: $ev"); exitProcess(1) }
        }
        println("connected, type chat lines (/quit to leave)")

        // stdin on its own daemon thread; it only hands lines over a queue and never touches the node.
        val typed = LinkedBlockingQueue<String>()
        thread(isDaemon = true, name = "stdin") {
            System.`in`.bufferedReader().forEachLine { typed.add(it) }
            typed.add("/quit")
        }

        val loop = cli.startPolling { ev ->
            when (ev) {
                is PeerovaEvent.Message -> if (ev.channel == CH_CHAT) println(ev.text)
                is PeerovaEvent.Disconnected -> { println("server closed: ${ev.reason}"); typed.add("/quit") }
                else -> {}
            }
        }

        var sent = 0
        var nextPos = 0L
        while (true) {
            val now = System.currentTimeMillis()
            if (now >= nextPos) {                                  // ~20 Hz position update
                val t = now / 1000.0
                val pos = ByteBuffer.allocate(8).putFloat((cos(t) * 10).toFloat()).putFloat((sin(t) * 10).toFloat()).array()
                if (cli.trySend(server, pos, CH_POSITION, reliable = false)) sent++
                nextPos = now + 50
            }
            val line = typed.poll(10, TimeUnit.MILLISECONDS) ?: continue
            if (line == "/quit") break
            cli.send(server, line, CH_CHAT)
        }
        Thread.sleep(300)          // let queued reliable messages reach the server
        loop.close()               // stop polling before disconnect/close
        cli.disconnect(server)
        println("bye ($sent position updates sent)")
    }
}
