// How to run (from bindings/go):
//
//	go run ./examples/chat server [port] [key_file]
//	go run ./examples/chat client <TICKET or CONNECT CODE>
//
// Tiny chat + position-sync demo. The server prints its ticket, forwards chat lines (reliable, channel 0)
// and positions (unreliable, channel 1) to the other clients. The client sends its position 20 times per
// second and every line typed on stdin as a chat message; "/quit", end of input or Ctrl+C disconnects
// cleanly.
package main

import (
	"bufio"
	"encoding/binary"
	"fmt"
	"math"
	"os"
	"os/signal"
	"strconv"
	"syscall"
	"time"

	"github.com/peerova/peerova-go"
)

const (
	chChat     = 0 // reliable: ordered, never lost
	chPosition = 1 // unreliable: latest state wins, may be dropped
)

func main() {
	var err error
	switch {
	case len(os.Args) >= 2 && os.Args[1] == "server":
		port, keyFile := 47000, "server.key"
		if len(os.Args) > 2 {
			port, _ = strconv.Atoi(os.Args[2])
		}
		if len(os.Args) > 3 {
			keyFile = os.Args[3]
		}
		err = server(port, keyFile)
	case len(os.Args) >= 3 && os.Args[1] == "client":
		err = client(os.Args[2])
	default:
		err = fmt.Errorf("usage: chat server [port] [key_file] | chat client <TICKET or CODE>")
	}
	if err != nil {
		fmt.Fprintln(os.Stderr, "error:", err)
		os.Exit(1)
	}
}

func interrupted() <-chan os.Signal {
	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Interrupt, syscall.SIGTERM)
	return c
}

func server(port int, keyFile string) error {
	srv, err := peerova.StartServer(fmt.Sprintf(`{"port":%d,"key_file":%q}`, port, keyFile))
	if err != nil {
		return err
	}
	defer srv.Close() // runs after the poll loop below has returned
	fmt.Println("ticket:", srv.Ticket())

	stop := interrupted()
	peers := map[uint32]bool{}
	codeShown, positions := false, 0
	for {
		select {
		case <-stop:
			fmt.Println("server stopped")
			return nil
		default:
		}
		if code, ok := srv.RoomCode(); ok && !codeShown { // only with "app" + "share" in the config
			fmt.Println("connect code:", code)
			codeShown = true
		}
		ev, ok := srv.PollWait(100 * time.Millisecond)
		if !ok {
			continue
		}
		switch ev.Type {
		case peerova.Connected:
			peers[ev.Peer] = true
			fmt.Printf("peer %d joined (%d online)\n", ev.Peer, srv.PeerCount())
		case peerova.Disconnected:
			delete(peers, ev.Peer)
			fmt.Printf("peer %d left: %s\n", ev.Peer, ev.Data)
		case peerova.Message:
			switch ev.Channel {
			case chChat:
				line := fmt.Sprintf("peer %d: %s", ev.Peer, ev.Data)
				fmt.Println(line)
				for p := range peers {
					if p != ev.Peer {
						_ = srv.Send(p, []byte(line), chChat, true)
					}
				}
			case chPosition:
				if positions++; positions%20 == 0 && len(ev.Data) == 8 {
					x := math.Float32frombits(binary.BigEndian.Uint32(ev.Data[0:]))
					y := math.Float32frombits(binary.BigEndian.Uint32(ev.Data[4:]))
					fmt.Printf("peer %d position %.1f,%.1f (%d updates)\n", ev.Peer, x, y, positions)
				}
				for p := range peers {
					if p != ev.Peer {
						_ = srv.Send(p, ev.Data, chPosition, false)
					}
				}
			}
		}
	}
}

func client(ticketOrCode string) error {
	cli, err := peerova.Connect(ticketOrCode, `{"timeout_ms":20000}`)
	if err != nil {
		return err
	}
	defer cli.Close()

	var server uint32
	for connected := false; !connected; {
		ev, ok := cli.PollWait(500 * time.Millisecond)
		switch {
		case !ok:
		case ev.Type == peerova.ConnectFailed:
			return fmt.Errorf("connect failed: %s", ev.Data)
		case ev.Type == peerova.Connected:
			server, connected = ev.Peer, true
		}
	}
	fmt.Println("connected, type chat lines (/quit to leave)")

	// stdin on its own goroutine; it is never joined (a blocked read cannot be cancelled), so it only
	// hands lines over a channel and never touches the node itself.
	lines := make(chan string)
	go func() {
		sc := bufio.NewScanner(os.Stdin)
		for sc.Scan() {
			lines <- sc.Text()
		}
		close(lines)
	}()

	// Poll on a dedicated goroutine; the main goroutine sends. done tells it to stop before Close.
	done := make(chan struct{})
	pollerExited := make(chan struct{})
	serverGone := make(chan struct{})
	go func() {
		defer close(pollerExited)
		for {
			select {
			case <-done:
				return
			default:
			}
			ev, ok := cli.PollWait(50 * time.Millisecond)
			if !ok {
				continue
			}
			switch {
			case ev.Type == peerova.Message && ev.Channel == chChat:
				fmt.Println(string(ev.Data))
			case ev.Type == peerova.Disconnected:
				fmt.Println("server closed:", string(ev.Data))
				close(serverGone)
				return
			}
		}
	}()

	tick := time.NewTicker(50 * time.Millisecond) // ~20 Hz position updates
	defer tick.Stop()
	stop := interrupted()
	sent := 0
loop:
	for {
		select {
		case now := <-tick.C:
			t := float64(now.UnixNano()) / 1e9
			pos := make([]byte, 8)
			binary.BigEndian.PutUint32(pos[0:], math.Float32bits(float32(math.Cos(t)*10)))
			binary.BigEndian.PutUint32(pos[4:], math.Float32bits(float32(math.Sin(t)*10)))
			if cli.Send(server, pos, chPosition, false) == nil {
				sent++
			}
		case line, ok := <-lines:
			if !ok || line == "/quit" {
				break loop
			}
			if err := cli.Send(server, []byte(line), chChat, true); err != nil {
				return err
			}
		case <-serverGone:
			break loop
		case <-stop:
			break loop
		}
	}
	time.Sleep(300 * time.Millisecond) // let queued reliable messages reach the server
	close(done)
	<-pollerExited // never Close while another goroutine is polling
	cli.Disconnect(server)
	fmt.Printf("bye (%d position updates sent)\n", sent)
	return nil
}
