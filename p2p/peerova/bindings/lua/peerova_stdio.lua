--- peerova_stdio — Peerova for plain Lua 5.1 / 5.2 / 5.3 / 5.4 (no FFI, no C module) through the
--- `peerova stdio` sidecar process (JSON lines). Same API as peerova.lua (LuaJIT FFI), except that
--- poll() always blocks and there is no room_code() (the sidecar does not report the server's
--- connect code; clients can still connect with a 6-character code instead of a ticket).
---
---   local peerova = require "peerova_stdio"
---   local srv = peerova.server{ port = 0, upnp = false }
---   local cli = peerova.client(srv.ticket)   -- or a 6-character connect code
---   local ev = cli:poll()                    -- blocks until the next event: "connected"
---   cli:send(ev.peer, "hello", { channel = 0, reliable = true })
---   print(srv:poll().type, srv:poll().data)  -- "connected", then "message" "hello"
---
--- How: commands go to the sidecar's stdin through io.popen(cmd, "w"); its stdout is redirected to
--- a named pipe (mkfifo) that we read line by line.
--- Limitations: POSIX only (sh + mkfifo; on Windows use LuaJIT + peerova.lua, or a socket library);
--- poll() BLOCKS until the next event (pure Lua has no select/timeouts) — run one node per
--- coroutine-free loop/process, or use the LuaJIT binding when you need non-blocking polling.
--- Binary lookup: opts.binary, os.getenv("PEEROVA_BIN"), the SDK development tree, `peerova` on PATH.
--- Errors: failures raise a Lua error "peerova_stdio: <message>"; catch with pcall.
---
--- @module peerova_stdio
local M = { CONNECTED = 1, DISCONNECTED = 2, MESSAGE = 3, CONNECT_FAILED = 4, MAX_UNRELIABLE_PAYLOAD = 1180 }

---------------------------------------------------------------- base64url (no padding), 5.1-safe
local ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
local DEC = {}
for i = 1, 64 do DEC[ALPHA:byte(i)] = i - 1 end
DEC[("+"):byte()] = 62
DEC[("/"):byte()] = 63
local floor = math.floor

