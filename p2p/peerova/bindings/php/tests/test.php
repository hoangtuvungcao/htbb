<?php
// Run: php tests/test.php     (sidecar variant always; FFI variant only when ext/ffi is loaded)
declare(strict_types=1);
require __DIR__ . '/../src/Common.php';
require __DIR__ . '/../src/Peerova.php';
require __DIR__ . '/../src/PeerovaStdio.php';

use Peerova\{EventType, Node};

function check(bool $c, string $msg): void
{
    if (!$c) {
        fwrite(STDERR, "FAIL: $msg\n");
        exit(1);
    }
}

function roundtrip(string $name, callable $server, callable $client): void
{
    $N = 200;
    $key = sys_get_temp_dir() . '/peerova-php-' . getmypid() . "-$name.key";
    $t = microtime(true);
    /** @var Node $srv */
    $srv = $server(['port' => 0, 'upnp' => false, 'key_file' => $key]);
    check(strpos((string)$srv->ticket(), 'pvt_') === 0, "$name ticket");
    if ($srv instanceof Peerova\FfiNode) {
        check($srv->roomCode() === null, "$name no connect code without app/share");
    }
    /** @var Node $cli */
    $cli = $client($srv->ticket());
    $ev = $cli->poll(10);
    check($ev !== null && $ev->type === EventType::CONNECTED, "$name client connected");
    $peer = $ev->peer;
    for ($i = 0; $i < $N; $i++) {
        $cli->send($peer, "msg $i", 1, true);
    }
    $cli->send($peer, 'realtime', 2, false);
    $got = [];
    $sp = null;
    $unrel = 0;
    foreach ($srv->events(5) as $e) {
        if ($e->type === EventType::CONNECTED) {
            $sp = $e->peer;
        } elseif ($e->isMessage() && $e->reliable) {
            check($e->channel === 1, "$name channel");
            $got[] = $e->data;
            if (count($got) === $N) {
                break;
            }
        } elseif ($e->isMessage()) {
            $unrel++;
        }
    }
    $want = [];
    for ($i = 0; $i < $N; $i++) {
        $want[] = "msg $i";
    }
    check($got === $want, "$name $N reliable msgs in order (got " . count($got) . ')');
    check($sp !== null, "$name server saw CONNECTED");
    // the datagram may land after the last reliable message (on loopback it is never lost)
    if ($unrel === 0) {
        foreach ($srv->events(1) as $e) {
            if ($e->isMessage() && !$e->reliable && $e->data === 'realtime') {
                $unrel++;
                break;
            }
        }
    }
    check($unrel === 1, "$name unreliable datagram received");
    $srv->send($sp, "pong\x00\xff", 0, true);
    $m = null;
    foreach ($cli->events(5) as $e) {
        if ($e->isMessage() && $e->reliable) {
            $m = $e;
            break;
        }
    }
    check($m !== null && $m->data === "pong\x00\xff", "$name reply");
    $ms = 1000 * (microtime(true) - $t);
    $cli->close();
    $srv->close();
    @unlink($key);
    printf("php %s OK: %d reliable msgs in order, unreliable received=%d, binary reply ok, %.1f ms\n", $name, $N, $unrel, $ms);
}

roundtrip('stdio', [Peerova\PeerovaStdio::class, 'server'], [Peerova\PeerovaStdio::class, 'client']);
if (extension_loaded('ffi')) {
    echo 'peerova ', Peerova\Peerova::version(), "\n";
    roundtrip('ffi', [Peerova\Peerova::class, 'server'], [Peerova\Peerova::class, 'client']);
} else {
    echo "php ffi SKIP: ext/ffi not loaded (enable extension=ffi and ffi.enable=true to run it)\n";
}
