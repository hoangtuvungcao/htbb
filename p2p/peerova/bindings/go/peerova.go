// Package peerova is a cgo binding for the peerova C ABI: direct P2P networking (no relay, E2E encrypted).
//
//	srv, err := peerova.StartServer(`{"port":0}`)
//	cli, err := peerova.Connect(srv.Ticket(), "")
//	ev, ok := cli.PollWait(10 * time.Second) // ev.Type == peerova.Connected
//	cli.Send(ev.Peer, []byte("hi"), 0, true)
//
// Each node is internally synchronized, so Send/Poll may be called from any goroutine.
// Do not call Close concurrently with other calls on the same node.
//
// Linking: by default links ../../target/release/libpeerova (repo layout) with an rpath.
// Override with CGO_CFLAGS / CGO_LDFLAGS, e.g.
//
//	CGO_CFLAGS=-I/opt/peerova/include CGO_LDFLAGS="-L/opt/peerova/lib -lpeerova" go build
package peerova

/*
#cgo CFLAGS: -I${SRCDIR}/../../crates/peerova-ffi/include
#cgo LDFLAGS: -L${SRCDIR}/../../target/release -lpeerova -Wl,-rpath,${SRCDIR}/../../target/release
#include <stdlib.h>
#include "peerova.h"
*/
import "C"

import (
	"errors"
	"sync"
	"time"
	"unsafe"
)

// EventType values mirror PEEROVA_EVENT_*.
type EventType int32

const (
	Connected     EventType = 1
	Disconnected  EventType = 2
	Message       EventType = 3
	ConnectFailed EventType = 4
)

// MaxUnreliablePayload is the largest payload accepted by an unreliable Send.
const MaxUnreliablePayload = 1180

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

// Event is one network event. Data is a Go copy (safe to keep).
type Event struct {
	Type     EventType
	Peer     uint32
	Channel  uint8
	Reliable bool
	Data     []byte // payload, or UTF-8 reason for ConnectFailed
}

// ErrClosed is returned when using a closed node.
var ErrClosed = errors.New("peerova: node is closed")

func lastError() error {
	if p := C.peerova_last_error(); p != nil {
		if s := C.GoString(p); s != "" {
			return errors.New("peerova: " + s)
		}
	}
	return errors.New("peerova: unknown error")
}

// Version returns the native library version.
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

// Poll returns the next pending event without blocking.
func (n *node) Poll() (Event, bool) { return n.poll(0, false) }

// PollWait waits up to timeout for an event.
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

// Send sends data to peer on channel (0..255). reliable=false: datagram, max 1180 bytes.
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
	if C.peerova_send(n.h, C.uint32_t(peer), C.uint8_t(channel), p, C.uint32_t(len(data)), rel) != 0 {
		return lastError()
	}
	return nil
}

// Disconnect drops a peer.
func (n *node) Disconnect(peer uint32) {
	n.mu.RLock()
	defer n.mu.RUnlock()
	if n.h != nil {
		C.peerova_disconnect(n.h, C.uint32_t(peer))
	}
}

// PeerCount returns the number of connected peers.
func (n *node) PeerCount() uint32 {
	n.mu.RLock()
	defer n.mu.RUnlock()
	if n.h == nil {
		return 0
	}
	return uint32(C.peerova_peer_count(n.h))
}

// Close frees the native node. Safe to call more than once.
func (n *node) Close() error {
	n.mu.Lock()
	defer n.mu.Unlock()
	if n.h != nil {
		C.peerova_free(n.h)
		n.h = nil
	}
	return nil
}

// Server is a peerova server node.
type Server struct {
	node
	ticket string
}

// Client is a peerova client node.
type Client struct{ node }

// StartServer starts a server (configJSON may be ""); blocks until the ticket is ready (about 3 s max).
func StartServer(configJSON string) (*Server, error) {
	cfg := cstr(configJSON)
	defer C.free(unsafe.Pointer(cfg))
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

// Ticket returns the connection ticket to share with clients.
func (s *Server) Ticket() string { return s.ticket }

// Connect starts connecting to a ticket (configJSON may be ""). Returns immediately;
// wait for Connected / ConnectFailed via Poll.
func Connect(ticket, configJSON string) (*Client, error) {
	if ticket == "" {
		return nil, errors.New("peerova: empty ticket")
	}
	t := C.CString(ticket)
	defer C.free(unsafe.Pointer(t))
	cfg := cstr(configJSON)
	defer C.free(unsafe.Pointer(cfg))
	h := C.peerova_client_connect(t, cfg)
	if h == nil {
		return nil, lastError()
	}
	return &Client{node{h: h}}, nil
}
