import { gcChuanHoaMa, gcChuanHoaThuong, gcGiaTri, gcGioiHan, gcKiemVatPham, gcMoTa, gcThangNay } from '../../../utils/giftcodeAnToan'
/**
 * Tạo / sửa mã. Body { ma, phanThuong, han ('' = không hạn), tongLuot, capToiThieu, bat, ghiChu, sua?: boolean }.
 * Sửa: không đổi được tên mã; giữ nguyên da_dung.
 */
export default defineAdminHandler(async (event, admin) => {
  if (!(await coBang(dbGame(), 'tob_giftcode'))) throw apiLoi(409, 'Chưa có bảng tob_giftcode (chạy sql/htbb.sql).')
  const b = await bodyObj(event)
  const ma = gcChuanHoaMa(b.ma)
  if (!ma) throw apiLoi(400, 'Mã: 3–32 ký tự a-z, 0-9, _ hoặc -.')
  const chuan = gcChuanHoaThuong(b.phanThuong)
  if (!chuan) throw apiLoi(400, 'Phần thưởng chỉ được gồm LUONGKHOA:n, XU:n (≤ 50 000), GEM:id:n, ITEM:id:n (n ≤ 10, vật phẩm khoá; Vé treo 1260: n ≤ 50).')
  const gioiHan = await gcGioiHan()
  const giaTri = gcGiaTri(chuan)
  if (giaTri > gioiHan.giaTriToiDa) throw apiLoi(400, `Giá trị mã ${giaTri} vượt trần ${gioiHan.giaTriToiDa} (lượng khoá + ⌈xu / 1000⌉ + số đá + 5 × số vật phẩm, vé treo 1 × số vé).`)
  const han = ngayGio(b.han)
  const tongLuot = Math.max(1, Math.min(1_000_000, int(b.tongLuot, 1)))
  const cap = Math.max(1, Math.min(100, int(b.capToiThieu, 1)))
  const loiVatPham = await gcKiemVatPham(chuan, cap)
  if (loiVatPham) throw apiLoi(400, loiVatPham)
  const bat = b.bat === false || int(b.bat, 1) === 0 ? 0 : 1
  // tob_giftcode là utf8 (3 byte): emoji → '?' để MariaDB strict không báo lỗi 1366 (chuoiDb.ts)
  const ghiChu = chuUtf8mb3(chuoi(b.ghiChu, 'Ghi chú', 255))
  const cu = await q1<Record<string, any>>(dbGame(), 'SELECT * FROM tob_giftcode WHERE ma = ? LIMIT 1', [ma])
  if (cu && b.sua !== true) throw apiLoi(409, `Đã có mã "${ma}".`)
  if (!cu && b.sua === true) throw apiLoi(404, 'Không tìm thấy mã.')
  if (cu) {
    if (tongLuot < Number(cu.da_dung)) throw apiLoi(400, `Tổng lượt không được nhỏ hơn số đã dùng (${cu.da_dung}).`)
    await exec(dbGame(), 'UPDATE tob_giftcode SET phan_thuong = ?, han = ?, tong_luot = ?, cap_toi_thieu = ?, bat = ?, ghi_chu = ? WHERE ma = ?', [chuan, han, tongLuot, cap, bat, ghiChu, ma])
  }
  else {
    await exec(dbGame(), 'INSERT INTO tob_giftcode (ma, phan_thuong, han, tong_luot, da_dung, cap_toi_thieu, bat, ghi_chu, tao_boi, tao_luc) VALUES (?, ?, ?, ?, 0, ?, ?, ?, ?, NOW())',
      [ma, chuan, han, tongLuot, cap, bat, ghiChu, admin.username])
  }
  await ghiThayDoi(event, cu ? 'sua_giftcode' : 'tao_giftcode', `giftcode:${ma}`, cu, { chuan, han, tongLuot, cap, bat, ghiChu, giaTri })
  return { ok: true, ma, giaTri }
})
