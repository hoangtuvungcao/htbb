import { gcChuanHoaMa, gcChuanHoaThuong, gcGiaTri, gcGioiHan, gcKiemVatPham, gcMoTa, gcThangNay } from '../../../utils/giftcodeAnToan'
/** Xem trước phần thưởng: chuẩn hoá, giá trị, mô tả, danh sách trắng vật phẩm. Body { phanThuong, capToiThieu? }. */
export default defineAdminHandler(async (event) => {
  const b = await bodyObj(event)
  const chuan = gcChuanHoaThuong(b.phanThuong)
  const gioiHan = await gcGioiHan()
  if (!chuan) return { ok: false, loi: 'Phần thưởng chỉ được gồm LUONGKHOA:n, GEM:id:n, ITEM:id:n (n ≤ 10), cách nhau dấu phẩy.' }
  const giaTri = gcGiaTri(chuan)
  // body.capToiThieu (nếu có) = cấp tối thiểu của mã: trang bị không được cao hơn
  const capRaw = Number((b as Record<string, unknown>).capToiThieu)
  const loiVatPham = await gcKiemVatPham(chuan, Number.isFinite(capRaw) ? Math.max(1, Math.min(100, Math.trunc(capRaw))) : 1)
  const loi = loiVatPham ?? (giaTri > gioiHan.giaTriToiDa ? `Giá trị ${giaTri} vượt trần ${gioiHan.giaTriToiDa}.` : '')
  return { ok: !loiVatPham && giaTri <= gioiHan.giaTriToiDa, chuan, giaTri, toiDa: gioiHan.giaTriToiDa, moTa: await gcMoTa(chuan), loi }
})
