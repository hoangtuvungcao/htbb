// Package peerova is a cgo binding for the Peerova C ABI (peerova.h): direct peer-to-peer networking,
// end-to-end encrypted.
//
// A Server produces a ticket (and optionally a 6-character connect code); a Client connects with either.
// Both sides receive Events by polling and exchange messages with Send.
//
//	srv, err := peerova.StartServer(`{"port":0}`)
//	cli, err := peerova.Connect(srv.Ticket(), "")
//	ev, ok := cli.PollWait(10 * time.Second) // ev.Type == peerova.Connected
//	cli.Send(ev.Peer, []byte("hi"), 0, true)
//
// # Concurrency
//
// Each node is internally synchronized, so Send, Poll, PollWait, Disconnect and PeerCount may be called
// from any goroutine. Run at most one polling goroutine per node. Close waits for in-flight calls on the
// node to return and makes later calls fail with ErrClosed (Send) or return nothing (Poll); still, stop
// the polling goroutine before calling Close so a long PollWait does not delay it.
//
// # Errors
//
// Functions that can fail return an *Error: Code is a stable error code (CodeTooLarge, CodeUnknownPeer, ...,
// same values as PEEROVA_E_* in peerova.h) and Message the English text from the native library
// ("E_TOO_LARGE: unreliable messages are limited to 1180 bytes"); Error() prefixes it with "peerova: ".
// Use CodeOf(err) to get the code of any error. Using a closed node returns ErrClosed (Code == CodeClosed).
//
// # Linking
//
// By default the package links libpeerova from the SDK's lib/linux-x86_64 directory with an rpath, and
// takes peerova.h from ../c. Override with CGO_CFLAGS / CGO_LDFLAGS, e.g.
//
//	CGO_CFLAGS=-I/opt/peerova/include CGO_LDFLAGS="-L/opt/peerova/lib -lpeerova -Wl,-rpath,/opt/peerova/lib" go build
package peerova

/*
#cgo CFLAGS: -I${SRCDIR}/../c
#cgo LDFLAGS: -L${SRCDIR}/../../target/release -lpeerova -Wl,-rpath,${SRCDIR}/../../target/release
#include <stdlib.h>
#include "peerova.h"
*/
import "C"

import (
	"errors"
	"runtime"
	"strings"
	"sync"
	"time"
	"unsafe"
)

// EventType is the kind of an Event (values mirror PEEROVA_EVENT_* in peerova.h).
type EventType int32

const (
	// Connected: server — a client joined; client — connected to the server.
	Connected EventType = 1
	// Disconnected: a peer left; Event.Data holds the UTF-8 reason.
	Disconnected EventType = 2
	// Message: a message arrived; see Event.Channel, Event.Reliable, Event.Data.
	Message EventType = 3
	// ConnectFailed (client only): the connection attempt failed; Event.Data holds the UTF-8 reason.
	ConnectFailed EventType = 4
)

// MaxUnreliablePayload is the largest payload, in bytes, accepted by an unreliable Send.
// Larger unreliable sends return an error.
const MaxUnreliablePayload = 1180

// String returns the event type name ("Connected", "Message", ...).
func (t EventType) String() string {
	switch t {
	case Connected:
		return "Connected"
	case Disconnected:
		return "Disconnected"
	case Message:
		return "Message"
	case ConnectFailed:
		return "ConnectFailed"
	}
	return "Unknown"
}

// Event is one network event returned by Poll / PollWait. Data is a Go copy and is safe to keep.
type Event struct {
	Type     EventType
	Peer     uint32 // peer (connection) id; pass it to Send / Disconnect
	Channel  uint8  // 0..255 (Message only)
	Reliable bool   // true if the message was sent reliably (Message only)
	Data     []byte // message payload, or the UTF-8 reason for Disconnected / ConnectFailed; never nil
}

// Code is a stable SDK error code (values mirror PEEROVA_E_* in peerova.h).
type Code int32

