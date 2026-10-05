/**
 * Xác nhận đã nhận tiền cho một đơn (khu quản trị). Chỉ đơn đang chờ thanh toán.
 * Body: { id } -> { ok, message }
 */
export default defineAdminHandler(async (event) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const id = int(body?.id, 0)
  if (id <= 0) throw apiLoi(400, 'Thiếu mã đơn cần xác nhận.')

  const kq = await napMocXacNhanThuCong(id)
  if (!kq.ok || !kq.don) {
    throw apiLoi(404, `Không tìm thấy đơn chờ thanh toán #${id}`)
  }
  // Đơn đã được xác nhận rồi: tra tên lỗi thì vẫn ghi nhật ký theo userID, không báo lỗi oan.
  const ten = (await napMocTenTaiKhoan([kq.don.userID]).catch(() => ({} as Record<number, string>)))[kq.don.userID] ?? null
  await ghiNhatKy(event, 'xac_nhan_nap_moc', kq.don.ma_don, napMocMoTaDon(kq.don, ten))
  return { ok: true, message: `Đã xác nhận đơn #${id}` }
})
