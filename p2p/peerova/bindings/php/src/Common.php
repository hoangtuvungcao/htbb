<?php
declare(strict_types=1);

namespace Peerova;

/** Event type constants (same values as the C ABI's PEEROVA_EVENT_*). */
final class EventType
{
    /** Server: a client connected. Client: connected to the server. */
    public const CONNECTED = 1;
    /** A connection closed; Event::$data holds the UTF-8 reason. */
    public const DISCONNECTED = 2;
    /** A message arrived; Event::$data holds the payload. */
    public const MESSAGE = 3;
    /** Client only: could not connect; Event::$data holds the UTF-8 reason. */
    public const CONNECT_FAILED = 4;
}

/**
 * Thrown when a Peerova call fails. For the FFI binding the message is the native error text
 * (peerova_last_error); for the sidecar it is the sidecar's error or a local check.
 */
class PeerovaException extends \RuntimeException
{
}

/** One network event returned by Node::poll() / Node::events(). */
final class Event
{
    /** @var int one of the EventType constants */
    public $type;
    /** @var int peer (connection) id; 0 for CONNECT_FAILED */
    public $peer;
    /** @var int channel 0..255 (MESSAGE only, else 0) */
    public $channel;
    /** @var bool true if the message was sent reliably (MESSAGE only) */
    public $reliable;
    /** @var string payload (binary string) for MESSAGE; UTF-8 reason for DISCONNECTED / CONNECT_FAILED; else '' */
    public $data;

    /** @internal created by the bindings */
    public function __construct(int $type, int $peer, int $channel, bool $reliable, string $data)
    {
        $this->type = $type;
        $this->peer = $peer;
        $this->channel = $channel;
        $this->reliable = $reliable;
        $this->data = $data;
    }

    /** True if this is a MESSAGE event. */
    public function isMessage(): bool
    {
        return $this->type === EventType::MESSAGE;
    }
}

/**
 * Common API of the FFI node (Peerova::server / Peerova::client) and the sidecar node
 * (PeerovaStdio::server / PeerovaStdio::client). A node is not safe to share between threads.
 */
interface Node
{
    /** Server ticket ("pvt_..."), share it with clients; null for clients. */
    public function ticket(): ?string;

    /**
     * Sends $data (binary-safe string) to $peer on $channel (0..255).
     * reliable=true: ordered and retransmitted per channel, any size (chat, RPC, file chunks).
     * reliable=false: datagram, may be lost or reordered, max 1180 bytes (positions, inputs).
     *
     * @throws PeerovaException if the send is rejected or the node is closed
     */
    public function send(int $peer, string $data, int $channel = 0, bool $reliable = true): void;

    /**
     * Returns the next event, or null if none arrives within $timeout seconds (0 = non-blocking).
     *
     * @throws PeerovaException if the node is closed (FFI)
     */
    public function poll(float $timeout = 0): ?Event;

    /**
     * Yields events until $timeout seconds pass without one.
     *
     * @return \Generator<Event>
     */
    public function events(float $timeout = 1.0): \Generator;

    /** Closes the connection to $peer; a DISCONNECTED event follows. */
    public function disconnect(int $peer): void;

    /** Disconnects all peers and frees the node. Idempotent; also called by the destructor. */
    public function close(): void;
}
