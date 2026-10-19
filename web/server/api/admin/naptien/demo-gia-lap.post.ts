/**
 * CHẾ ĐỘ DEMO: "Giả lập thanh toán" cho MỘT đơn demo (mã DM...). Chỉ đổi trạng thái HIỂN THỊ trong bộ nhớ
 * sang "đã cộng" để người tạo đơn thấy màn hình thành công — KHÔNG ghi database, KHÔNG cộng lượng,
 * KHÔNG cấp Thành viên, không đi qua napXacNhanThanhToan. Body: { ma_don }.
 */
import { laMaDonDemo } from '../../../utils/napDemoThuan'

export default defineAdminHandler(async (event, admin) => {
  if (!useHtbbConfig().nap.demo) throw apiLoi(404, 'Chỉ dùng khi bật chế độ DEMO (HTBB_NAP_DEMO=1).')
  const b = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const maDon = str(b?.ma_don).toUpperCase()
  const d = laMaDonDemo(maDon) ? napDemoKho().giaLap(maDon, admin.username) : null
  if (!d) throw apiLoi(404, 'Không có đơn demo này (đơn demo mất khi khởi động lại web).')
  console.log(`[htbb] DEMO: ${admin.username} gia lap thanh toan don demo ${maDon} (khong cong gi)`)
  return { ok: true, message: `Đã giả lập đơn demo ${maDon} — chỉ hiển thị, không cộng gì.` }
})
