--- Peerova — direct peer-to-peer networking with end-to-end encryption.
--- LuaJIT FFI binding (LuaJIT 2.0/2.1: OpenResty, LÖVE, Defold, ...). For plain Lua 5.1–5.4 use
--- peerova_stdio.lua (same API, through the `peerova stdio` sidecar).
---
---   local peerova = require "peerova"
---   local srv = peerova.server{ port = 0, upnp = false }
---   print(srv.ticket)
---   local cli = peerova.client(srv.ticket)   -- or a 6-character connect code
---   local ev = cli:poll(10)                  -- ev.type == "connected"
---   cli:send(ev.peer, "hello", { channel = 0, reliable = true })
---   for e in srv:events(2) do print(e.type, e.peer, e.data) end
---   cli:close(); srv:close()
---
--- Library lookup: os.getenv("PEEROVA_LIB"), next to this file, the SDK development tree, then the
--- system loader (ffi.load("peerova")). Event data is copied into a Lua string immediately.
---
--- Errors: failing calls raise a Lua error "peerova: <message>" with the native error text
--- (peerova_last_error); catch with pcall. Threading: a node may be used from several OS threads
--- (e.g. LÖVE threads with their own Lua state) but never close it while another one polls.
---
--- @module peerova
local ffi = require "ffi"

ffi.cdef [[
typedef struct peerova_node peerova_node;
typedef struct {
    int32_t type; uint32_t peer; uint8_t channel; uint8_t reliable;
    const uint8_t* data; uint32_t len;
} peerova_event;
peerova_node* peerova_server_start(const char* config_json);
peerova_node* peerova_client_connect(const char* ticket, const char* config_json);
int32_t peerova_ticket(peerova_node* node, char* buf, int32_t buf_len);
int32_t peerova_room_code(peerova_node* node, char* buf, int32_t buf_len);
int32_t peerova_send(peerova_node* node, uint32_t peer, uint8_t channel, const uint8_t* data, uint32_t len, int32_t reliable);
int32_t peerova_poll(peerova_node* node, peerova_event* out);
int32_t peerova_poll_wait(peerova_node* node, peerova_event* out, uint32_t timeout_ms);
void peerova_disconnect(peerova_node* node, uint32_t peer);
uint32_t peerova_peer_count(peerova_node* node);
void peerova_free(peerova_node* node);
const char* peerova_last_error(void);
const char* peerova_version(void);
]]

--- Module table. Event type numbers (CONNECTED .. CONNECT_FAILED) match the C ABI; poll() returns
--- them as strings ("connected", "disconnected", "message", "connect_failed").
--- MAX_UNRELIABLE_PAYLOAD: maximum bytes of one unreliable message.
local M = {
  CONNECTED = 1, DISCONNECTED = 2, MESSAGE = 3, CONNECT_FAILED = 4,
  MAX_UNRELIABLE_PAYLOAD = 1180,
}
local TYPES = { "connected", "disconnected", "message", "connect_failed" }

local function exists(p)
  local f = p and io.open(p, "rb")
  if f then f:close() return true end
  return false
end

local function load_lib()
  local name = ffi.os == "Windows" and "peerova.dll" or (ffi.os == "OSX" and "libpeerova.dylib" or "libpeerova.so")
  local here = (debug.getinfo(1, "S").source:match("^@(.*[/\\])") or "./")
  local cands = { os.getenv "PEEROVA_LIB" or false, here .. name, here .. "../../target/release/" .. name }
  for i = 1, #cands do
    if cands[i] and exists(cands[i]) then return ffi.load(cands[i]) end
  end
  return ffi.load "peerova"
end

local C = load_lib()
--- Raw C functions (the full C ABI), for advanced use.
M.C = C

local function last_error()
  local p = C.peerova_last_error()
  return p ~= nil and ffi.string(p) or "unknown error"
end

--- Version string of the native library, e.g. "0.1.0".
--- @treturn string
function M.version() return ffi.string(C.peerova_version()) end

