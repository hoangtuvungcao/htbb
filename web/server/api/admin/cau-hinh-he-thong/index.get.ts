/**
 * Trang "Cấu hình hệ thống": danh mục (CSDL cau_hinh_danh_muc + cau_hinh_nhom; dự phòng bản sinh), giá trị đang lưu, giá trị MÁY CHỦ ĐANG CHẠY (cau_hinh_hieu_luc,
 * do game ghi sau khi áp dụng), xác nhận áp dụng từng máy chủ, phiên bản, yêu cầu khởi động lại.
 * Đọc: siêu quản trị + Chỉ xem. Không trả bí mật nào (khoá env chỉ là cờ 0/1).
 */
export default defineAdminHandler(async (_event, admin) => {
  let loiDoc = ''
  let luu: Record<string, DongLuuTT> = {}
  let hieuLuc: Record<string, { giaTri: string | null, nguon: string, cho: string | null, loi: string | null, luc: string | null }> = {}
  let mayChu: Record<string, any>[] = []
  let phienBan = { phien_ban: 0, sua_luc: null as string | null, sua_boi: null as string | null }
  let khoiDongLai: Record<string, any> | null = null
  let giayTuNhip: number | null = null
  const nguonDanhMuc = await napDanhMucTTServer()
  try {
    await chTaoBang()
    luu = await chDocLuu()
    phienBan = await chPhienBan()
    khoiDongLai = await chKhoiDongLai()
    mayChu = await q(dbGame(), `SELECT may_chu, phien_ban, ap_dung_luc, khoi_dong_luc, nhip_luc, so_khoa, so_loi, loi, cho_khoi_dong_lai, thay_doi_cuoi
      FROM cau_hinh_ap_dung ORDER BY nhip_luc DESC`)
    giayTuNhip = await chGiayTuNhip()
    // Máy chủ mới nhịp gần nhất là "máy đang chạy" (trường hợp chạy nhiều bản trên cùng CSDL thì chọn được ở trang).
    const chinh = mayChu[0]?.may_chu
    if (chinh) {
      for (const r of await q<{ khoa: string, gia_tri: string | null, nguon: string, cho_khoi_dong_lai: string | null, loi: string | null, cap_nhat_luc: string | null }>(dbGame(),
        'SELECT khoa, gia_tri, nguon, cho_khoi_dong_lai, loi, cap_nhat_luc FROM cau_hinh_hieu_luc WHERE may_chu = ?', [chinh])) {
        hieuLuc[r.khoa] = { giaTri: r.gia_tri, nguon: r.nguon, cho: r.cho_khoi_dong_lai, loi: r.loi, luc: r.cap_nhat_luc }
      }
    }
  } catch (e: any) {
    loiDoc = String(e?.message ?? e)
  }
  const n = useHtbbConfig().nap
  const giaTri: Record<string, string | null> = {}
  const suaBoi: Record<string, { boi: string | null, luc: string | null }> = {}
  for (const k of danhMucTT()) {
    if (k.bang === 'env') continue
    giaTri[k.khoa] = giaTriLuu(luu, k)
    const d = k.bang === 'nap_cau_hinh' ? luu[`nap_cau_hinh:${k.khoa}`] : luu[k.khoa]
    if (d) suaBoi[k.khoa] = { boi: d.suaBoi, luc: d.suaLuc }
  }
  return {
    loiDoc,
    danhMuc: danhMucTT(),
    nhom: dsNhomTT(),
    nguonDanhMuc,
    giaTri,
    suaBoi,
    env: chTrangThaiEnv(),
    nap: { bat: n.bat, yeuCauBat: n.yeuCauBat, daCoGiayPhep: n.daCoGiayPhep, congAnToan: n.congAnToan, loiCauHinh: n.loiCauHinh, lyDoTat: n.lyDoTat },
    hieuLuc,
    mayChu,
    giayTuNhip,
    phienBan,
    khoiDongLai,
    quyen: { ghi: admin.vaiTro === 'sieu_quan_tri', choPhepThuNghiem: choPhepThuNghiemWeb() },
  }
})
