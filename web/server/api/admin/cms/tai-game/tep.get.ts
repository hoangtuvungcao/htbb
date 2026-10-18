/** Các tệp game đã tải lên kho /tai/ (tên, dung lượng, SHA-256). */
export default defineAdminHandler(async () => ({
  ds: await dsTepGame(),
  toiDaMb: Math.round(toiDaTepGame() / 1048576),
}))
