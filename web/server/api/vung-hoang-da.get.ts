/**
 * Lịch Vùng hoang dã xoay vòng (loai-bo-sung): các tuần (data_loai_xoay_vong_lich), bản đồ tham gia
 * (data_loai_xoay_vong_map + tên data_map_ten + vùng sinh thái data_hst_vung), số loài mỗi chu kỳ. null = CSDL chưa có bảng.
 */
import type { LichVungHoangDa } from '~~/shared/utils/vungHoangDa'

export default defineEventHandler(async () => quaDem('vung-hoang-da', 300, async (): Promise<LichVungHoangDa | null> => {
  try {
    const tuan = await q<Record<string, any>>(dbGame(), 'SELECT chu_ky, ten, mo_ta, the_he, DATE_FORMAT(ngay_goc, \'%Y-%m-%d\') AS goc FROM data_loai_xoay_vong_lich ORDER BY chu_ky')
    if (!tuan.length) return null
    const bd = await q<Record<string, any>>(dbGame(), 'SELECT m.map_id, m.nhom, IFNULL(t.ten, \'\') AS ten, IFNULL(v.vung, 1) AS vung FROM data_loai_xoay_vong_map m'
      + ' LEFT JOIN data_map_ten t ON t.map_id = m.map_id LEFT JOIN data_hst_vung v ON v.map_id = m.map_id ORDER BY m.map_id')
    const so = await q<Record<string, any>>(dbGame(), 'SELECT chu_ky, COUNT(DISTINCT dex) AS n FROM data_loai_xoay_vong GROUP BY chu_ky')
    return {
      ngayGoc: String(tuan.map(t => t.goc).sort()[0]),
      tuan: tuan.map(t => ({ chuKy: Number(t.chu_ky), ten: String(t.ten), moTa: String(t.mo_ta), theHe: Number(t.the_he) })),
      banDo: bd.map(b => ({ id: Number(b.map_id), ten: String(b.ten) || `Bản đồ ${b.map_id}`, vung: Number(b.vung), nhom: Number(b.nhom) })),
      soLoai: Object.fromEntries(so.map(r => [Number(r.chu_ky), Number(r.n)])),
    }
  }
  catch {
    return null
  }
}))
