/**
 * Hoàn tiền / chargeback một đơn. Body: { id, ly_do, chargeback }.
 * Đã thanh toán (chưa vào game) -> hoan_tien. Đã cộng -> cho_thu_hoi: server game chuyển lượng
 * thành nợ, trừ dần khi online; thẻ tháng -30 ngày; vé mùa mất cao cấp. Chargeback khoá tạo đơn mới.
 * Việc trả tiền thật cho người chơi làm NGOÀI hệ thống (chuyển khoản lại) — ghi mã GD vào lý do.
 */
export default defineAdminHandler(async (event, admin) => {
  if (!(await napCoBang())) napThieuBang()
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const id = int(body?.id, 0)
  const lyDo = str(body?.ly_do).trim().slice(0, 200)
  const chargeback = body?.chargeback === true
  if (id <= 0) throw apiLoi(400, 'Thiếu mã đơn.')
  if (lyDo.length < 5) throw apiLoi(400, 'Ghi lý do (ít nhất 5 ký tự), kèm mã giao dịch hoàn nếu có.')
  const don = await napHoanTien(id, admin.username, lyDo, chargeback, ipKhach(event))
  await ghiNhatKy(event, chargeback ? 'chargeback_nap' : 'hoan_tien_nap', don.ma_don, `đơn #${id} -> ${don.trang_thai}: ${lyDo}`)
  return {
    ok: true,
    message: don.trang_thai === 'cho_thu_hoi'
      ? `Đơn #${id}: chờ server game thu hồi (lượng thành nợ, trừ dần).`
      : `Đơn #${id}: đã hoàn (chưa vào game).`,
  }
})
