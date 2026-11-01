<?php
// How to run (from bindings/php):
//   php examples/chat.php server                       # prints the ticket
//   php examples/chat.php client <ticket|code>         # type lines to chat, /quit to leave
// Options: --stdio (force the sidecar backend), --seconds N (client quits after N s), --key FILE.
//
// Tiny chat + position-sync demo. Positions go unreliable on channel 1 at 20 Hz (a lost update
// is replaced by the next one); chat lines go reliable on channel 0. Uses the FFI backend when
// ext/ffi is loaded, otherwise the `peerova stdio` sidecar — the code is the same for both.
declare(strict_types=1);

require __DIR__ . '/../src/Common.php';
require __DIR__ . '/../src/Peerova.php';
require __DIR__ . '/../src/PeerovaStdio.php';

use Peerova\{EventType, Node, Peerova, PeerovaException, PeerovaStdio};

const CHAT = 0;
const POSITION = 1;

function opt(array $argv, string $name): ?string
{
    $i = array_search($name, $argv, true);
    return $i !== false && isset($argv[$i + 1]) ? $argv[$i + 1] : null;
}

$mode = $argv[1] ?? '';
if ($mode !== 'server' && !($mode === 'client' && isset($argv[2]))) {
    fwrite(STDERR, "usage: php chat.php server [--key FILE] [--stdio] | php chat.php client <ticket|code> [--seconds N] [--stdio]\n");
    exit(2);
}
$useFfi = extension_loaded('ffi') && !in_array('--stdio', $argv, true);
echo 'backend: ', $useFfi ? 'ffi' : 'stdio sidecar', "\n";

try {
    if ($mode === 'server') {
        $key = opt($argv, '--key') ?? sys_get_temp_dir() . '/peerova-chat-example.key';
        $config = ['port' => 0, 'key_file' => $key];
        runServer($useFfi ? Peerova::server($config) : PeerovaStdio::server($config));
    } else {
        $secs = opt($argv, '--seconds');
        runClient($useFfi ? Peerova::client($argv[2]) : PeerovaStdio::client($argv[2]), $secs === null ? null : (float)$secs);
    }
} catch (PeerovaException $e) {
    fwrite(STDERR, 'peerova error: ' . $e->getMessage() . "\n");
    exit(1);
}

function runServer(Node $srv): void
{
    echo "ticket: {$srv->ticket()}\n";
    echo "run:    php examples/chat.php client {$srv->ticket()}\n";
    $stop = false;
    if (function_exists('pcntl_signal')) {
        pcntl_async_signals(true);
        pcntl_signal(SIGINT, function () use (&$stop) { $stop = true; });
        pcntl_signal(SIGTERM, function () use (&$stop) { $stop = true; });
    }
    $peers = [];
    while (!$stop) {
        $ev = $srv->poll(0.05);
        if ($ev === null) {
            continue;
        }
        switch ($ev->type) {
            case EventType::CONNECTED:
                $peers[$ev->peer] = true;
                echo "peer {$ev->peer} joined (" . count($peers) . " online)\n";
                break;
            case EventType::DISCONNECTED:
                unset($peers[$ev->peer]);
                echo "peer {$ev->peer} left: {$ev->data}\n";
                break;
            case EventType::MESSAGE:
                if ($ev->channel === CHAT) {
                    echo "[{$ev->peer}] {$ev->data}\n";
                }
                foreach (array_keys($peers) as $p) { // forward to everyone else, same delivery mode
                    if ($p !== $ev->peer) {
                        try {
                            $srv->send($p, $ev->data, $ev->channel, $ev->reliable);
                        } catch (PeerovaException $e) {
                            fwrite(STDERR, "send to $p failed: {$e->getMessage()}\n");
                        }
                    }
                }
                break;
        }
    }
    $srv->close();
    echo "server stopped\n";
}

function runClient(Node $cli, ?float $runFor): void
{
    $start = microtime(true);
    $server = null;
    $sent = $received = 0;
    $nextMove = 0.0;
    stream_set_blocking(STDIN, false);
    $why = 'time is up';
    while ($runFor === null || microtime(true) - $start < $runFor) {
        // 1. network events (waits at most 10 ms)
        $ev = $cli->poll(0.01);
        if ($ev !== null) {
            if ($ev->type === EventType::CONNECTED) {
                $server = $ev->peer;
                echo "connected; type a line to chat, /quit to leave\n";
                $cli->send($server, 'hello from ' . gethostname(), CHAT, true);
            } elseif ($ev->type === EventType::CONNECT_FAILED) {
                $why = "connect failed: {$ev->data}";
                break;
            } elseif ($ev->type === EventType::DISCONNECTED) {
                $why = "disconnected: {$ev->data}";
                break;
            } elseif ($ev->channel === CHAT) {
                echo "chat: {$ev->data}\n";
            } elseif ($ev->channel === POSITION && strlen($ev->data) === 8) {
                if ($received++ % 20 === 0) {
                    [, $x, $y] = unpack('g2', $ev->data) + [0, 0, 0];
                    printf("other player at (%.2f, %.2f)\n", $x, $y);
                }
            }
        }
        // 2. 20 Hz position updates: small, frequent, superseded → unreliable
        $now = microtime(true);
        if ($server !== null && $now >= $nextMove) {
            $t = $now - $start;
            $cli->send($server, pack('g2', cos($t), sin($t)), POSITION, false);
            $sent++;
            $nextMove = $now + 0.05;
        }
        // 3. chat input (non-blocking)
        $line = fgets(STDIN);
        if ($line !== false) {
            $line = rtrim($line, "\r\n");
            if ($line === '/quit') {
                $why = 'quit';
                break;
            }
            if ($server !== null && $line !== '') {
                $cli->send($server, $line, CHAT, true); // reliable, ordered
            }
        }
    }
    $cli->close();
    echo "client stopped ($why): sent $sent positions, received $received\n";
}
