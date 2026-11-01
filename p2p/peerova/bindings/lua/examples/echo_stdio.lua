-- How to run (plain Lua 5.1–5.4, POSIX, from bindings/lua):
--   lua examples/echo_stdio.lua server [keyfile]          -- prints the ticket, echoes every message
--   lua examples/echo_stdio.lua client <ticket|code> [n]  -- sends n chat lines (default 3), prints echoes
-- Uses the `peerova stdio` sidecar (peerova_stdio.lua). Its poll() blocks until the next event, so
-- the program is event-driven: it reacts to each event instead of running a frame loop.
local dir = ((arg and arg[0]) or "./"):match("^(.*[/\\])") or "./"
package.path = dir .. "../?.lua;" .. package.path
local peerova = require "peerova_stdio"

local mode = arg[1]
if mode == "server" then
  local srv = peerova.server { port = 0, key_file = arg[2] or (os.getenv "TMPDIR" or "/tmp") .. "/peerova-echo-example.key" }
  print("ticket: " .. srv.ticket)
  print("run:    lua examples/echo_stdio.lua client " .. srv.ticket)
  io.stdout:flush()
  for ev in srv:events() do -- stop with Ctrl+C
    if ev.type == "connected" then
      print("peer " .. ev.peer .. " joined")
    elseif ev.type == "disconnected" then
      print("peer " .. ev.peer .. " left: " .. ev.data)
    elseif ev.type == "message" then
      print(("[%d] ch%d %s"):format(ev.peer, ev.channel, ev.data))
      srv:send(ev.peer, "echo: " .. ev.data, { channel = ev.channel, reliable = ev.reliable })
    end
    io.stdout:flush()
  end
  srv:close()
elseif mode == "client" and arg[2] then
  local n = tonumber(arg[3]) or 3
  local cli = peerova.client(arg[2])
  local got = 0
  for ev in cli:events() do
    if ev.type == "connected" then
      for i = 1, n do cli:send(ev.peer, "chat line " .. i, { channel = 0, reliable = true }) end
      -- one realtime update; may be lost on a real network, so we don't wait for its echo
      cli:send(ev.peer, "position 1.0,2.0", { channel = 1, reliable = false })
    elseif ev.type == "connect_failed" then
      print("connect failed: " .. ev.data); break
    elseif ev.type == "disconnected" then
      print("disconnected: " .. ev.data); break
    elseif ev.type == "message" then
      print(ev.data)
      if ev.reliable then got = got + 1 end
      if got == n then break end
    end
  end
  cli:close()
  print(("client done: %d/%d echoes"):format(got, n))
else
  io.stderr:write "usage: echo_stdio.lua server [keyfile] | echo_stdio.lua client <ticket|code> [n]\n"
  os.exit(2)
end
