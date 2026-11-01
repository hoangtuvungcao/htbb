-- How to run (LuaJIT, from bindings/lua):
--   luajit examples/chat.lua server [keyfile]              -- prints the ticket
--   luajit examples/chat.lua client <ticket|code> [seconds] -- type lines to chat (POSIX), /quit to leave
-- No `luajit` binary? test/run_luajit.sh shows how to embed libluajit (examples/run_chat.sh does it).
--
-- Tiny chat + position-sync demo in game-loop style: every frame we drain events with a short
-- poll, send our position unreliably on channel 1 at 20 Hz, and send chat lines reliably on channel 0.
local dir = ((arg and arg[0]) or "./"):match("^(.*[/\\])") or "./"
package.path = dir .. "../?.lua;" .. package.path
local peerova = require "peerova"
local ffi = require "ffi"

local CHAT, POSITION = 0, 1

-- wall clock in seconds + non-blocking stdin check (POSIX)
local now, stdin_ready
if ffi.os == "Windows" then
  ffi.cdef "unsigned long long GetTickCount64(void);"
  now = function() return tonumber(ffi.C.GetTickCount64()) / 1000 end
  stdin_ready = function() return false end -- console chat input not wired up on Windows
else
  ffi.cdef [[
    struct pv_timeval { long tv_sec; long tv_usec; };
    int gettimeofday(struct pv_timeval* tv, void* tz);
    struct pv_pollfd { int fd; short events; short revents; };
    int poll(struct pv_pollfd* fds, unsigned long nfds, int timeout);
  ]]
  local tv, pfd = ffi.new "struct pv_timeval", ffi.new("struct pv_pollfd", { 0, 1, 0 }) -- POLLIN
  now = function() ffi.C.gettimeofday(tv, nil); return tonumber(tv.tv_sec) + tonumber(tv.tv_usec) / 1e6 end
  stdin_ready = function() return ffi.C.poll(pfd, 1, 0) > 0 end
end

local function run_server(key_file)
  local srv = peerova.server { port = 0, key_file = key_file } -- blocks up to ~3 s
  print("ticket: " .. srv.ticket)
  print("run:    luajit examples/chat.lua client " .. srv.ticket)
  io.stdout:flush()
  local peers = {}
  while true do -- stop with Ctrl+C
    local ev = srv:poll(0.05)
    if ev then
      if ev.type == "connected" then
        peers[ev.peer] = true
        print(("peer %d joined (%d online)"):format(ev.peer, srv:peer_count()))
      elseif ev.type == "disconnected" then
        peers[ev.peer] = nil
        print(("peer %d left: %s"):format(ev.peer, ev.data))
      elseif ev.type == "message" then
        if ev.channel == CHAT then print(("[%d] %s"):format(ev.peer, ev.data)) end
        for p in pairs(peers) do -- forward to everyone else, same delivery mode
          if p ~= ev.peer then
            local ok, err = pcall(srv.send, srv, p, ev.data, { channel = ev.channel, reliable = ev.reliable })
            if not ok then io.stderr:write(err, "\n") end
          end
        end
      end
      io.stdout:flush()
    end
  end
end

local function run_client(ticket, seconds)
  local cli = peerova.client(ticket) -- returns immediately (a connect code is looked up first)
  local start, next_move = now(), 0
  local server, sent, received, why = nil, 0, 0, "time is up"
  while not seconds or now() - start < seconds do
    -- 1. drain network events; the 10 ms wait doubles as the frame delay
    local ev = cli:poll(0.01)
    while ev do
      if ev.type == "connected" then
        server = ev.peer
        print("connected; type a line to chat, /quit to leave")
        cli:send(server, "hello from luajit", { channel = CHAT, reliable = true })
      elseif ev.type == "connect_failed" then
        why = "connect failed: " .. ev.data; goto done
      elseif ev.type == "disconnected" then
        why = "disconnected: " .. ev.data; goto done
      elseif ev.channel == CHAT then
        print("chat: " .. ev.data)
      elseif ev.channel == POSITION and #ev.data == 8 then
        received = received + 1
        if received % 20 == 1 then
          local p = ffi.cast("const float*", ev.data)
          print(("other player at (%.2f, %.2f)"):format(p[0], p[1]))
        end
      end
      ev = cli:poll()
    end
    -- 2. 20 Hz position: small, frequent, superseded by the next one -> unreliable
    local t = now()
    if server and t >= next_move then
      local pos = ffi.new("float[2]", math.cos(t - start), math.sin(t - start))
      cli:send(server, ffi.string(pos, 8), { channel = POSITION, reliable = false })
      sent, next_move = sent + 1, t + 0.05
    end
    -- 3. chat input
    if stdin_ready() then
      local line = io.read "*l"
      if line == nil or line == "/quit" then why = "quit"; break end
      if server and line ~= "" then cli:send(server, line, { channel = CHAT, reliable = true }) end
    end
    io.stdout:flush()
  end
  ::done::
  cli:close()
  print(("client stopped (%s): sent %d positions, received %d"):format(why, sent, received))
end

local mode = arg and arg[1]
if mode == "server" then
  run_server(arg[2] or (os.getenv "TMPDIR" or "/tmp") .. "/peerova-chat-example.key")
elseif mode == "client" and arg[2] then
  run_client(arg[2], tonumber(arg[3]))
else
  io.stderr:write "usage: chat.lua server [keyfile] | chat.lua client <ticket|code> [seconds]\n"
  os.exit(2)
end
