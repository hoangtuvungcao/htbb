<?php
declare(strict_types=1);

namespace Peerova;

/**
 * Native in-process binding via PHP ext/ffi (PHP >= 7.4).
 *
 *   $srv = Peerova\Peerova::server(['port' => 0, 'upnp' => false]);
 *   $cli = Peerova\Peerova::client($srv->ticket());   // or a 6-character connect code
 *   $ev  = $cli->poll(10);                           // CONNECTED
 *   $cli->send($ev->peer, "hello", 0, true);
 *
 * Requirements: ext-ffi loaded and `ffi.enable=1` in php.ini (the default "preload" allows FFI only
 * in preloaded scripts). Library lookup: getenv('PEEROVA_LIB'), next to this file, the SDK
 * development tree, then the system loader (LD_LIBRARY_PATH / PATH).
 * Hosts without ext/ffi (shared hosting, many php-fpm setups): use PeerovaStdio — same API.
 */
final class Peerova
{
    private const CDEF = <<<'C'
typedef struct peerova_node peerova_node;
typedef struct {
    int32_t type; uint32_t peer; uint8_t channel; uint8_t reliable;
    const uint8_t* data; uint32_t len;
} peerova_event;
peerova_node* peerova_server_start(const char* config_json);
peerova_node* peerova_client_connect(const char* ticket, const char* config_json);
int32_t peerova_ticket(peerova_node* node, char* buf, int32_t buf_len);
int32_t peerova_room_code(peerova_node* node, char* buf, int32_t buf_len);
int32_t peerova_send(peerova_node* node, uint32_t peer, uint8_t channel, const char* data, uint32_t len, int32_t reliable);
int32_t peerova_poll(peerova_node* node, peerova_event* out);
int32_t peerova_poll_wait(peerova_node* node, peerova_event* out, uint32_t timeout_ms);
void peerova_disconnect(peerova_node* node, uint32_t peer);
uint32_t peerova_peer_count(peerova_node* node);
void peerova_free(peerova_node* node);
const char* peerova_last_error(void);
const char* peerova_version(void);
C;
    // peerova_send's data is declared `const char*` (ABI-identical to const uint8_t*) so PHP strings pass directly.

    /** @var \FFI|null */
    private static $ffi;

    /**
     * The loaded FFI instance (loads the native library on first use). Use it to call the raw C API.
     *
     * @throws PeerovaException if ext/ffi is not loaded
     * @throws \FFI\Exception if the native library cannot be found / loaded
     */
    public static function ffi(): \FFI
    {
        if (self::$ffi === null) {
            if (!extension_loaded('ffi')) {
                throw new PeerovaException('ext/ffi is not loaded — enable it or use Peerova\\PeerovaStdio');
            }
            $name = PHP_OS_FAMILY === 'Windows' ? 'peerova.dll' : (PHP_OS_FAMILY === 'Darwin' ? 'libpeerova.dylib' : 'libpeerova.so');
            $lib = $name;
            foreach ([getenv('PEEROVA_LIB') ?: null, __DIR__ . '/' . $name, __DIR__ . '/../../../target/release/' . $name] as $p) {
                if ($p !== null && is_file($p)) {
                    $lib = $p;
                    break;
                }
            }
            self::$ffi = \FFI::cdef(self::CDEF, $lib);
        }
        return self::$ffi;
    }

    /** @internal */
    public static function str($v): string
    {
        if ($v === null) {
            return '';
        }
        return is_string($v) ? $v : (\FFI::isNull($v) ? '' : \FFI::string($v));
    }

    /** @internal */
    public static function lastError(): string
    {
        return self::str(self::ffi()->peerova_last_error());
    }

    /** Version string of the native library, e.g. "0.1.0". */
    public static function version(): string
    {
        return self::str(self::ffi()->peerova_version());
    }

    /**
     * Starts a server; blocks up to ~3 s until the ticket is ready.
     *
     * Config keys (all optional): port (default 47000, 0 = random), key_file (default "server.key";
     * same key => same ticket), license ("PVA-..."; without it dev mode, max 3 clients), max_ccu,
     * token (shared secret clients must present), upnp (default true), private, and for a connect
     * code app (project id) + share (["name","public","max","mode","region"]).
     *
     * @param array<string,mixed> $config
     * @throws PeerovaException with the native error text (bad config, port in use, ...)
     */
    public static function server(array $config = []): FfiNode
    {
        $h = self::ffi()->peerova_server_start(json_encode((object)$config));
        return new FfiNode($h, true);
    }

