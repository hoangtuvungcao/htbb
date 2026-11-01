<?php
declare(strict_types=1);

namespace Peerova;

/**
 * Native in-process binding via PHP ext/ffi (PHP >= 7.4).
 *
 *   $srv = Peerova\Peerova::server(['port' => 0, 'upnp' => false]);
 *   $cli = Peerova\Peerova::client($srv->ticket());
 *   $ev  = $cli->poll(10);                       // CONNECTED
 *   $cli->send($ev->peer, "hello", 0, true);
 *
 * Requirements: ext-ffi loaded and `ffi.enable=true` (php.ini; the default "preload" only allows
 * FFI in preloaded scripts / CLI). Library lookup: getenv('PEEROVA_LIB'), next to this file,
 * ../../../target/release (repo checkout), then the system loader.
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

    public static function version(): string
    {
        return self::str(self::ffi()->peerova_version());
    }

    /** Starts a server; blocks up to ~3 s until the ticket is ready. @param array<string,mixed> $config */
    public static function server(array $config = []): FfiNode
    {
        $h = self::ffi()->peerova_server_start(json_encode((object)$config));
        return new FfiNode($h, true);
    }

    /** Connects to a server ticket; returns immediately, wait for CONNECTED / CONNECT_FAILED. */
    public static function client(string $ticket, array $config = []): FfiNode
    {
        $h = self::ffi()->peerova_client_connect($ticket, json_encode((object)$config));
        return new FfiNode($h, false);
    }
}

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

    public function ticket(): ?string
    {
        return $this->ticket;
    }

    public function send(int $peer, string $data, int $channel = 0, bool $reliable = true): void
    {
        if (Peerova::ffi()->peerova_send($this->handle(), $peer, $channel, $data, strlen($data), $reliable ? 1 : 0) !== 0) {
            throw new PeerovaException(Peerova::lastError());
        }
    }

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

    public function events(float $timeout = 1.0): \Generator
    {
        while (($ev = $this->poll($timeout)) !== null) {
            yield $ev;
        }
    }

    public function disconnect(int $peer): void
    {
        Peerova::ffi()->peerova_disconnect($this->handle(), $peer);
    }

    public function peerCount(): int
    {
        return Peerova::ffi()->peerova_peer_count($this->handle());
    }

    public function close(): void
    {
        if ($this->h !== null) {
            Peerova::ffi()->peerova_free($this->h);
            $this->h = null;
        }
    }

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
