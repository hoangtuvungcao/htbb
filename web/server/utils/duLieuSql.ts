/**
 * Đọc bảng dữ liệu game (data_* / cau_hinh_danh_muc ...) từ CSDL htbb_db LÚC CHẠY, có cache.
 *
 * CSDL là nguồn duy nhất (bảng sinh bởi tools/pokemon/*, tools/cauhinh/*); JSON trong shared/data/ chỉ là bản
 * dự phòng lúc build. Hàm trả `null` khi bảng không đọc được hoặc trống — nơi gọi giữ bản JSON. Mỗi bảng
 * hỏng chỉ ghi MỘT dòng cảnh báo mỗi chu kỳ cache (không ngập log khi CSDL tắt).
 *
 * Tên bảng phải nằm trong danh sách trắng BANG_DOC_DUOC (hằng trong mã, không bao giờ lấy từ người dùng).
 */
import { BANG_CAM_NANG, type DongBang } from '../../shared/utils/duLieuSqlThuan'

export const CACHE_DU_LIEU_MS = 5 * 60_000

const BANG_DOC_DUOC = new Set<string>([
  ...BANG_CAM_NANG, 'data_noi_dung_trang_bi', 'cau_hinh_danh_muc', 'cau_hinh_nhom',
])

const cache = new Map<string, { luc: number, dong: DongBang, cho?: Promise<DongBang> }>()

/** Dòng của bảng `ten` (SELECT *), cache CACHE_DU_LIEU_MS; null = không đọc được / trống. */
export async function docBangGame(ten: string): Promise<DongBang> {
  if (!BANG_DOC_DUOC.has(ten) || !/^[a-z0-9_]+$/.test(ten)) throw new Error(`docBangGame: bảng không nằm trong danh sách trắng: ${ten}`)
  const c = cache.get(ten)
  const bay = Date.now()
  if (c && bay - c.luc < CACHE_DU_LIEU_MS) return c.cho ?? c.dong
  const cho = (async (): Promise<DongBang> => {
    try {
      const dong = await q<Record<string, string | number | null>>(dbGame(), `SELECT * FROM \`${ten}\``)
      if (dong.length === 0) {
        console.warn(`[du-lieu-sql] bảng ${ten} TRỐNG — dùng JSON dự phòng lúc build (nạp sql/htbb_day_du.sql hoặc chạy vps/nang_cap_csdl.sh).`)
        return null
      }
      return dong.map(r => ({ ...r }))
    }
    catch (e) {
      console.warn(`[du-lieu-sql] không đọc được bảng ${ten} (${(e as Error).message}) — dùng JSON dự phòng lúc build.`)
      return null
    }
  })()
  cache.set(ten, { luc: bay, dong: c?.dong ?? null, cho })
  const dong = await cho
  cache.set(ten, { luc: bay, dong })
  return dong
}

/** Đọc nhiều bảng song song: { ten: dong | null }. */
export async function docNhieuBangGame<T extends string>(ds: readonly T[]): Promise<Record<T, DongBang>> {
  const kq = await Promise.all(ds.map(t => docBangGame(t)))
  return Object.fromEntries(ds.map((t, i) => [t, kq[i]])) as Record<T, DongBang>
}

/** Xoá cache (test / sau khi đồng bộ dữ liệu). */
export function xoaCacheDuLieuSql(): void {
  cache.clear()
}
