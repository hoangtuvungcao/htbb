#!/usr/bin/env perl
# How to run:  perl demo.pl server [seconds]   then   perl demo.pl client <TICKET|CODE> [seconds]
#
# Peerova position-sync + chat demo in plain Perl 5 (core modules only, no native binding):
# it spawns the `peerova stdio` sidecar and speaks the JSON-lines protocol from ../PROTOCOL.md.
#
#   server : prints its ticket, forwards every chat line to all other clients, prints received positions.
#   client : connects with the ticket (or a 6-character connect code), sends its position 20 times per
#            second on channel 1 (unreliable) and every line you type as chat on channel 0 (reliable).
#   seconds: optional run time; without it the demo runs until Ctrl+C / end of input.
#
# Sidecar binary: $PEEROVA_BIN, else ../../../target/release/peerova next to this file, else `peerova` on PATH.
# Server identity key: $PEEROVA_KEY_FILE, else ./server.key (keep it secret, do not commit it).
use strict;
use warnings;
use IPC::Open2 qw(open2);
use IO::Select;
use JSON::PP qw(encode_json decode_json);
use MIME::Base64 qw(encode_base64 decode_base64);
use Time::HiRes qw(time);
use File::Basename qw(dirname);
use POSIX qw(WNOHANG);
$| = 1;   # flush stdout per line

my $CHAT = 0;   # reliable, ordered: chat lines
my $POS  = 1;   # unreliable, latest-wins: positions (max 1180 bytes per datagram)

