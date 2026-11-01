<?php
declare(strict_types=1);

namespace Peerova;

/** Event types (same values as the C ABI). */
final class EventType
{
    public const CONNECTED = 1;
    public const DISCONNECTED = 2;
    public const MESSAGE = 3;
    public const CONNECT_FAILED = 4;
}

class PeerovaException extends \RuntimeException
{
}

/** One event. $data = payload (binary string) for MESSAGE, UTF-8 reason for DISCONNECTED / CONNECT_FAILED. */
final class Event
{
    /** @var int */ public $type;
    /** @var int */ public $peer;
    /** @var int */ public $channel;
    /** @var bool */ public $reliable;
    /** @var string */ public $data;

    public function __construct(int $type, int $peer, int $channel, bool $reliable, string $data)
    {
        $this->type = $type;
        $this->peer = $peer;
        $this->channel = $channel;
        $this->reliable = $reliable;
        $this->data = $data;
    }

    public function isMessage(): bool
    {
        return $this->type === EventType::MESSAGE;
    }
}

/** Common API of the FFI node (Peerova::server/client) and the sidecar node (PeerovaStdio::server/client). */
interface Node
{
    /** Server ticket ("pvt_..."), null for clients. */
    public function ticket(): ?string;

    /** reliable=true: ordered + retransmitted per channel; false: datagram, max 1180 bytes. */
    public function send(int $peer, string $data, int $channel = 0, bool $reliable = true): void;

    /** Next event or null. $timeout in seconds (0 = non-blocking). */
    public function poll(float $timeout = 0): ?Event;

    /** Yields events until $timeout seconds pass without one. @return \Generator<Event> */
    public function events(float $timeout = 1.0): \Generator;

    public function disconnect(int $peer): void;

    public function close(): void;
}
