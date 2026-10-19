/**
 * Đọc lịch sự kiện năm (htbb_db.tob_su_kien_lich do máy chủ game ghi — com.htbb.pokemon.LichNam,
 * data/DatabaseLichNam). Logic thuần ở shared/utils/suKienNam.ts.
 */
import type { SuKienLan } from '../../shared/utils/suKienNam'
import { suKienLanTuDong } from '../../shared/utils/suKienNam'

export const SU_KIEN_LICH_BANG = 'tob_su_kien_lich'

/** Các lần diễn ra có cửa sổ giao [tu, den] (chuỗi giờ CSDL); [] nếu chưa có bảng (máy chủ game chưa chạy bản mới). */
export async function docLanSuKien(tu: string, den: string): Promise<SuKienLan[]> {
  if (!(await coBang(dbGame(), SU_KIEN_LICH_BANG))) return []
  const rows = await q<Record<string, unknown>>(dbGame(),
    `SELECT ma, nam, lan, ten, nhom, ngay_le, mo_ta, phan_thuong, mo_luc, dong_luc, ghi_de
       FROM ${SU_KIEN_LICH_BANG}
      WHERE dong_luc >= ? AND mo_luc <= ?
      ORDER BY mo_luc, ma`, [tu, den])
  return rows.map(suKienLanTuDong)
}

/** tob_su_kien đã có cột tu_dong chưa (máy chủ game bản mới tự thêm; hoặc chạy sql/them_lich_su_kien_nam.sql). */
export async function coCotTuDong(): Promise<boolean> {
  try {
    const n = await qv<number>(dbGame(),
      `SELECT COUNT(*) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = 'tu_dong'`, [SU_KIEN_BANG])
    return Number(n) > 0
  }
  catch { return false }
}

/** Mã có trong lịch năm (đã có ít nhất một dòng tob_su_kien_lich). */
export async function laMaLichNam(ma: string): Promise<boolean> {
  if (!(await coBang(dbGame(), SU_KIEN_LICH_BANG))) return false
  return Number(await qv<number>(dbGame(), `SELECT COUNT(*) FROM ${SU_KIEN_LICH_BANG} WHERE ma = ?`, [ma])) > 0
}

/**
 * Admin bật / tắt / hẹn giờ TAY một sự kiện đang theo lịch năm: chuyển nó sang thủ công (tu_dong = 0), không thì
 * nhịp 15 giây của máy chủ game sẽ đặt lại theo lịch. Trả về true nếu vừa chuyển.
 */
export async function chuyenThuCongNeuTheoLich(ma: string): Promise<boolean> {
  if (!(await coCotTuDong()) || !(await laMaLichNam(ma))) return false
  const r = await exec(dbGame(), `UPDATE ${SU_KIEN_BANG} SET tu_dong = 0 WHERE ma = ? AND tu_dong = 1`, [ma])
  return Number(r.affectedRows) > 0
}
