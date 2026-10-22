/**
 * Đấu trường Bảo Bối 3v3 (công khai): bảng mùa (top 100 điểm mùa hiện tại, tob_dau_truong_mua) và bảng TUẦN (điểm kiếm
 * được trong tuần cúp hiện tại, tob_dau_truong_tran). Mùa hiện tại = mùa lớn nhất máy chủ game đã ghi. Chỉ trả tên nhân
 * vật (tob_dau_truong_doi.ten), không tài khoản / thiết bị / IP. Đệm 60 giây. Logic thuần: shared/utils/dauTruong.ts.
 */
import { DAU_TRUONG, dongMuaTu, dongTuanTu, tenBacDauTruong, xepMua, xepTuan } from '../../shared/utils/dauTruong'
import { moTuanCup, tuanCup } from '../../shared/utils/cupPvp'
import { chuoiTuMs, msTuChuoi } from '../../shared/utils/suKienNam'

export default defineEventHandler(async () => quaDem('dau-truong', 60, async () => {
  const rong = { bayGio: null as string | null, mua: 0, tuan: 0, mo: '', dong: '', bangMua: [] as unknown[], bangTuan: [] as unknown[], soNguoi: 0, quyTac: DAU_TRUONG }
  try {
    const db = dbGame()
    if (!(await coBang(db, 'tob_dau_truong_mua')) || !(await coBang(db, 'tob_dau_truong_tran'))) return rong
    const bayGio = await suKienBayGio()
    const t = tuanCup(msTuChuoi(bayGio))
    const mua = Number((await q<Record<string, unknown>>(db, 'SELECT COALESCE(MAX(mua), 0) AS m FROM tob_dau_truong_mua'))[0]?.m ?? 0)
    const mDs = xepMua((await q<Record<string, unknown>>(db,
      `SELECT m.id_char, d.ten, m.diem, m.thang, m.thua, m.so_tran FROM tob_dau_truong_mua m
         LEFT JOIN tob_dau_truong_doi d ON d.id_char = m.id_char WHERE m.mua = ? AND m.so_tran > 0 ORDER BY m.diem DESC, m.id_char LIMIT 100`, [mua])).map(dongMuaTu))
    const tDs = xepTuan((await q<Record<string, unknown>>(db,
      `SELECT t.cong, d.ten, COUNT(*) AS tran, SUM(t.ket_qua = 1) AS thang, SUM(t.diem_cong_sau - t.diem_cong_truoc) AS diem_doi, SUM(t.luong) AS luong
         FROM tob_dau_truong_tran t LEFT JOIN tob_dau_truong_doi d ON d.id_char = t.cong
        WHERE t.tuan = ? AND t.ket_qua >= 0 GROUP BY t.cong, d.ten ORDER BY diem_doi DESC LIMIT 200`, [t])).map(dongTuanTu))
    const soNguoi = Number((await q<Record<string, unknown>>(db, 'SELECT COUNT(*) AS n FROM tob_dau_truong_mua WHERE mua = ? AND so_tran > 0', [mua]))[0]?.n ?? 0)
    const mo = moTuanCup(t)
    return {
      ...rong,
      bayGio,
      mua,
      tuan: t,
      mo: chuoiTuMs(mo),
      dong: chuoiTuMs(mo + 7 * 86_400_000),
      bangMua: mDs.map((d, i) => ({ ...d, hang: i + 1, bac: tenBacDauTruong(d.diem) })),
      bangTuan: tDs.slice(0, 50).map((d, i) => ({ ...d, hang: i + 1 })),
      soNguoi,
    }
  }
  catch {
    return rong
  }
}))
