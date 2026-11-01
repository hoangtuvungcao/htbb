<?php
declare(strict_types=1);

namespace Peerova;

/**
 * Pure-PHP binding through the `peerova stdio` sidecar process (JSON lines). Same API as Peerova (FFI),
 * no extension needed — only proc_open and the `peerova` command-line binary.
 *
 *   $srv = Peerova\PeerovaStdio::server(['port' => 0, 'upnp' => false]);
 *   $cli = Peerova\PeerovaStdio::client($srv->ticket());   // or a 6-character connect code
 *
 * Binary lookup: $binary argument, getenv('PEEROVA_BIN'), the SDK development tree, then `peerova`
 * on PATH. The sidecar does not report the server's connect code (use the FFI binding for that);
 * clients can still connect with a code. A php-fpm request is short-lived — run long-lived
 * servers and clients as CLI worker processes.
 */
final class PeerovaStdio
{
    /** Resolves the sidecar binary path (see the class comment for the lookup order). */
    public static function binary(?string $override = null): string
    {
        $exe = PHP_OS_FAMILY === 'Windows' ? 'peerova.exe' : 'peerova';
        foreach ([$override, getenv('PEEROVA_BIN') ?: null, __DIR__ . '/../../../target/release/' . $exe] as $p) {
            if ($p !== null && is_file($p)) {
                return $p;
            }
        }
        return $exe;
    }

    /**
     * Starts the sidecar server and waits (up to 15 s) for its ticket.
     *
     * @param array<string,mixed> $config same keys as Peerova::server()
     * @param string|null $binary explicit path of the `peerova` binary
     * @throws PeerovaException if the binary is missing or the server cannot start (the sidecar prints
     *         the exact reason on stderr)
     */
    public static function server(array $config = [], ?string $binary = null): StdioNode
    {
        return new StdioNode([self::binary($binary), 'stdio', 'server', json_encode((object)$config)]);
    }

    /**
     * Starts the sidecar client; poll() for CONNECTED or CONNECT_FAILED.
     *
     * @param string $ticket server ticket ("pvt_...") or 6-character connect code
     * @param array<string,mixed> $config token, timeout_ms (default 20000)
     * @param string|null $binary explicit path of the `peerova` binary
     * @throws PeerovaException if the binary is missing, the ticket is invalid or the connect code is
     *         unknown (the sidecar prints the exact reason on stderr)
     */
    public static function client(string $ticket, array $config = [], ?string $binary = null): StdioNode
    {
        return new StdioNode([self::binary($binary), 'stdio', 'client', $ticket, json_encode((object)$config)]);
    }

    /** @internal */
    public static function b64url(string $d): string
    {
        return rtrim(strtr(base64_encode($d), '+/', '-_'), '=');
    }

    /** @internal */
    public static function unb64url(string $s): string
    {
        $r = base64_decode(strtr($s, '-_', '+/'));
        return $r === false ? '' : $r;
    }
}

/** A Peerova node running in a `peerova stdio` child process. */
final class StdioNode implements Node
{
    /** @var resource|null */
    private $proc;
    /** @var resource */
    private $in;
    /** @var resource */
    private $out;
    /** @var string */
    private $buf = '';
    /** @var string|null */
    private $ticket;
    /** @var array<int,true> */
    private $peers = [];
    /** @var string[] sidecar error messages ({"event":"error"}), drained by lastErrors() */
    private $errors = [];

    /** @internal @param string[] $cmd */
    public function __construct(array $cmd)
    {
        $err = defined('STDERR') ? STDERR : ['file', PHP_OS_FAMILY === 'Windows' ? 'NUL' : '/dev/null', 'w'];
        $proc = proc_open($cmd, [0 => ['pipe', 'r'], 1 => ['pipe', 'w'], 2 => $err], $pipes);
        if (!is_resource($proc)) {
            throw new PeerovaException('cannot start ' . $cmd[0]);
        }
        $this->proc = $proc;
        $this->in = $pipes[0];
        $this->out = $pipes[1];
        stream_set_blocking($this->out, false);
        $deadline = microtime(true) + 15;
        while (true) {
            $j = $this->readLine(max(0.0, $deadline - microtime(true)));
            if ($j === null) {
                $this->close();
                throw new PeerovaException('sidecar did not become ready (binary missing or bad config?)');
            }
            if (($j['event'] ?? '') === 'ready') {
                $this->ticket = $j['ticket'] ?? null;
                return;
            }
            if (($j['event'] ?? '') === 'error') {
                $this->close();
                throw new PeerovaException((string)($j['message'] ?? 'error'));
            }
        }
    }

