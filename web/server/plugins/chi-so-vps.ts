/**
 * Bật bộ lấy mẫu chỉ số VPS (server/utils/vpsLayMau.ts) khi web khởi động: mỗi 2 giây đọc CPU / RAM / swap / đĩa / mạng /
 * tiến trình / dịch vụ, giữ 30 phút trong bộ nhớ, ghi mỗi phút một dòng tob_chi_so_vps (7 ngày). Trang /admin/he-thong (thẻ VPS).
 * Tắt: HTBB_CHI_SO_VPS=0. Không chạy lúc prerender (build).
 */
import { batDauLayMauVps } from '../utils/vpsLayMau'

export default defineNitroPlugin(() => {
  if (import.meta.prerender) return
  if (['0', 'false', 'off'].includes(String(process.env.HTBB_CHI_SO_VPS ?? '').trim().toLowerCase())) return
  batDauLayMauVps()
})