my ($mode, @args) = @ARGV;
my ($ticket, $seconds);
if (($mode // '') eq 'server')    { $seconds = $args[0]; }
elsif (($mode // '') eq 'client') { ($ticket, $seconds) = @args; die "usage: $0 client <TICKET|CODE> [seconds]\n" unless $ticket; }
else { die "usage: $0 server [seconds] | client <TICKET|CODE> [seconds]\n"; }

# ---- base64url without padding (RFC 4648 section 5)
sub b64e { my $s = encode_base64($_[0], ''); $s =~ tr{+/}{-_}; $s =~ s/=+$//; $s }
sub b64d { my $s = $_[0]; $s =~ tr{-_}{+/}; $s .= '=' x (-length($s) % 4); decode_base64($s) }

# ---- locate and spawn the sidecar
sub sidecar_bin {
    return $ENV{PEEROVA_BIN} if $ENV{PEEROVA_BIN};
    my $dev = dirname(__FILE__) . '/../../../target/release/peerova';
    return -x $dev ? $dev : 'peerova';
}
my @cmd = $mode eq 'server'
    ? (sidecar_bin(), 'stdio', 'server', encode_json({ port => 0, key_file => $ENV{PEEROVA_KEY_FILE} // 'server.key' }))
    : (sidecar_bin(), 'stdio', 'client', $ticket, encode_json({ timeout_ms => 20000 }));
my ($from_pv, $to_pv);
my $pid = open2($from_pv, $to_pv, @cmd);   # stderr is inherited: diagnostics show up in the terminal
$to_pv->autoflush(1);

sub command { print {$to_pv} encode_json($_[0]), "\n" }
sub send_msg {   # send_msg(peer, channel, reliable, bytes)
    command({ cmd => 'send', peer => $_[0] + 0, channel => $_[1], reliable => $_[2] ? JSON::PP::true : JSON::PP::false,
              data => b64e($_[3]) });
}

# Buffered line reader over the sidecar's stdout (works with IO::Select, unlike <$fh>).
my $buf = '';
my $eof = 0;
sub read_lines {
    my $n = sysread($from_pv, $buf, 65536, length $buf);
    $eof = 1 unless $n;
    my @lines;
    push @lines, $1 while $buf =~ s/^([^\n]*)\n//;
    return @lines;
}

# The first line must be "ready"; EOF before it means startup failed (error is on stderr, exit code 1).
my @pending;
@pending = grep { length } read_lines() until @pending || $eof;
my $first = shift @pending;
my $ready = $first ? decode_json($first) : undef;
unless ($ready && $ready->{event} eq 'ready') { waitpid($pid, 0); die "sidecar failed to start (exit " . ($? >> 8) . ")\n"; }
if ($mode eq 'server') { print "ticket: $ready->{ticket}\n"; } else { print "connecting...\n"; }

my %peers;               # peer id => 1 (server: clients; client: the server)
my %positions;           # server: peer id => [x, y, count]
my ($x, $y, $t) = (0, 0, 0);
my $stop = 0;
$SIG{INT} = $SIG{TERM} = sub { $stop = 1 };
my $deadline = $seconds ? time + $seconds : undef;
my $next_pos = time;
my $next_report = time + 1;
my $sel = IO::Select->new($from_pv);
my $stdin_open = -t STDIN || !defined $seconds;   # scripted runs (seconds given, stdin not a tty) ignore stdin
$sel->add(\*STDIN) if $stdin_open;
my $said_hello = 0;

sub handle_event {
    my $ev = decode_json($_[0]);
    my $e = $ev->{event};
    if ($e eq 'connected') {
        $peers{$ev->{peer}} = 1;
        print "* peer $ev->{peer} connected\n";
        if ($mode eq 'client' && !$said_hello) { send_msg($ev->{peer}, $CHAT, 1, "hello from perl client $$"); $said_hello = 1; }
    } elsif ($e eq 'disconnected') {
        delete $peers{$ev->{peer}};
        delete $positions{$ev->{peer}};
        print "* peer $ev->{peer} left: $ev->{reason}\n";
        $stop = 1 if $mode eq 'client';
    } elsif ($e eq 'connect_failed') {
        print "* connect failed: $ev->{reason}\n";
        $stop = 2;
    } elsif ($e eq 'error') {
        print "* error: $ev->{message}\n";   # non-fatal: a rejected command
    } elsif ($e eq 'message') {
        my $data = b64d($ev->{data});
        if ($ev->{channel} == $POS && !$ev->{reliable}) {
            my ($px, $py) = unpack 'f< f<', $data;   # two little-endian float32
            my $p = $positions{$ev->{peer}} //= [0, 0, 0];
            @$p = ($px, $py, $p->[2] + 1);
        } elsif ($ev->{channel} == $CHAT) {
            print "<$ev->{peer}> $data\n";
            if ($mode eq 'server') {   # forward the chat line to every other client
                send_msg($_, $CHAT, 1, "<$ev->{peer}> $data") for grep { $_ != $ev->{peer} } keys %peers;
            }
        }
    }
}

handle_event($_) for @pending;   # events that arrived together with "ready"
while (!$stop && !$eof) {
    my $now = time;
    last if $deadline && $now >= $deadline;
    for my $fh ($sel->can_read(0.01)) {
        if ($fh == $from_pv) {
            handle_event($_) for grep { length } read_lines();
        } else {
            my $line = <STDIN>;
            if (!defined $line) { $sel->remove(\*STDIN); $stop = 1; last; }   # end of input: quit
            chomp $line;
            send_msg($_, $CHAT, 1, $line) for keys %peers;
        }
    }
    $now = time;
    if ($mode eq 'client' && $now >= $next_pos) {   # 20 Hz position updates, unreliable
        $next_pos += 0.05;
        $t += 0.05;
        ($x, $y) = (10 * cos($t), 10 * sin($t));
        send_msg($_, $POS, 0, pack('f< f<', $x, $y)) for keys %peers;
    }
    if ($mode eq 'server' && $now >= $next_report) {
        $next_report += 1;
        printf "  pos %s: (%.2f, %.2f), %d updates\n", $_, @{ $positions{$_} } for sort keys %positions;
    }
}

# Clean shutdown: ask the sidecar to quit, wait up to 3 s, then kill it.
command({ cmd => 'quit' }) unless $eof;
close $to_pv;
my $until = time + 3;
while (waitpid($pid, WNOHANG) == 0) {
    if (time > $until) { kill 'KILL', $pid; waitpid($pid, 0); last; }
    select(undef, undef, undef, 0.05);
}
print "bye\n";
exit($stop == 2 ? 1 : 0);