const (
	CodeOK            Code = 0  // no error
	CodeInvalidArg    Code = 1  // invalid argument / config JSON / connect code format
	CodeTooLarge      Code = 2  // unreliable payload larger than MaxUnreliablePayload
	CodeUnknownPeer   Code = 3  // peer id not connected
	CodeClosed        Code = 4  // connection or node closed
	CodeCodeNotFound  Code = 5  // 6-character connect code unknown or expired
	CodeNetwork       Code = 6  // socket / port in use / website or license server unreachable
	CodeTimeout       Code = 7  // connect timed out, server not ready, peer stopped responding
	CodeLicense       Code = 8  // license / lease invalid, expired or rejected
	CodeCCULimit      Code = 9  // the server's concurrent-client limit was reached
	CodeAccessDenied  Code = 10 // access token wrong / expired / revoked
	CodeInvalidTicket Code = 11 // ticket malformed, corrupted or unsupported
	CodeIO            Code = 12 // server key file unreadable / unwritable / corrupted
	CodeInternal      Code = 99 // internal or unclassified error
)

var codeNames = map[Code]string{
	CodeOK: "E_OK", CodeInvalidArg: "E_INVALID_ARG", CodeTooLarge: "E_TOO_LARGE", CodeUnknownPeer: "E_UNKNOWN_PEER",
	CodeClosed: "E_CLOSED", CodeCodeNotFound: "E_CODE_NOT_FOUND", CodeNetwork: "E_NETWORK", CodeTimeout: "E_TIMEOUT",
	CodeLicense: "E_LICENSE", CodeCCULimit: "E_CCU_LIMIT", CodeAccessDenied: "E_ACCESS_DENIED",
	CodeInvalidTicket: "E_INVALID_TICKET", CodeIO: "E_IO", CodeInternal: "E_INTERNAL",
}

// String returns the stable name, e.g. "E_TOO_LARGE".
func (c Code) String() string {
	if n, ok := codeNames[c]; ok {
		return n
	}
	return "E_UNKNOWN"
}

// CodeFromText parses the "E_NAME:" prefix of an SDK error text (e.g. a ConnectFailed reason).
// Returns CodeInternal when there is no known prefix.
func CodeFromText(text string) Code {
	name := strings.TrimSpace(strings.SplitN(text, ":", 2)[0])
	for c, n := range codeNames {
		if n == name {
			return c
		}
	}
	return CodeInternal
}

// Error is the error type returned by this package.
type Error struct {
	Code    Code   // stable error code
	Message string // English text "E_NAME: description"
}

func (e *Error) Error() string { return "peerova: " + e.Message }

// CodeOf returns the Code of err (CodeOK for nil, CodeInternal for errors not from this package).
func CodeOf(err error) Code {
	if err == nil {
		return CodeOK
	}
	var e *Error
	if errors.As(err, &e) {
		return e.Code
	}
	return CodeInternal
}

// ErrorCode returns the code of a ConnectFailed / Disconnected reason (Data is English text
// "E_TIMEOUT: ..."), or CodeOK for other events.
func (e Event) ErrorCode() Code {
	if e.Type == ConnectFailed || e.Type == Disconnected {
		return CodeFromText(string(e.Data))
	}
	return CodeOK
}

// ErrClosed is returned by Send after Close.
var ErrClosed error = &Error{Code: CodeClosed, Message: "E_CLOSED: node is closed"}

// lastError reads the native thread-local error. The caller must hold runtime.LockOSThread across the
// failing call and this one, otherwise the goroutine may have moved to another OS thread.
func lastError() error {
	code := Code(C.peerova_last_error_code())
	msg := ""
	if p := C.peerova_last_error(); p != nil {
		msg = C.GoString(p)
	}
	if msg == "" {
		msg = "E_INTERNAL: unknown error"
	}
	if code == CodeOK {
		code = CodeFromText(msg)
	}
	return &Error{Code: code, Message: msg}
}

// Version returns the native library version string.
func Version() string { return C.GoString(C.peerova_version()) }

func cstr(s string) *C.char {
	if s == "" {
		return nil
	}
	return C.CString(s)
}