    /**
     * Starts connecting and returns immediately; poll() for CONNECTED or CONNECT_FAILED.
     *
     * @param string $ticket server ticket ("pvt_...") or its 6-character connect code
     * @param array<string,mixed> $config token (must match the server), timeout_ms (default 20000)
     * With a connect code the call first looks the code up on the Peerova website (blocks briefly).
     *
     * @throws PeerovaException for an invalid ticket or an unknown / closed connect code
     */
    public static function client(string $ticket, array $config = []): FfiNode
    {
        $h = self::ffi()->peerova_client_connect($ticket, json_encode((object)$config));
        return new FfiNode($h, false);
    }
}

/** A native Peerova node (server or client) created by Peerova::server() / Peerova::client(). */
final class FfiNode implements Node
{
    /** @var \FFI\CData|null */
    private $h;
    /** @var \FFI\CData */
    private $ev;
    /** @var string|null */
    private $ticket;

    /** @internal use Peerova::server / Peerova::client */
    public function __construct($handle, bool $isServer)
    {
        if ($handle === null || \FFI::isNull($handle)) {
            throw new PeerovaException(Peerova::lastError());
        }
        $f = Peerova::ffi();
        $this->h = $handle;
        $this->ev = $f->new('peerova_event');
        if ($isServer) {
            $n = $f->peerova_ticket($handle, null, 0);
            if ($n > 0) {
                $buf = $f->new("char[$n]");
                $f->peerova_ticket($handle, $buf, $n);
                $this->ticket = \FFI::string($buf, $n - 1);
            }
        }
    }

    /** {@inheritdoc} */
    public function ticket(): ?string
    {
        return $this->ticket;
    }

    /**
     * Server only: the 6-character connect code (e.g. "K7Q2XD") assigned by the Peerova website, or
     * null while not (yet) available. Only servers started with `app` + `share` get one; it usually
     * appears a few seconds after start, so call again later. Clients may pass it to
     * Peerova::client() instead of the ticket.
     */
    public function roomCode(): ?string
    {
        $f = Peerova::ffi();
        $h = $this->handle();
        $n = $f->peerova_room_code($h, null, 0);
        if ($n <= 0) {
            return null;
        }
        $buf = $f->new("char[$n]");
        $m = $f->peerova_room_code($h, $buf, $n);
        if ($m <= 0 || $m > $n) {
            return null;
        }
        $code = \FFI::string($buf, $m - 1);
        return $code === '' ? null : $code;
    }

    /** {@inheritdoc} Thread-safe on the native side; errors carry the native message. */
    public function send(int $peer, string $data, int $channel = 0, bool $reliable = true): void
    {
        if (Peerova::ffi()->peerova_send($this->handle(), $peer, $channel, $data, strlen($data), $reliable ? 1 : 0) !== 0) {
            throw new PeerovaException(Peerova::lastError());
        }
    }

    /** {@inheritdoc} With $timeout > 0 the call blocks the PHP process up to that long. */
    public function poll(float $timeout = 0): ?Event
    {
        $f = Peerova::ffi();
        $p = \FFI::addr($this->ev);
        $got = $timeout > 0 ? $f->peerova_poll_wait($this->handle(), $p, (int)($timeout * 1000)) : $f->peerova_poll($this->handle(), $p);
        if (!$got) {
            return null;
        }
        $e = $this->ev;
        // event.data is valid only until the next poll: copy into a PHP string now.
        $data = $e->len > 0 ? \FFI::string($e->data, $e->len) : '';
        return new Event($e->type, $e->peer, $e->channel, $e->reliable !== 0, $data);
    }

    /** {@inheritdoc} */
    public function events(float $timeout = 1.0): \Generator
    {
        while (($ev = $this->poll($timeout)) !== null) {
            yield $ev;
        }
    }

    /** {@inheritdoc} Unknown peers are ignored. */
    public function disconnect(int $peer): void
    {
        Peerova::ffi()->peerova_disconnect($this->handle(), $peer);
    }

    /** Number of connected peers (server: clients; client: 0 or 1). */
    public function peerCount(): int
    {
        return Peerova::ffi()->peerova_peer_count($this->handle());
    }

    /** {@inheritdoc} */
    public function close(): void
    {
        if ($this->h !== null) {
            Peerova::ffi()->peerova_free($this->h);
            $this->h = null;
        }
    }

    /** Frees the native node if close() was not called. */
    public function __destruct()
    {
        $this->close();
    }

    private function handle()
    {
        if ($this->h === null) {
            throw new PeerovaException('node is closed');
        }
        return $this->h;
    }
}
