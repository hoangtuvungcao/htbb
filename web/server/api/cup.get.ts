/**
 * Cúp PvP tuần (công khai): cúp tuần này (tên, luật, lịch — từ tob_su_kien_lich mã moi:cuptuan do máy chủ game ghi theo
 * com.htbb.pokemon.LichNam / CupLogic), bảng điểm tuần này (tob_cup_dang_ky), nhà vô địch 8 tuần gần nhất.
 * Chỉ trả tên Pokémon (nhân vật), không thiết bị / IP. Đệm 60 giây. Logic thuần: shared/utils/cupPvp.ts.
 */
import { dongCupTuDong, gopDoi, moTuanCup, tuanCup } from '../../shared/utils/cupPvp'
import { chuoiTuMs, msTuChuoi } from '../../shared/utils/suKienNam'

export default defineEventHandler(async () => quaDem('cup-pvp', 60, async () => {
  try {
    const bayGio = await suKienBayGio()
    const bay = msTuChuoi(bayGio)
    const t = tuanCup(bay)
    const mo = moTuanCup(t)
    const lich = (await docLanSuKien(chuoiTuMs(mo), chuoiTuMs(mo + 1000))).filter(l => l.ma === 'moi:cuptuan' && Number(l.ghiDe) !== 1)
    const coBangDk = await coBang(dbGame(), 'tob_cup_dang_ky')
    const bang = coBangDk
      ? gopDoi((await q<Record<string, unknown>>(dbGame(),
          'SELECT char_id, doi_id, ten, cap, diem, thang, hoa, thua, hang FROM tob_cup_dang_ky WHERE tuan = ? ORDER BY thu_tu LIMIT 2000', [t])).map(dongCupTuDong))
      : []
    const voDich = coBangDk
      ? await q<Record<string, unknown>>(dbGame(),
          `SELECT d.tuan, d.ma_loai, GROUP_CONCAT(d.ten ORDER BY d.char_id SEPARATOR ' & ') AS ten, MAX(d.diem) AS diem
             FROM tob_cup_dang_ky d WHERE d.hang = 1 AND d.tuan < ? AND d.tuan >= ? GROUP BY d.tuan, d.ma_loai ORDER BY d.tuan DESC`, [t, t - 8])
      : []
    return {
      bayGio,
      tuan: t,
      mo: chuoiTuMs(mo),
      dong: chuoiTuMs(mo + 7 * 86_400_000),
      cup: lich[0] ?? null,
      bang: bang.slice(0, 200),
      soDoi: bang.length,
      voDich: voDich.map(r => ({ tuan: Number(r.tuan), maLoai: String(r.ma_loai ?? ''), ten: String(r.ten ?? ''), diem: Number(r.diem) || 0, mo: chuoiTuMs(moTuanCup(Number(r.tuan))) })),
    }
  }
  catch {
    return { bayGio: null, tuan: 0, mo: '', dong: '', cup: null, bang: [], soDoi: 0, voDich: [] }
  }
}))
