/**
 * Cộng tay (đền bù / tiền vào không khớp nội dung). Tạo đơn nguồn 'admin' rồi xác nhận qua
 * hàm duy nhất — vào cùng sổ cái, cùng nhật ký. Body: { username, char_id, ma_goi, ma_gd, ly_do }.
 * Chỉ khi HTBB_NAP_BAT bật (không dựng sổ sách khi nạp đang tắt).
 */
export default defineAdminHandler(async (event, admin) => {
  if (!useHtbbConfig().nap.bat) throw apiLoi(409, 'Nạp tiền đang tắt (HTBB_NAP_BAT).')
  if (!(await napCoBang())) napThieuBang()
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const ten = str(body?.username).trim().slice(0, 50)
  const charId = int(body?.char_id, 0)
  const maGoi = str(body?.ma_goi).slice(0, 20)
  const maGd = str(body?.ma_gd).trim().slice(0, 64)
  const lyDo = str(body?.ly_do).trim().slice(0, 200)
  if (lyDo.length < 5) throw apiLoi(400, 'Ghi lý do (ít nhất 5 ký tự).')
  if (!/^[A-Za-z0-9_.:-]{3,64}$/.test(maGd)) throw apiLoi(400, 'Nhập mã giao dịch / mã phiếu (3–64 ký tự) — chống cộng trùng.')
  const tk = await napTenTaiKhoan(ten)
  if (!tk) throw apiLoi(404, `Không có tài khoản "${ten}".`)
  const don = await napTaoDon({ id: Number(tk.id), username: tk.username }, maGoi, charId, null, 'admin', admin.username)
  const kq = await napXacNhanThanhToan({
    maDon: don.ma_don, soTienNhan: Number(don.so_tien), nhaCungCap: 'admin', maGd, tacNhan: `admin:${admin.username}`,
    ip: ipKhach(event), ghiChu: `cộng tay: ${lyDo}`,
  })
  if (kq.ketQua !== 'ok') throw apiLoi(409, `Đã tạo đơn ${don.ma_don} nhưng không xác nhận được: ${kq.ketQua}`)
  await ghiNhatKy(event, 'cong_tay_nap', don.ma_don, `${tk.username} -> ${don.charname}: ${maGoi}, GD ${maGd}, ${lyDo}`)
  return { ok: true, message: `Đã tạo + xác nhận đơn ${don.ma_don} cho ${don.charname}.` }
})
