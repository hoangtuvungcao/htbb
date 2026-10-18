/**
 * Thêm / sửa một gói nạp (CMS). Body: { ma, ten, loai, gia_vnd, mo_ta, dang_ban, thu_tu }.
 * Lượng của gói lượng LUÔN tính theo tỷ giá cố định (không có ô nhập, không khuyến mãi %).
 * Đơn đã tạo giữ giá/lượng lúc tạo (chốt trong nap_don) — sửa gói không ảnh hưởng đơn cũ.
 */
export default defineAdminHandler(async (event, admin) => {
  if (!(await napCoBang())) napThieuBang()
  const b = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const ma = str(b?.ma).trim().toUpperCase()
  const ten = str(b?.ten).trim().slice(0, 100)
  const loai = str(b?.loai)
  const gia = int(b?.gia_vnd, 0)
  const luong = loai === 'luong' ? luongTheoGia(gia) : 0
  if (!/^[A-Z0-9_]{2,20}$/.test(ma)) throw apiLoi(400, 'Mã gói: 2–20 ký tự A-Z 0-9 _.')
  if (ten === '') throw apiLoi(400, 'Thiếu tên gói.')
  const loi = kiemGoi(loai, gia, luong)
  if (loi) throw apiLoi(400, loi)
  const moTa = str(b?.mo_ta).slice(0, 500)
  const dangBan = b?.dang_ban === false || Number(b?.dang_ban) === 0 ? 0 : 1
  const thuTu = int(b?.thu_tu, 0)
  await exec(dbGame(), 'INSERT IGNORE INTO nap_goi (ma, ten, loai, gia_vnd, luong) VALUES (?, ?, ?, ?, ?)', [ma, ten, loai, gia, luong])
  await exec(dbGame(),
    'UPDATE nap_goi SET ten = ?, loai = ?, gia_vnd = ?, luong = ?, mo_ta = ?, dang_ban = ?, thu_tu = ?, cap_nhat_luc = ?, admin_user = ? WHERE ma = ?',
    [ten, loai, gia, luong, moTa, dangBan, thuTu, gioDb(), admin.username.slice(0, 50), ma])
  await napNhatKy(null, `admin:${admin.username}`, 'sua_goi', '', 0, `${ma} ${loai} ${gia} đ ${luong} lượng, bán=${dangBan}`, ipKhach(event))
  await ghiNhatKy(event, 'sua_goi_nap', ma, `${ten}: ${gia} đ, ${luong} lượng, ${dangBan ? 'đang bán' : 'ngừng bán'}`)
  return { ok: true, luong }
})
