/** Chỉ số kinh tế hằng đêm (tob_kinh_te_ngay, game ghi lúc 04:00). Chỉ admin. ?ngay=60 */
export default defineAdminHandler(async (event) => {
  if (!(await coBang(dbGame(), 'tob_kinh_te_ngay'))) return { coBang: false, ds: [] }
  const n = Math.max(7, Math.min(400, int(getQuery(event).ngay, 60)))
  const ds = await q(dbGame(), `SELECT * FROM tob_kinh_te_ngay ORDER BY ngay DESC LIMIT ${n}`)
  return {
    coBang: true,
    ds: ds.reverse().map(r => ({
      ngay: String(r.ngay).slice(0, 10), soNguoi: Number(r.so_nguoi), capTrungVi: Number(r.cap_trung_vi), chiSoGia: Number(r.chi_so_gia),
      tongXu: Number(r.tong_xu), tongLuong: Number(r.tong_luong), tongLuongKhoa: Number(r.tong_luong_khoa),
      cungTienThuc: Number(r.cung_tien_thuc_x100) / 100, top1: Number(r.top1_phan_tram_x100) / 100,
      xuVao: Number(r.xu_vao), xuRa: Number(r.xu_ra), muc: Number(r.muc),
    })),
  }
})