--- base64url-encodes a binary string (no padding), as used by the sidecar protocol.
function M.b64url_encode(s)
  local out, n = {}, #s
  for i = 1, n, 3 do
    local a, b, c = s:byte(i, i + 2)
    local v = a * 65536 + (b or 0) * 256 + (c or 0)
    local c1, c2 = floor(v / 262144) % 64, floor(v / 4096) % 64
    local c3, c4 = floor(v / 64) % 64, v % 64
    local chunk = ALPHA:sub(c1 + 1, c1 + 1) .. ALPHA:sub(c2 + 1, c2 + 1)
    if b then chunk = chunk .. ALPHA:sub(c3 + 1, c3 + 1) end
    if c then chunk = chunk .. ALPHA:sub(c4 + 1, c4 + 1) end
    out[#out + 1] = chunk
  end
  return table.concat(out)
end

--- Decodes base64url (padding optional) into a binary string.
function M.b64url_decode(s)
  s = s:gsub("[=%s]", "")
  local out = {}
  for i = 1, #s, 4 do
    local a, b, c, d = s:byte(i, i + 3)
    local v = DEC[a] * 262144 + DEC[b] * 4096 + (c and DEC[c] * 64 or 0) + (d and DEC[d] or 0)
    local x, y, z = floor(v / 65536) % 256, floor(v / 256) % 256, v % 256
    if d then out[#out + 1] = string.char(x, y, z)
    elseif c then out[#out + 1] = string.char(x, y)
    else out[#out + 1] = string.char(x) end
  end
  return table.concat(out)
end

---------------------------------------------------------------- tiny JSON
--- Encodes a config table as JSON (strings, numbers, booleans, arrays, nested tables such as
--- `share = { ... }`). A string is passed through as-is.
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

local function utf8char(cp)
  if cp < 0x80 then return string.char(cp) end
  if cp < 0x800 then return string.char(0xC0 + floor(cp / 64), 0x80 + cp % 64) end
  return string.char(0xE0 + floor(cp / 4096), 0x80 + floor(cp / 64) % 64, 0x80 + cp % 64)
end

--- Parses one sidecar line: a flat JSON object of strings / numbers / booleans / null.
local function parse_line(s)
  local o, i = {}, 1
  local function ws() i = s:find("[^ \t\r\n]", i) or #s + 1 end
  local function str()
    i = i + 1 -- opening quote
    local buf = {}
    while true do
      local c = s:sub(i, i)
      if c == "" then error("unterminated string") end
      if c == '"' then i = i + 1 break end
      if c == "\\" then
        local e = s:sub(i + 1, i + 1)
        if e == "u" then
          buf[#buf + 1] = utf8char(tonumber(s:sub(i + 2, i + 5), 16)); i = i + 6
        else
          buf[#buf + 1] = ({ n = "\n", t = "\t", r = "\r", b = "\b", f = "\f" })[e] or e; i = i + 2
        end
      else
        local j = s:find('["\\]', i) or #s + 1
        buf[#buf + 1] = s:sub(i, j - 1); i = j
      end
    end
    return table.concat(buf)
  end
  ws()
  if s:sub(i, i) ~= "{" then return nil end
  i = i + 1
  while true do
    ws()
    local c = s:sub(i, i)
    if c == "}" or c == "" then break end
    if c == "," then i = i + 1; ws() end
    local k = str(); ws(); i = i + 1; ws() -- key, ':'
    c = s:sub(i, i)
    if c == '"' then o[k] = str()
    else
      local j = s:find("[,}%s]", i) or #s + 1
      local lit = s:sub(i, j - 1); i = j
      if lit == "true" then o[k] = true elseif lit == "false" then o[k] = false
      elseif lit ~= "null" then o[k] = tonumber(lit) end
    end
  end
  return o
end
M.parse_line = parse_line

---------------------------------------------------------------- process plumbing
local function q(a) return "'" .. tostring(a):gsub("'", "'\\''") .. "'" end

local function ok(r, _, code) -- os.execute result in 5.1 (number) and 5.2+ (true/nil, "exit", code)
  if type(r) == "number" then return r == 0 end
  return r == true and (code == nil or code == 0)
end

local function exists(p)
  local f = p and io.open(p, "rb")
  if f then f:close() return true end
  return false
end

--- Resolves the sidecar binary path (see the module comment for the lookup order).
--- @tparam[opt] string override explicit path
--- @treturn string
function M.binary(override)
  local here = (debug.getinfo(1, "S").source:match("^@(.*[/\\])") or "./")
  local cands = { override or false, os.getenv "PEEROVA_BIN" or false, here .. "../../target/release/peerova" }
  for i = 1, #cands do if cands[i] and exists(cands[i]) then return cands[i] end end
  return "peerova"
end

local Node = {}
Node.__index = Node

local function spawn(args)
  local fifo = os.tmpname()
  os.remove(fifo)
  if not ok(os.execute("mkfifo -m 600 " .. q(fifo))) then error("peerova_stdio: mkfifo failed (POSIX only)", 3) end
  local parts = {}
  for i = 1, #args do parts[i] = q(args[i]) end
  local cmd = table.concat(parts, " ") .. " > " .. q(fifo)
  local proc = assert(io.popen(cmd, "w"))
  local out = assert(io.open(fifo, "r")) -- blocks until the sidecar opens it for writing
  local self = setmetatable({ _proc = proc, _out = out, _fifo = fifo, _peers = {}, _npeers = 0, errors = {} }, Node)
  while true do
    local line = out:read("*l")
    if not line then self:close(); error("peerova_stdio: sidecar exited before ready (bad binary/config?)", 3) end
    local j = parse_line(line)
    if j and j.event == "ready" then self.ticket = j.ticket break end
    if j and j.event == "error" then self:close(); error("peerova_stdio: " .. tostring(j.message), 3) end
  end
  return self
end

--- Starts a sidecar server and waits for its ticket (node.ticket).
--- @param config table|string|nil same keys as peerova.server (port, key_file, license, max_ccu,
---   token, upnp, private, app, share)
--- @param opts table|nil `{ binary = "/path/to/peerova" }`
--- @return node; raises an error if the binary is missing or the server cannot start (the sidecar
---   prints the exact reason on stderr)
function M.server(config, opts)
  return spawn { M.binary(opts and opts.binary), "stdio", "server", to_json(config) }
end

--- Starts a sidecar client; wait for the "connected" / "connect_failed" event with poll().
--- @param ticket string server ticket ("pvt_...") or 6-character connect code
--- @param config table|string|nil `{ token = "...", timeout_ms = 20000 }`
--- @param opts table|nil `{ binary = "/path/to/peerova" }`
--- @return node; raises an error if the ticket is invalid or the code unknown (reason on stderr)
function M.client(ticket, config, opts)
  return spawn { M.binary(opts and opts.binary), "stdio", "client", ticket, to_json(config) }
end

local function write(self, line)
  if not self._proc then error("peerova_stdio: node is closed", 3) end
  self._proc:write(line, "\n")
  self._proc:flush()
end

--- Sends a binary-safe string. opts = { channel = 0..255, reliable = true }.
--- reliable=true: ordered, retransmitted per channel, any size. reliable=false: datagram, may be
--- lost/reordered, max 1180 bytes (checked locally, raises an error). Errors reported by the
--- sidecar (e.g. unknown peer) are appended to node.errors when poll() reads them.
function Node:send(peer, data, opts)
  local ch, rel = 0, true
  if opts then
    ch = opts.channel or 0
    if opts.reliable == false then rel = false end
  end
  if not rel and #data > M.MAX_UNRELIABLE_PAYLOAD then error("peerova_stdio: unreliable payload max 1180 bytes", 2) end
  write(self, ('{"cmd":"send","peer":%d,"channel":%d,"reliable":%s,"data":"%s"}')
    :format(peer, ch, tostring(rel), M.b64url_encode(data)))
end

--- Blocks until the next event and returns { type, peer, channel, reliable, data }, or nil when the
--- sidecar has exited. type is "connected" | "message" | "disconnected" | "connect_failed"; data is
--- the payload for "message" and the reason text for "disconnected" / "connect_failed".
function Node:poll()
  while self._out do
    local line = self._out:read("*l")
    if not line then return nil end
    local j = parse_line(line)
    local ev = j and j.event
    if ev == "message" then
      return { type = "message", peer = j.peer, channel = j.channel, reliable = j.reliable, data = M.b64url_decode(j.data or "") }
    elseif ev == "connected" then
      if not self._peers[j.peer] then self._peers[j.peer] = true; self._npeers = self._npeers + 1 end
      return { type = "connected", peer = j.peer, channel = 0, reliable = false, data = "" }
    elseif ev == "disconnected" then
      if self._peers[j.peer] then self._peers[j.peer] = nil; self._npeers = self._npeers - 1 end
      return { type = "disconnected", peer = j.peer, channel = 0, reliable = false, data = j.reason or "" }
    elseif ev == "connect_failed" then
      return { type = "connect_failed", peer = 0, channel = 0, reliable = false, data = j.reason or "" }
    elseif ev == "error" then
      self.errors[#self.errors + 1] = j.message
    end
  end
  return nil
end

--- Iterator over events (blocking; ends when the sidecar exits — break out yourself).
function Node:events() return function() return self:poll() end end

--- Closes the connection to peer; a "disconnected" event follows.
function Node:disconnect(peer) write(self, ('{"cmd":"disconnect","peer":%d}'):format(peer)) end
--- Connected peers, as seen by the events polled so far.
function Node:peer_count() return self._npeers end

--- Asks the sidecar to quit, waits for it, and removes the pipe. Idempotent.
function Node:close()
  if self._proc then
    pcall(function() self._proc:write('{"cmd":"quit"}\n'); self._proc:flush() end)
    self._proc:close() -- waits for the sidecar to exit
    self._proc = nil
  end
  if self._out then self._out:close(); self._out = nil end
  if self._fifo then os.remove(self._fifo); self._fifo = nil end
end

return M
