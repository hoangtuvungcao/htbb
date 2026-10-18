import { gcChuanHoaMa, gcChuanHoaThuong, gcGiaTri, gcGioiHan, gcMoTa, gcThangNay } from '../../../utils/giftcodeAnToan'
/** Xem trước phần thưởng: chuẩn hoá, giá trị, mô tả. Body { phanThuong }. */
export default defineAdminHandler(async (event) => {
  const b = await bodyObj(event)
  const chuan = gcChuanHoaThuong(b.phanThuong)
  const gioiHan = await gcGioiHan()
  if (!chuan) return { ok: false, loi: 'Phần thưởng chỉ được gồm LUONGKHOA:n, GEM:id:n, ITEM:id:n (n ≤ 10), cách nhau dấu phẩy.' }
  const giaTri = gcGiaTri(chuan)
  return { ok: giaTri <= gioiHan.giaTriToiDa, chuan, giaTri, toiDa: gioiHan.giaTriToiDa, moTa: await gcMoTa(chuan), loi: giaTri > gioiHan.giaTriToiDa ? `Giá trị ${giaTri} vượt trần ${gioiHan.giaTriToiDa}.` : '' }
})