type node struct {
	mu sync.RWMutex // guards h against Close racing other calls
	h  *C.peerova_node
}

func (n *node) poll(ms uint32, wait bool) (Event, bool) {
	n.mu.RLock()
	defer n.mu.RUnlock()
	if n.h == nil {
		return Event{}, false
	}
	var ev C.peerova_event
	var got C.int32_t
	if wait {
		got = C.peerova_poll_wait(n.h, &ev, C.uint32_t(ms))
	} else {
		got = C.peerova_poll(n.h, &ev)
	}
	if got == 0 {
		return Event{}, false
	}
	var data []byte
	if ev.len > 0 && ev.data != nil {
		data = C.GoBytes(unsafe.Pointer(ev.data), C.int(ev.len)) // copy: valid only until next poll
	} else {
		data = []byte{}
	}
	return Event{EventType(ev._type), uint32(ev.peer), uint8(ev.channel), ev.reliable != 0, data}, true
}

// Poll returns the next pending event without blocking. ok is false when no event is pending or the
// node is closed. Typical game-loop use: `for ev, ok := n.Poll(); ok; ev, ok = n.Poll() { ... }`.
func (n *node) Poll() (Event, bool) { return n.poll(0, false) }

// PollWait waits up to timeout for an event; ok is false on timeout or when the node is closed.
// A timeout <= 0 behaves like Poll. Blocks the calling goroutine (and its OS thread) while waiting.
func (n *node) PollWait(timeout time.Duration) (Event, bool) {
	ms := timeout.Milliseconds()
	if ms <= 0 {
		return n.poll(0, false)
	}
	if ms > 0xFFFFFFFF {
		ms = 0xFFFFFFFF
	}
	return n.poll(uint32(ms), true)
}

// Send sends data to peer on channel (0..255).
//
// reliable=true: delivered and ordered per channel, may be large — chat, inventory, RPCs, file chunks.
// reliable=false: one datagram, lowest latency, may be lost or reordered, at most MaxUnreliablePayload
// bytes — positions, inputs, voice-like realtime state. Channels never block each other.
//
// Returns ErrClosed after Close, or the native error (unknown peer, oversized unreliable payload, ...).
func (n *node) Send(peer uint32, data []byte, channel uint8, reliable bool) error {
	n.mu.RLock()
	defer n.mu.RUnlock()
	if n.h == nil {
		return ErrClosed
	}
	var p *C.uint8_t
	if len(data) > 0 {
		p = (*C.uint8_t)(unsafe.Pointer(&data[0])) // cgo rules: Go memory without Go pointers may be passed
	}
	rel := C.int32_t(0)
	if reliable {
		rel = 1
	}
	runtime.LockOSThread() // keep the thread-local native error readable
	defer runtime.UnlockOSThread()
	if C.peerova_send(n.h, C.uint32_t(peer), C.uint8_t(channel), p, C.uint32_t(len(data)), rel) != 0 {
		return lastError()
	}
	return nil
}

// Disconnect closes the connection to peer; a Disconnected event follows. Unknown ids are ignored.
func (n *node) Disconnect(peer uint32) {
	n.mu.RLock()
	defer n.mu.RUnlock()
	if n.h != nil {
		C.peerova_disconnect(n.h, C.uint32_t(peer))
	}
}

// PeerCount returns the number of connected peers (client: 0 or 1); 0 after Close.
func (n *node) PeerCount() uint32 {
	n.mu.RLock()
	defer n.mu.RUnlock()
	if n.h == nil {
		return 0
	}
	return uint32(C.peerova_peer_count(n.h))
}

// Close disconnects all peers and frees the native node. Safe to call more than once; always returns nil.
func (n *node) Close() error {
	n.mu.Lock()
	defer n.mu.Unlock()
	if n.h != nil {
		C.peerova_free(n.h)
		n.h = nil
	}
	return nil
}

