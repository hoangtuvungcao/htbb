/**
 * Khoá toàn bộ API nạp tiền khi KPAH_NAP_BAT tắt (mặc định trên nhánh pokemon/chuyen-doi).
 *
 * Quyết định Q10 (HTBB/docs/pokemon/KE_HOACH_CHUYEN_DOI.md): không thu tiền khi còn dùng
 * asset PMDCollab (CC BY-NC 4.0) hoặc tên Pokémon thật. Code nạp KHÔNG bị xoá, chỉ bị chặn
 * ở một chỗ này: tạo đơn, xem đơn, hỏi trạng thái, webhook SePay và trang xác nhận nạp.
 * Khu quản trị (/api/admin/naptien) vẫn mở để admin xem lại đơn cũ.
 */
export default defineEventHandler((event) => {
  const duongDan = event.path.split('?')[0] ?? ''
  if (!duongDan.startsWith('/api/nap/') && !duongDan.startsWith('/api/xac-nhan-nap/')) return
  if (useHtbbConfig().nap.bat) return
  throw apiLoi(403, 'Máy chủ này phi thương mại nên tính năng nạp tiền đã tắt.')
})
