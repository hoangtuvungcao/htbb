/**
 * Danh mục gói nạp (bảng nap_goi, admin sửa ở /admin/naptien) + nhân vật của tài khoản + trần nạp.
 * Cần đăng nhập (trang /naptien chỉ cho người đã đăng nhập). -> { goi, nhanVat, tran, mua }
 */
export default defineEventHandler(async (event) => {
  const u = await requireUser(event)
  if (!(await napCoBang())) napThieuBang()
  try {
    const [goi, nhanVat, tran, vi, da, cauHinh] = await Promise.all([
      napDanhSachGoi(true), napNhanVat(u.id), napTran(), napVi(u.id), napDaDung(null, u.id), napCauHinh(),
    ])
    const mua = /mua=(\d+);con_ngay=(\d+)/.exec(cauHinh['trang_thai.mua_ve'] ?? '')
    return {
      goi: goi.map(g => ({ ma: g.ma, ten: g.ten, loai: g.loai, gia_vnd: Number(g.gia_vnd), luong: Number(g.luong), mo_ta: g.mo_ta })),
      nhanVat: nhanVat.map(n => ({ id: Number(n.id), charname: n.charname, lastLv: Number(n.lastLv) })),
      tran: {
        tuan: tran.tuan,
        thang: tran.thang,
        tuDat: Number(vi.tran_tu_dat),
        cho: Number(vi.tran_cho),
        choLuc: vi.tran_cho_luc,
        daTuan: da.tuan,
        daThang: da.thang,
      },
      khoaNap: Number(vi.khoa_nap) === 1,
      noLuong: Number(vi.no_luong),
      mua: mua ? { so: Number(mua[1]) + 1, conNgay: Number(mua[2]) } : null,
      tyGia: LUONG_MOI_1000_VND,
    }
  } catch (e: any) {
    if (e?.statusCode) throw e
    console.error('[nap] doc goi loi:', e?.code ?? e)
    throw apiLoi(503, 'Chưa tải được danh mục nạp lúc này. Bạn vui lòng thử lại sau ít phút.')
  }
})