--- Encodes a config table as JSON: string keys with string / number / boolean values, arrays, and
--- nested tables such as `share = { name = "...", public = true }`. A string is passed through as-is.
--- @param t table|string|nil
--- @treturn string
local function to_json(t)
  if t == nil then return "{}" end
  if type(t) == "string" then return t end
  local function str(x)
    return '"' .. x:gsub('[%c"\\]', function(c) return string.format("\\u%04x", c:byte()) end) .. '"'
  end
  local function v(x)
    local tx = type(x)
    if tx == "string" then return str(x) end
    if tx == "number" or tx == "boolean" then return tostring(x) end
    if tx == "table" then
      local parts = {}
      if #x > 0 then -- array
        for i = 1, #x do parts[i] = v(x[i]) end
        return "[" .. table.concat(parts, ",") .. "]"
      end
      for k, y in pairs(x) do parts[#parts + 1] = str(tostring(k)) .. ":" .. v(y) end -- object
      return "{" .. table.concat(parts, ",") .. "}"
    end
    error("unsupported config value type " .. tx)
  end
  return v(t)
end
M.to_json = to_json

local Node = {}
Node.__index = Node

--- A node (server or client). Fields: `ticket` (servers only). Methods: send, poll, events,
--- disconnect, peer_count, room_code (servers), close.
--- @type node
local function wrap(h, is_server)
  if h == nil then error("peerova: " .. last_error(), 3) end
  h = ffi.gc(h, C.peerova_free)
  local self = setmetatable({ _h = h, _ev = ffi.new "peerova_event" }, Node)
  if is_server then
    local n = C.peerova_ticket(h, nil, 0)
    local buf = ffi.new("char[?]", n)
    C.peerova_ticket(h, buf, n)
    self.ticket = ffi.string(buf)
  end
  return self
end

--- Starts a server; blocks up to ~3 s until the ticket (node.ticket, "pvt_...") is ready.
--- @param config table|string|nil keys (all optional): port (default 47000, 0 = random), key_file
---   (default "server.key"; same key => same ticket), license ("PVA-..."; without it dev mode, max
---   3 clients), max_ccu, token (shared secret), upnp (default true), private, and for a connect
---   code app (project id) + share = { name, public, max, mode, region }
--- @return node; raises "peerova: <native error>" on failure
function M.server(config) return wrap(C.peerova_server_start(to_json(config)), true) end

--- Starts connecting; wait for a "connected" / "connect_failed" event with poll().
--- With a ticket it returns immediately; with a 6-character connect code it first looks the code up
--- on the Peerova website (blocks briefly).
--- @param ticket string server ticket ("pvt_...") or connect code
--- @param config table|string|nil `{ token = "...", timeout_ms = 20000 }`
--- @return node; raises "peerova: <native error>" for a bad ticket or unknown code
function M.client(ticket, config) return wrap(C.peerova_client_connect(ticket, to_json(config)), false) end

local function handle(self)
  if self._h == nil then error("peerova: node is closed", 3) end
  return self._h
end

--- Sends a Lua string (binary-safe). opts = { channel = 0..255, reliable = true }.
--- reliable=true: ordered, retransmitted per channel, any size (chat, RPC, file chunks).
--- reliable=false: datagram, max 1180 bytes, may be lost / reordered (positions, inputs).
--- Raises "peerova: <native error>" if rejected (unknown peer, payload too large, closed node).
function Node:send(peer, data, opts)
  local ch, rel = 0, true
  if opts then
    ch = opts.channel or 0
    if opts.reliable == false then rel = false end
  end
  if C.peerova_send(handle(self), peer, ch, data, #data, rel and 1 or 0) ~= 0 then
    error("peerova: " .. last_error(), 2)
  end
end

--- Returns the next event table { type, peer, channel, reliable, data } or nil.
--- type is "connected" | "message" | "disconnected" | "connect_failed"; data is the payload for
--- "message" and the reason text for "disconnected" / "connect_failed".
--- @tparam[opt] number timeout seconds to wait (nil/0 = non-blocking; > 0 blocks the thread)
function Node:poll(timeout)
  local h, ev = handle(self), self._ev
  local got
  if timeout and timeout > 0 then
    got = C.peerova_poll_wait(h, ev, math.floor(timeout * 1000))
  else
    got = C.peerova_poll(h, ev)
  end
  if got == 0 then return nil end
  return {
    type = TYPES[ev.type] or ev.type,
    peer = tonumber(ev.peer),
    channel = ev.channel,
    reliable = ev.reliable ~= 0,
    data = ev.len > 0 and ffi.string(ev.data, ev.len) or "", -- copy: valid only until next poll
  }
end

--- Iterator: yields events until `timeout` seconds (default 1) pass with none.
function Node:events(timeout)
  timeout = timeout or 1
  return function() return self:poll(timeout) end
end

--- Closes the connection to peer; a "disconnected" event follows. Unknown peers are ignored.
function Node:disconnect(peer) C.peerova_disconnect(handle(self), peer) end

--- Number of connected peers (server: clients; client: 0 or 1).
function Node:peer_count() return tonumber(C.peerova_peer_count(handle(self))) end

--- Server only: the 6-character connect code (e.g. "K7Q2XD") assigned by the Peerova website, or
--- nil while not (yet) available. Only servers started with `app` + `share` get one; it usually
--- appears a few seconds after start, so call again later. Clients may pass it to peerova.client.
--- @treturn string|nil
function Node:room_code()
  local h = handle(self)
  local n = C.peerova_room_code(h, nil, 0)
  if n <= 0 then return nil end
  local buf = ffi.new("char[?]", n)
  local m = C.peerova_room_code(h, buf, n)
  if m <= 0 or m > n then return nil end
  local code = ffi.string(buf)
  return code ~= "" and code or nil
end

--- Disconnects all peers and frees the node (also done by the GC). Idempotent. Never call it
--- while another thread polls this node.
function Node:close()
  if self._h ~= nil then
    C.peerova_free(ffi.gc(self._h, nil))
    self._h = nil
  end
end

return M
