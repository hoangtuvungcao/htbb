/**
 * Admin xác nhận đã nhận tiền (đối chiếu sao kê). Body: { id, ma_gd, ghi_chu }.
 * Đi qua hàm duy nhất napXacNhanThanhToan (nhà cung cấp 'admin', mã GD bắt buộc, UNIQUE).
 */
export default defineAdminHandler(async (event, admin) => {
  if (!(await napCoBang())) napThieuBang()
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const id = int(body?.id, 0)
  const maGd = str(body?.ma_gd).trim().slice(0, 64)
  const ghiChu = str(body?.ghi_chu).trim().slice(0, 200)
  if (id <= 0) throw apiLoi(400, 'Thiếu mã đơn cần xác nhận.')
  if (!/^[A-Za-z0-9_.:-]{3,64}$/.test(maGd)) throw apiLoi(400, 'Nhập mã giao dịch trên sao kê ngân hàng (3–64 ký tự).')
  const don = await q1<{ ma_don: string, so_tien: number }>(dbGame(), 'SELECT ma_don, so_tien FROM nap_don WHERE id = ? LIMIT 1', [id])
  if (!don) throw apiLoi(404, `Không có đơn #${id}.`)
  const kq = await napXacNhanThanhToan({
    maDon: don.ma_don, soTienNhan: Number(don.so_tien), nhaCungCap: 'admin', maGd,
    tacNhan: `admin:${admin.username}`, ip: ipKhach(event), ghiChu: ghiChu || 'xác nhận tay',
  })
  if (kq.ketQua !== 'ok') throw apiLoi(409, `Không xác nhận được: ${kq.ketQua}`)
  await ghiNhatKy(event, 'xac_nhan_nap', don.ma_don, `đơn #${id} ${don.so_tien} đ, GD ${maGd}${ghiChu ? ` — ${ghiChu}` : ''}`)
  return { ok: true, message: `Đã xác nhận đơn #${id}. Server game sẽ cộng khi Pokémon online.` }
})
