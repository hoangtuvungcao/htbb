-- LuaJIT test. Run with a LuaJIT interpreter:  luajit test/test_peerova.lua
-- (here: test/run_luajit.sh, which embeds libluajit via luajit_host.c because no luajit binary is installed)
local sdir = ((arg and arg[0]) or debug.getinfo(1, "S").source:sub(2)):match("^(.*[/\\])") or "./"
package.path = sdir .. "../?.lua;" .. package.path
local peerova = require "peerova"

local N = 200
local key = os.tmpname()
os.remove(key)
local srv = peerova.server { port = 0, upnp = false, key_file = key }
assert(srv.ticket:sub(1, 4) == "pvt_", srv.ticket)
local cli = peerova.client(srv.ticket)
local ev = cli:poll(10)
assert(ev and ev.type == "connected", "client not connected")
for i = 0, N - 1 do cli:send(ev.peer, "msg " .. i, { channel = 1, reliable = true }) end
cli:send(ev.peer, "realtime", { channel = 2, reliable = false })
local got, sp, unrel = {}, nil, 0
for e in srv:events(3) do
  if e.type == "connected" then sp = e.peer
  elseif e.type == "message" and e.reliable then
    assert(e.channel == 1)
    got[#got + 1] = e.data
    if #got == N then break end
  elseif e.type == "message" then unrel = unrel + 1 end
end
assert(#got == N, "got " .. #got)
for i = 0, N - 1 do assert(got[i + 1] == "msg " .. i, "out of order at " .. i) end
-- the datagram may land after the last reliable message (on loopback it is never lost)
while unrel == 0 do
  local e = srv:poll(1)
  if not e then break end
  if e.type == "message" and not e.reliable and e.data == "realtime" then unrel = unrel + 1 end
end
assert(unrel == 1, "unreliable datagram not received")
srv:send(sp, "pong\0\255")
local m
for e in cli:events(5) do
  if e.type == "message" and e.reliable then m = e break end
end
assert(m and m.data == "pong\0\255", "reply")
print(("luajit OK: peerova %s, %d reliable msgs in order, unreliable received=%d, binary reply ok, peers=%d")
  :format(peerova.version(), N, unrel, srv:peer_count()))
cli:close()
srv:close()
os.remove(key)
