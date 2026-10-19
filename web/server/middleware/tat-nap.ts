/**
 * Khoá toàn bộ API nạp tiền khi HTBB_NAP_BAT tắt (mặc định trên nhánh pokemon/chuyen-doi).
 *
 * Quyết định Q10 (docs/kien-truc.md): không thu tiền khi còn dùng
 * asset PMDCollab (CC BY-NC 4.0) hoặc tên Pokémon thật. Code nạp KHÔNG bị xoá, chỉ bị chặn
 * ở một chỗ này: tạo đơn, xem đơn, hỏi trạng thái, webhook SePay và trang xác nhận nạp.
 * Khu quản trị (/api/admin/naptien) vẫn mở để admin xem lại đơn cũ.
 *
 * CHẾ ĐỘ DEMO (HTBB_NAP_DEMO=1, napDemoThuan.ts): webhook SePay/Casso/mock -> 403 + ghi log (không ghi
 * nhận tiền); /api/xac-nhan-nap/** -> 403; các API nạp khác chỉ mở cho người được xem demo
 * (HTBB_NAP_DEMO_CHI_ADMIN) và đi vào nhánh demo trong handler (không chạm bảng nap_*).
 */
export default defineEventHandler(async (event) => {
  const duongDan = event.path.split('?')[0] ?? ''
  if (!duongDan.startsWith('/api/nap/') && !duongDan.startsWith('/api/xac-nhan-nap/')) return
  const n = useHtbbConfig().nap
  if (n.demo) {
    const kq = demoXetDuongDan(duongDan, true, napDemoXem(napDemoTenNguoiXem(event)))
    if (kq === 'tu_choi_webhook') {
      napDemoGhiWebhook(event, duongDan)
      throw apiLoi(403, 'Chế độ DEMO: webhook bị bỏ qua, không ghi nhận tiền.', { code: 'demo' })
    }
    if (kq === 'tu_choi_xac_nhan') throw apiLoi(403, 'Chế độ DEMO: không xác nhận nạp tiền.', { code: 'demo' })
    if (kq === 'demo') return
    throw apiLoi(403, 'Máy chủ này phi thương mại nên tính năng nạp tiền đã tắt.')
  }
  if (n.bat) return
  // Bán Thành viên HTBB có công tắc riêng (thanh_vien.ban_bat): khi đang bán thì webhook cổng thanh toán
  // và trang xác nhận tay vẫn phải nhận tiền vào (đơn thành viên đi chung luồng nạp).
  if ((duongDan.startsWith('/api/nap/webhook-') || duongDan.startsWith('/api/xac-nhan-nap/')) && (await tvBanDuoc().catch(() => ({ duoc: false }))).duoc) return
  throw apiLoi(403, 'Máy chủ này phi thương mại nên tính năng nạp tiền đã tắt.')
})
