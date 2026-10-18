/**
 * Tải một tệp game lên kho /tai/. POST ?ten=<tên tệp>&ghiDe=1, Content-Type: application/octet-stream,
 * thân = nội dung tệp (ghi theo luồng, xem server/utils/tepGame.ts). Trả { ten, url, byte, sha256 }.
 */
export default defineAdminHandler(async (event) => {
  const q = getQuery(event)
  const ct = (getHeader(event, 'content-type') ?? '').toLowerCase()
  if (!ct.startsWith('application/octet-stream')) throw apiLoi(415, 'Gửi tệp dạng application/octet-stream.')
  const tep = await luuTepGameTuLuong(event, String(q.ten ?? ''), q.ghiDe === '1')
  await ghiNhatKy(event, 'tai_game_tai_len', tep.ten.slice(0, 100), `${tep.byte} byte · sha256 ${tep.sha256}`)
  return tep
})
