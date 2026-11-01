// Sandboxed preload (must be CommonJS): exposes a tiny, read-only API to the page.
const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("peerova", {
  /** Calls `fn(line)` for every log line the main process forwards. */
  onLog: (fn) => ipcRenderer.on("peerova:log", (_event, line) => fn(line)),
});
