/**
 * Xác nhận đã nhận tiền cho một đơn (trang /xac-nhan-nap, mật khẩu chung).
 * Body: { id } -> { ok, message }
 */
export default defineEventHandler(async (event) => {
  napMocRequireTrangAdmin(event)
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const id = int(body?.id, 0)

  let kq
  try {
    kq = await napMocXacNhanThuCong(id)
  } catch (e: any) {
    console.error('[admin-naptien] xac nhan loi:', e?.code ?? e)
    throw apiLoi(503, 'Chưa xác nhận được đơn. Kiểm tra MySQL rồi thử lại.')
  }
  if (!kq.ok || !kq.don) {
    throw apiLoi(404, `Không tìm thấy đơn chờ thanh toán #${id}`)
  }
  // Ghi nhật ký để trang Nhật ký admin thấy được ai đã xác nhận đơn nào
  // (admin_user để trống vì đây là mật khẩu chung, không gắn với tài khoản nào).
  const ten = (await napMocTenTaiKhoan([kq.don.userID]).catch(() => ({} as Record<number, string>)))[kq.don.userID] ?? null
  await ghiNhatKy(event, 'xac_nhan_nap_moc', kq.don.ma_don, `${napMocMoTaDon(kq.don, ten)} (qua trang xác nhận nạp bằng mật khẩu chung)`)
  return { ok: true, message: `Đã xác nhận đơn #${id}` }
})
