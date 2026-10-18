/**
 * Bảng kinh tế: chuỗi ngày tob_kinh_te_ngay + chỉ số dẫn xuất + cảnh báo (luật trong
 * shared/utils/kinhTeCanhBao.ts). ?ngay=90 (7..400). Chỉ đọc; siêu quản trị + chỉ xem.
 */
export default defineAdminHandler(async (event) => {
  const soNgay = int(getQuery(event).ngay, 90)
  const ds = await docKinhTeNgay(soNgay)
  const nhanXu = await docNhanXu48h().catch(() => ({ co: false, ds: [], soDong: 0, catBot: false }))
  const cuoi = ds?.at(-1)
  const dotBien = timDotBien(nhanXu.ds, cuoi?.chiSoGia ?? 0)
  const d = new Date()
  const homNay = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  return {
    coBang: ds !== null,
    ds: ds ?? [],
    chiSo: chiSoDanXuat(ds ?? []),
    canhBao: tinhCanhBaoKinhTe(ds ?? [], dotBien, homNay),
    luat: MO_TA_LUAT_KINH_TE,
    nhanXu: { co: nhanXu.co, soDong: nhanXu.soDong, catBot: nhanXu.catBot, top: nhanXu.ds.filter(x => x.nhan24 > 0).slice(0, 20), dotBien },
    gioiHanLog: KINH_TE_LOG_TOI_DA,
  }
})
