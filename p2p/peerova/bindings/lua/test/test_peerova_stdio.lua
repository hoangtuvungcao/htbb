-- Plain Lua 5.1–5.4 test via the sidecar:  lua test/test_peerova_stdio.lua
local sdir = ((arg and arg[0]) or "./"):match("^(.*[/\\])") or "./"
package.path = sdir .. "../?.lua;" .. package.path
local peerova = require "peerova_stdio"

-- base64url self-check (all lengths mod 3, all byte values)
local all = {}
for i = 0, 255 do all[#all + 1] = string.char(i) end
all = table.concat(all)
for n = 0, 7 do assert(peerova.b64url_decode(peerova.b64url_encode(all:sub(1, 250 + n))) == all:sub(1, 250 + n)) end
assert(peerova.b64url_encode("hi?") == "aGk_")

local N = 200
local key = os.tmpname()
os.remove(key)
local srv = peerova.server { port = 0, upnp = false, key_file = key }
assert(srv.ticket and srv.ticket:sub(1, 4) == "pvt_", tostring(srv.ticket))
local cli = peerova.client(srv.ticket)
local ev = cli:poll()
assert(ev and ev.type == "connected", "client not connected: " .. tostring(ev and ev.type) .. " " .. tostring(ev and ev.data))
for i = 0, N - 1 do cli:send(ev.peer, "msg " .. i, { channel = 1, reliable = true }) end
cli:send(ev.peer, "realtime", { channel = 2, reliable = false })
local got, sp, unrel = {}, nil, 0
for e in srv:events() do
  if e.type == "connected" then sp = e.peer
  elseif e.type == "message" and e.reliable then
    assert(e.channel == 1)
    got[#got + 1] = e.data
    if #got == N then break end
  elseif e.type == "message" then unrel = unrel + 1 end
end
for i = 0, N - 1 do assert(got[i + 1] == "msg " .. i, "out of order at " .. i) end
srv:send(sp, "pong\0\255")
local m
for e in cli:events() do
  if e.type == "message" and e.reliable then m = e break end
end
assert(m and m.data == "pong\0\255", "reply")
print(("%s stdio OK: %d reliable msgs in order, unreliable received=%d, binary reply ok, peers=%d")
  :format(_VERSION, N, unrel, srv:peer_count()))
cli:close()
srv:close()
os.remove(key)
