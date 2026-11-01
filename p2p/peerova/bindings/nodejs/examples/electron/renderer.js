// Renderer: never touches Peerova directly; it only shows what the main process sends over IPC.
window.peerova.onLog((line) => {
  const li = document.createElement("li");
  li.textContent = line;
  document.getElementById("log").append(li);
});