    /** {@inheritdoc} */
    public function ticket(): ?string
    {
        return $this->ticket;
    }

    /**
     * {@inheritdoc}
     * Asynchronous: errors detected by the sidecar (e.g. unknown peer) are collected by poll() and
     * returned by lastErrors(). Throws only for local checks (unreliable > 1180 bytes, closed node).
     */
    public function send(int $peer, string $data, int $channel = 0, bool $reliable = true): void
    {
        if (!$reliable && strlen($data) > 1180) {
            throw new PeerovaException('unreliable payload max 1180 bytes');
        }
        $this->write(['cmd' => 'send', 'peer' => $peer, 'channel' => $channel, 'reliable' => $reliable, 'data' => PeerovaStdio::b64url($data)]);
    }

    /** {@inheritdoc} Returns null also when the sidecar has exited. */
    public function poll(float $timeout = 0): ?Event
    {
        $deadline = microtime(true) + $timeout;
        while (true) {
            $j = $this->readLine(max(0.0, $deadline - microtime(true)));
            if ($j === null) {
                return null;
            }
            switch ($j['event'] ?? '') {
                case 'connected':
                    $this->peers[(int)$j['peer']] = true;
                    return new Event(EventType::CONNECTED, (int)$j['peer'], 0, false, '');
                case 'disconnected':
                    unset($this->peers[(int)$j['peer']]);
                    return new Event(EventType::DISCONNECTED, (int)$j['peer'], 0, false, (string)($j['reason'] ?? ''));
                case 'connect_failed':
                    return new Event(EventType::CONNECT_FAILED, 0, 0, false, (string)($j['reason'] ?? ''));
                case 'message':
                    return new Event(EventType::MESSAGE, (int)$j['peer'], (int)$j['channel'], (bool)$j['reliable'], PeerovaStdio::unb64url((string)$j['data']));
                case 'error':
                    $this->errors[] = (string)($j['message'] ?? '');
                    break;
            }
        }
    }

    /** {@inheritdoc} */
    public function events(float $timeout = 1.0): \Generator
    {
        while (($ev = $this->poll($timeout)) !== null) {
            yield $ev;
        }
    }

    /** {@inheritdoc} */
    public function disconnect(int $peer): void
    {
        $this->write(['cmd' => 'disconnect', 'peer' => $peer]);
    }

    /** Connected peers as seen by the events polled so far. */
    public function peerCount(): int
    {
        return count($this->peers);
    }

    /** @return string[] error messages reported by the sidecar since the last call */
    public function lastErrors(): array
    {
        $e = $this->errors;
        $this->errors = [];
        return $e;
    }

    /** {@inheritdoc} Asks the sidecar to quit and waits for it to exit. */
    public function close(): void
    {
        if ($this->proc === null) {
            return;
        }
        @fwrite($this->in, "{\"cmd\":\"quit\"}\n");
        @fclose($this->in);
        @fclose($this->out);
        proc_close($this->proc);
        $this->proc = null;
    }

    /** Stops the sidecar if close() was not called. */
    public function __destruct()
    {
        $this->close();
    }

    private function write(array $cmd): void
    {
        if ($this->proc === null) {
            throw new PeerovaException('node is closed');
        }
        $line = json_encode($cmd) . "\n";
        for ($off = 0, $n = strlen($line); $off < $n;) {
            $w = fwrite($this->in, substr($line, $off));
            if ($w === false || $w === 0) {
                throw new PeerovaException('sidecar stdin closed');
            }
            $off += $w;
        }
    }

    /** Next decoded JSON line, or null on timeout / EOF. */
    private function readLine(float $timeout): ?array
    {
        $deadline = microtime(true) + $timeout;
        while (true) {
            $nl = strpos($this->buf, "\n");
            if ($nl !== false) {
                $line = substr($this->buf, 0, $nl);
                $this->buf = (string)substr($this->buf, $nl + 1);
                $j = json_decode($line, true);
                if (is_array($j)) {
                    return $j;
                }
                continue;
            }
            if ($this->proc === null || feof($this->out)) {
                return null;
            }
            $left = $deadline - microtime(true);
            $r = [$this->out];
            $w = $x = null;
            $sec = $left > 0 ? (int)$left : 0;
            $usec = $left > 0 ? (int)(($left - $sec) * 1e6) : 0;
            $n = @stream_select($r, $w, $x, $sec, $usec);
            if ($n) {
                $chunk = fread($this->out, 65536);
                if ($chunk !== false) {
                    $this->buf .= $chunk;
                }
            } elseif ($left <= 0) {
                return null;
            }
        }
    }
}
