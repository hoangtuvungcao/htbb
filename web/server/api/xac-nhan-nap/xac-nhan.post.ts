/**
 * Xác nhận đã nhận tiền cho một đơn (trang /xac-nhan-nap, mật khẩu chung). Đi qua đúng hàm duy
 * nhất napXacNhanThanhToan. Body: { id, ma_gd } — ma_gd: mã giao dịch trên sao kê ngân hàng
 * (bắt buộc: một giao dịch không xác nhận được hai đơn).
 */
export default defineEventHandler(async (event) => {
  napMocRequireTrangAdmin(event)
  if (!(await napCoBang())) napThieuBang()
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const id = int(body?.id, 0)
  const maGd = str(body?.ma_gd).trim().slice(0, 64)
  if (id <= 0) throw apiLoi(400, 'Thiếu mã đơn.')
  if (!/^[A-Za-z0-9_.:-]{3,64}$/.test(maGd)) throw apiLoi(400, 'Nhập mã giao dịch trên sao kê ngân hàng (3–64 ký tự).')
  const don = await q1<{ ma_don: string, so_tien: number }>(dbGame(), 'SELECT ma_don, so_tien FROM nap_don WHERE id = ? LIMIT 1', [id])
  if (!don) throw apiLoi(404, `Không có đơn #${id}.`)
  const kq = await napXacNhanThanhToan({
    maDon: don.ma_don, soTienNhan: Number(don.so_tien), nhaCungCap: 'mat_khau_chung', maGd,
    tacNhan: 'mat_khau_chung', ip: ipKhach(event), ghiChu: 'xác nhận tay (mật khẩu chung)',
  })
  if (kq.ketQua !== 'ok') throw apiLoi(409, `Không xác nhận được: ${kq.ketQua}`)
  await ghiNhatKy(event, 'xac_nhan_nap', don.ma_don, `đơn #${id} ${don.so_tien} đ, GD ${maGd} (mật khẩu chung)`)
  return { ok: true, message: `Đã xác nhận đơn #${id}` }
})
