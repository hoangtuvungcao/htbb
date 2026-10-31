/**
 * Link của website PHP cũ (/kpah/..., /*.php, danh sách máy chủ cũ) — xem server/utils/duongDanCu.ts.
 * Danh sách máy chủ trả thẳng nội dung (server/utils/danhSachMayChu.ts); còn lại 301/308.
 */
import { xuLyDuongDanCu } from '../utils/duongDanCu'
import { bienDanhSachClientCu, bienDanhSachMayChu, docDanhSachMayChu } from '../utils/danhSachMayChu'

export default defineEventHandler((event) => {
  const kq = xuLyDuongDanCu(event.path, event.method)
  if (kq === null) return
  if (kq.loai === 'chuyen') return sendRedirect(event, kq.dich, kq.ma)
  setHeader(event, 'Content-Type', 'text/plain; charset=utf-8')
  setHeader(event, 'Cache-Control', 'no-cache')
  const tho = kq.bien === 'cu' ? bienDanhSachClientCu(process.env) : bienDanhSachMayChu(process.env)
  const noiDung = docDanhSachMayChu(tho)
  if (noiDung === null) {
    setResponseStatus(event, 503)
    console.error('[server-list] chua cau hinh HTBB_SERVER_LIST')
    return 'Danh sach may chu dang cap nhat.\n'
  }
  return noiDung
})
