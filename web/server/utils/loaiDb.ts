/**
 * Pokédex 1.025 loài: nối logic thuần shared/utils/loaiDuLieu.ts vào CSDL game (bảng data_loai* của
 * sql/nang_cap.sql, khối them_loai_day_du.sql). Danh sách nhớ 5 phút trong RAM (dữ liệu tĩnh, đổi khi chạy lại tệp SQL).
 */
import type { LoaiTomTat } from '~~/shared/utils/loai'
import { laThieuBang, taiDanhSachLoai, type Truy } from '~~/shared/utils/loaiDuLieu'

export const truyLoai: Truy = (sql, thamSo = []) => q(dbGame(), sql, thamSo)

let dem: { luc: number, ds: LoaiTomTat[] } | null = null
const GIU_MS = 5 * 60 * 1000

/** Danh sách loài; [] khi CSDL chưa có bảng data_loai (trang rơi về Pokédex tĩnh). */
export async function dsLoaiCoDem(): Promise<{ ds: LoaiTomTat[], coDuLieu: boolean }> {
  if (dem && Date.now() - dem.luc < GIU_MS) return { ds: dem.ds, coDuLieu: dem.ds.length > 0 }
  try {
    const ds = await taiDanhSachLoai(truyLoai)
    dem = { luc: Date.now(), ds }
    return { ds, coDuLieu: ds.length > 0 }
  }
  catch (e: any) {
    if (!laThieuBang(e)) console.error('[pokedex] danh sach:', e?.code ?? e?.message ?? e)
    return { ds: [], coDuLieu: false }
  }
}