// Server is a server node: it accepts clients that connect with its ticket or connect code.
// Its methods (Poll, PollWait, Send, Disconnect, PeerCount, Close) are described on the shared node type.
type Server struct {
	node
	ticket string
}

// Client is a client node: one connection to one server. Same methods as Server, minus Ticket/RoomCode.
type Client struct{ node }

// StartServer starts a server. It blocks until the ticket is ready (about 3 s at most).
//
// configJSON may be "" for defaults, e.g. `{"port":47000,"key_file":"server.key","license":"PVA-...","token":"secret"}`.
// Keys: port, key_file, license, max_ccu, token, upnp, private, hide_ip, app, share (see the README).
// "hide_ip": true hides the server IP: players never learn its real address (DDoS protection); clients
// connect through the automatic encrypted route, with slightly higher latency.
// Returns the native error if the server cannot start (port in use, bad config, invalid license, ...).
func StartServer(configJSON string) (*Server, error) {
	cfg := cstr(configJSON)
	defer C.free(unsafe.Pointer(cfg))
	runtime.LockOSThread()
	defer runtime.UnlockOSThread()
	h := C.peerova_server_start(cfg)
	if h == nil {
		return nil, lastError()
	}
	s := &Server{node: node{h: h}}
	if need := C.peerova_ticket(h, nil, 0); need > 0 {
		buf := (*C.char)(C.malloc(C.size_t(need)))
		C.peerova_ticket(h, buf, need)
		s.ticket = C.GoString(buf)
		C.free(unsafe.Pointer(buf))
	}
	return s, nil
}

// Ticket returns the ticket ("pvt_...") clients pass to Connect. It is stable for the same key_file and
// safe to share with users.
func (s *Server) Ticket() string { return s.ticket }

// RoomCode returns the 6-character connect code (e.g. "K7Q2XD") assigned by the Peerova website, a short
// alternative to the ticket. It is only available when the server was started with "app" and "share" in
// its config, and only once the website has registered it (shortly after start): call it periodically
// until ok is true. ok is false if there is no code yet or the server is closed.
func (s *Server) RoomCode() (code string, ok bool) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	if s.h == nil {
		return "", false
	}
	need := C.peerova_room_code(s.h, nil, 0)
	if need <= 0 {
		return "", false
	}
	buf := (*C.char)(C.malloc(C.size_t(need)))
	defer C.free(unsafe.Pointer(buf))
	if C.peerova_room_code(s.h, buf, need) <= 0 {
		return "", false
	}
	return C.GoString(buf), true
}

// Connect starts connecting and returns immediately when given a ticket (with a 6-character connect code
// it first looks the code up on the Peerova website, an HTTPS request). Then wait for a Connected (success) or ConnectFailed
// (with reason) event via Poll / PollWait. Connections are automatic: direct first, with an automatic
// encrypted fallback route when networks block direct connections.
//
// ticket is the server's ticket ("pvt_...") or its 6-character connect code. configJSON may be "",
// e.g. `{"token":"secret","timeout_ms":20000,"key_file":"<appdata>/<game>/peerova-client.key"}`.
// "key_file" (optional) is the client's identity key, created if missing: keep it (in the user's app-data
// folder, see os.UserConfigDir) so the player keeps the same identity and the same stable address on the
// server across runs; without it every run uses a new random key. Bans by address only hold while the player
// keeps the file — accounts/tokens are the robust way. Returns an error for an empty or malformed ticket or
// an unknown/unreachable connect code.
func Connect(ticket, configJSON string) (*Client, error) {
	if ticket == "" {
		return nil, &Error{Code: CodeInvalidArg, Message: "E_INVALID_ARG: empty ticket"}
	}
	t := C.CString(ticket)
	defer C.free(unsafe.Pointer(t))
	cfg := cstr(configJSON)
	defer C.free(unsafe.Pointer(cfg))
	runtime.LockOSThread()
	defer runtime.UnlockOSThread()
	h := C.peerova_client_connect(t, cfg)
	if h == nil {
		return nil, lastError()
	}
	return &Client{node{h: h}}, nil
}
