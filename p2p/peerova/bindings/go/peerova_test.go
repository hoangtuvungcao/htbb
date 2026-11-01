package peerova

import (
	"fmt"
	"path/filepath"
	"testing"
	"time"
)

func TestRoundtrip(t *testing.T) {
	key := filepath.Join(t.TempDir(), "go.key")
	srv, err := StartServer(fmt.Sprintf(`{"port":0,"upnp":false,"key_file":%q}`, key))
	if err != nil {
		t.Fatal(err)
	}
	defer srv.Close()
	cli, err := Connect(srv.Ticket(), "")
	if err != nil {
		t.Fatal(err)
	}
	defer cli.Close()

	var peer uint32
	deadline := time.Now().Add(10 * time.Second)
	for peer == 0 && time.Now().Before(deadline) {
		ev, ok := cli.PollWait(500 * time.Millisecond)
		if !ok {
			continue
		}
		if ev.Type == ConnectFailed {
			t.Fatalf("connect failed: %s", ev.Data)
		}
		if ev.Type == Connected {
			peer = ev.Peer
		}
	}
	if peer == 0 {
		t.Fatal("client did not connect")
	}

	const n = 100
	start := time.Now()
	for i := 0; i < n; i++ {
		if err := cli.Send(peer, []byte(fmt.Sprintf("msg %d", i)), 1, true); err != nil {
			t.Fatal(err)
		}
	}
	if err := cli.Send(peer, []byte("realtime"), 2, false); err != nil {
		t.Fatal(err)
	}

	got, unreliable, sp := 0, false, uint32(0)
	deadline = time.Now().Add(5 * time.Second)
	for (got < n || !unreliable) && time.Now().Before(deadline) {
		e, ok := srv.PollWait(200 * time.Millisecond)
		if !ok {
			continue
		}
		switch {
		case e.Type == Connected:
			sp = e.Peer
		case e.Type == Message && e.Channel == 1:
			if !e.Reliable || string(e.Data) != fmt.Sprintf("msg %d", got) {
				t.Fatalf("out of order at %d: %q", got, e.Data)
			}
			got++
		case e.Type == Message && e.Channel == 2:
			unreliable = !e.Reliable && string(e.Data) == "realtime"
		}
	}
	if got != n {
		t.Fatalf("got %d/%d", got, n)
	}
	if !unreliable {
		t.Fatal("unreliable datagram not received on loopback")
	}
	if err := srv.Send(sp, []byte("pong"), 0, true); err != nil {
		t.Fatal(err)
	}
	var reply *Event
	deadline = time.Now().Add(5 * time.Second)
	for reply == nil && time.Now().Before(deadline) {
		if e, ok := cli.PollWait(200 * time.Millisecond); ok && e.Type == Message {
			reply = &e
		}
	}
	if reply == nil || string(reply.Data) != "pong" {
		t.Fatal("no pong")
	}
	if err := cli.Send(peer, make([]byte, MaxUnreliablePayload+100), 3, false); err == nil {
		t.Error("oversized unreliable send was accepted")
	} else {
		t.Logf("oversized unreliable rejected: %v", err)
	}
	t.Logf("peerova %s: %d reliable in order + 1 unreliable + pong in %v, server peers=%d", Version(), n, time.Since(start), srv.PeerCount())
	cli.Close()
	if err := cli.Send(peer, []byte("x"), 0, true); err != ErrClosed {
		t.Fatalf("send after close: %v", err)
	}
}
