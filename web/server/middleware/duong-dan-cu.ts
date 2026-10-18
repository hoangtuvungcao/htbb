/**
 * Link của website PHP cũ (/kpah/..., /*.php, /NQSH2.txt) — xem server/utils/duongDanCu.ts.
 * Danh sách máy chủ trả thẳng nội dung (server/utils/danhSachMayChu.ts); còn lại 301/308.
 */
import { xuLyDuongDanCu } from '../utils/duongDanCu'
import { bienDanhSachMayChu, bienDanhSachNqsh, docDanhSachMayChu } from '../utils/danhSachMayChu'

export default defineEventHandler((event) => {
  const kq = xuLyDuongDanCu(event.path, event.method)
  if (kq === null) return
  if (kq.loai === 'chuyen') return sendRedirect(event, kq.dich, kq.ma)
  setHeader(event, 'Content-Type', 'text/plain; charset=utf-8')
  setHeader(event, 'Cache-Control', 'no-cache')
  const tho = kq.bien === 'nqsh' ? bienDanhSachNqsh(process.env) : bienDanhSachMayChu(process.env)
  const noiDung = docDanhSachMayChu(tho)
  if (noiDung === null) {
    setResponseStatus(event, 503)
    return 'Chua cau hinh danh sach may chu (HTBB_SERVER_LIST).\n'
  }
  return noiDung
})
