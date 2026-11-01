--- peerova — direct P2P networking (no relay, E2E encrypted). LuaJIT FFI binding (LuaJIT 2.0/2.1,
--- also OpenResty, LÖVE, Defold-with-LuaJIT...). For plain Lua 5.1–5.4 see peerova_stdio.lua.
---
---   local peerova = require "peerova"
---   local srv = peerova.server{ port = 0, upnp = false }
---   print(srv.ticket)
---   local cli = peerova.client(srv.ticket)
---   local ev = cli:poll(10)                  -- ev.type == "connected"
---   cli:send(ev.peer, "hello", { channel = 0, reliable = true })
---   for e in srv:events(2) do print(e.type, e.peer, e.data) end
---   cli:close(); srv:close()
---
--- Library lookup: os.getenv("PEEROVA_LIB"), next to this file, ../../target/release (repo checkout),
--- then the system loader (ffi.load("peerova")). event.data is copied with ffi.string immediately.
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
int32_t peerova_send(peerova_node* node, uint32_t peer, uint8_t channel, const uint8_t* data, uint32_t len, int32_t reliable);
int32_t peerova_poll(peerova_node* node, peerova_event* out);
int32_t peerova_poll_wait(peerova_node* node, peerova_event* out, uint32_t timeout_ms);
void peerova_disconnect(peerova_node* node, uint32_t peer);
uint32_t peerova_peer_count(peerova_node* node);
void peerova_free(peerova_node* node);
const char* peerova_last_error(void);
const char* peerova_version(void);
]]

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
M.C = C -- raw C functions, if you need them

local function last_error()
  local p = C.peerova_last_error()
  return p ~= nil and ffi.string(p) or "unknown error"
end

function M.version() return ffi.string(C.peerova_version()) end

--- Encodes a flat Lua table as JSON (strings, numbers, booleans, arrays of strings).
local function to_json(t)
  if t == nil then return "{}" end
  if type(t) == "string" then return t end
  local function v(x)
    local tx = type(x)
    if tx == "string" then
      return '"' .. x:gsub('[%c"\\]', function(c) return string.format("\\u%04x", c:byte()) end) .. '"'
    elseif tx == "number" or tx == "boolean" then
      return tostring(x)
    elseif tx == "table" then
      local a = {}
      for i = 1, #x do a[i] = v(x[i]) end
      return "[" .. table.concat(a, ",") .. "]"
    end
    error("unsupported config value type " .. tx)
  end
  local parts = {}
  for k, x in pairs(t) do parts[#parts + 1] = v(tostring(k)) .. ":" .. v(x) end
  return "{" .. table.concat(parts, ",") .. "}"
end
M.to_json = to_json

local Node = {}
Node.__index = Node

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

--- Starts a server (blocks up to ~3 s until the ticket is ready). config: table or JSON string.
function M.server(config) return wrap(C.peerova_server_start(to_json(config)), true) end

--- Connects to a ticket; returns immediately — wait for a "connected" / "connect_failed" event.
function M.client(ticket, config) return wrap(C.peerova_client_connect(ticket, to_json(config)), false) end

local function handle(self)
  if self._h == nil then error("peerova: node is closed", 3) end
  return self._h
end

--- Sends a Lua string (binary-safe). opts = { channel = 0..255, reliable = true }.
--- reliable=false: datagram, max 1180 bytes, may be lost / reordered.
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

--- Returns the next event table { type = "message"|..., peer, channel, reliable, data } or nil.
--- timeout in seconds (nil/0 = non-blocking).
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

function Node:disconnect(peer) C.peerova_disconnect(handle(self), peer) end
function Node:peer_count() return tonumber(C.peerova_peer_count(handle(self))) end

function Node:close()
  if self._h ~= nil then
    C.peerova_free(ffi.gc(self._h, nil))
    self._h = nil
  end
end

return M
