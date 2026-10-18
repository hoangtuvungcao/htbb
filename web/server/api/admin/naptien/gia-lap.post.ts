/**
 * CHẾ ĐỘ THỬ: giả lập cổng thanh toán báo "đã nhận tiền" cho một đơn — đi đúng đường webhook
 * (ghi nap_webhook + napXacNhanThanhToan, nhà cung cấp 'mock'). Chỉ khi KPAH_NAP_SANDBOX=1.
 * Body: { ma_don, so_tien? } (thiếu so_tien = đúng số tiền đơn; nhỏ hơn để thử nhánh "thiếu tiền").
 */
export default defineAdminHandler(async (event) => {
  if (!useHtbbConfig().nap.sandbox) throw apiLoi(404, 'Chỉ dùng ở chế độ thử (KPAH_NAP_SANDBOX=1).')
  if (!(await napCoBang())) napThieuBang()
  const b = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const maDon = str(b?.ma_don).toUpperCase()
  const don = /^HT[A-Z0-9]{8}$/.test(maDon) ? await napLayDon(maDon) : null
  if (!don) throw apiLoi(404, 'Không có đơn này.')
  const soTien = int(b?.so_tien, Number(don.so_tien))
  const kq = await napXuLyGiaoDich(event, 'mock', { maGd: `MOCK-${Date.now()}`, soTien, noiDung: `NAP ${maDon} GIA LAP` })
  return { ok: kq.success, message: kq.message }
})
