import dev.peerova.kt.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

fun check(ok: Boolean, what: String) { if (!ok) { println("FAIL: $what"); exitProcess(1) } }

fun main() {
    println("peerova native ${peerovaVersion()}")
    val key = File.createTempFile("peerova-kt", ".key").apply { delete() }
    val cfg = mapOf("port" to 0, "upnp" to false, "key_file" to key.absolutePath)
    PeerovaServer.start(cfg).use { srv ->
        check(srv.ticket.startsWith("pvt_"), "ticket")
        check(srv.roomCode == null, "connect code without app/share config")
        PeerovaClient.connect(srv.ticket).use { cli ->
            // 1) blocking poll API
            val peer = cli.events(10_000).first { it !is PeerovaEvent.Message }.let {
                check(it is PeerovaEvent.Connected, "connect: $it"); it.peer
            }
            val n = 100
            val t0 = System.nanoTime()
            repeat(n) { cli.send(peer, "msg $it", channel = 1) }
            cli.send(peer, "realtime", channel = 2, reliable = false)

            // 2) background poll loop with listener on the server
            val got = java.util.Collections.synchronizedList(mutableListOf<String>())
            var unreliable = false
            var sp = -1L
            val done = CountDownLatch(1)
            val loop = srv.startPolling { ev ->
                when (ev) {
                    is PeerovaEvent.Connected -> sp = ev.peer
                    is PeerovaEvent.Message ->
                        if (ev.reliable && ev.channel == 1) got.add(ev.text)
                        else if (!ev.reliable && ev.channel == 2) unreliable = ev.text == "realtime"
                    else -> {}
                }
                if (got.size == n && unreliable) done.countDown()
            }
            check(done.await(5, TimeUnit.SECONDS), "got ${got.size}/$n reliable, unreliable=$unreliable")
            loop.close()
            check(got == List(n) { "msg $it" }, "order: ${got.take(5)}")
            check(srv.peerCount == 1L && sp >= 0, "peerCount=${srv.peerCount}")

            // 3) reply, received through the coroutines Flow extension
            srv.send(sp, "pong")
            val reply = runBlocking { withTimeout(5_000) { cli.eventFlow().filterIsInstance<PeerovaEvent.Message>().first() } }
            check(reply.text == "pong" && reply.reliable, "reply: $reply")
            println("kotlin PASS: $n reliable msgs in order + 1 unreliable + reply (Flow), ${(System.nanoTime() - t0) / 1_000_000} ms")
        }
    }
}
