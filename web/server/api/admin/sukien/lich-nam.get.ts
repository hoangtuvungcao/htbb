/**
 * Lịch năm cho /admin/sukien: mọi lần diễn ra (năm trước .. năm sau) của mọi sự kiện có lịch, kèm trạng thái
 * tob_su_kien (tự động / thủ công, bật, máy chủ đang chạy). Nguồn: tob_su_kien_lich (máy chủ game ghi).
 */
export default defineAdminHandler(async () => {
  const bayGio = await suKienBayGio()
  const coLich = await coBang(dbGame(), SU_KIEN_LICH_BANG)
  if (!coLich) return { coLich, bayGio, ds: [] }
  const nam = Number(bayGio.slice(0, 4)) || new Date().getFullYear()
  const coTuDong = await coCotTuDong()
  const rows = await q<Record<string, unknown>>(dbGame(),
    `SELECT l.ma, l.nam, l.lan, l.ten, l.nhom, l.ngay_le, l.mo_ta, l.phan_thuong, l.lich_mo, l.lich_dong,
            l.mo_luc, l.dong_luc, l.ghi_de, l.sua_boi, l.sua_luc,
            s.bat, s.dang_bat, ${coTuDong ? 's.tu_dong' : '1 AS tu_dong'}
       FROM ${SU_KIEN_LICH_BANG} l LEFT JOIN ${SU_KIEN_BANG} s ON s.ma = l.ma
      WHERE l.nam BETWEEN ? AND ?
      ORDER BY l.mo_luc, l.ma`, [nam - 1, nam + 1])
  return {
    coLich,
    coTuDong,
    bayGio,
    ds: rows.map(r => ({
      ma: String(r.ma), nam: Number(r.nam), lan: Number(r.lan), ten: String(r.ten ?? ''), nhom: String(r.nhom ?? ''),
      ngayLe: String(r.ngay_le ?? ''), lichMo: String(r.lich_mo ?? ''), lichDong: String(r.lich_dong ?? ''),
      mo: String(r.mo_luc ?? ''), dong: String(r.dong_luc ?? ''), ghiDe: Number(r.ghi_de) || 0,
      suaBoi: r.sua_boi == null ? null : String(r.sua_boi), suaLuc: r.sua_luc == null ? null : String(r.sua_luc),
      bat: Number(r.bat) || 0, dangBat: Number(r.dang_bat) || 0, tuDong: Number(r.tu_dong ?? 1),
    })),
  }
})
