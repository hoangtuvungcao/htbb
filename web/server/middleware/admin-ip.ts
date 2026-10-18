/**
 * Giới hạn IP cho API quản trị (tuỳ chọn): HTBB_ADMIN_IP_CHO_PHEP="203.0.113.5, 10.0.0.0/8, ::1".
 * Bỏ trống = không giới hạn (mặc định). Áp cho MỌI /api/admin/** kể cả đăng nhập; trả 404 chung chung
 * để không lộ là có khu quản trị. IP lấy bằng ipKhach (chỉ tin X-Forwarded-For từ proxy tin cậy).
 */
import { docLuatIp, ipDuocPhep, type LuatIp } from '../../shared/utils/ipChoPhep'

let dem: { raw: string, luat: LuatIp } | null = null

export default defineEventHandler((event) => {
  if (!event.path.startsWith('/api/admin/') && event.path !== '/api/admin') return
  const raw = useHtbbConfig().admin.ipChoPhep
  if (!raw) return
  if (!dem || dem.raw !== raw) {
    dem = { raw, luat: docLuatIp(raw) }
    if (dem.luat.loi.length) console.error('[htbb-admin] HTBB_ADMIN_IP_CHO_PHEP có mục sai, bị bỏ qua:', dem.luat.loi.join(', '))
  }
  if (!ipDuocPhep(ipKhach(event), dem.luat)) {
    throw apiLoi(404, 'Không tìm thấy trang.')
  }
})
