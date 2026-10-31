/**
 * Đổi lượng trên web /account (luật thuần, dùng chung server + trình duyệt + test). Luật gốc ở máy chủ game
 * (com.htbb.pokemon.DoiLuong, real.DoiLuongGame — docs/thiet-ke-game.md 9.1); web chỉ kiểm sơ bộ rồi chèn MỘT dòng
 * lệnh vào web_lenh_nhan_vat, game đổi trên RAM (online: ngay; offline: khi Pokémon vào game). Web KHÔNG ghi tob_char.
 *
 * Chỉ từ LƯỢNG THƯỜNG: lượng khoá không bao giờ là đầu vào, và không có đường nào đổi ngược ra lượng.
 */

export const LENH_DOI_LUONG_KHOA = 'doi_luong_khoa'
export const LENH_DOI_LUONG_XU = 'doi_luong_xu'
export const LOAI_DOI = { khoa: LENH_DOI_LUONG_KHOA, xu: LENH_DOI_LUONG_XU } as const
export type LoaiDoi = keyof typeof LOAI_DOI

export const DOI_LUONG_TOI_DA_MOT_LAN = 1_000_000

/** Ngày theo giờ Việt Nam ('YYYY-MM-DD') — khoá trần ngày và tỉ giá, cùng múi giờ với máy chủ game. */
export function ngayVN(ms: number = Date.now()): string {
  const p = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit', day: '2-digit' })
    .formatToParts(new Date(ms))
  const g = (t: string) => p.find(x => x.type === t)?.value ?? ''
  return `${g('year')}-${g('month')}-${g('day')}`
}

/** Số nhận được: lượng khoá (tiLe mỗi lượng) hoặc xu (tiGia mỗi lượng). <= 0 = không đổi được. */
export function soNhan(loai: LoaiDoi, so: number, tiLe: number, tiGia: number): number {
  const n = Math.trunc(so)
  if (!(n > 0)) return 0
  const heSo = loai === 'khoa' ? Math.trunc(tiLe) : Math.trunc(tiGia)
  return heSo > 0 ? n * heSo : 0
}

export interface DieuKienDoi {
  /** Lượng thường của Pokémon (CSDL; đang online thì theo lần lưu gần nhất). */
  luong: number
  tiLe: number
  /** Tỉ giá hôm nay (xu / 1 lượng); 0 = chưa có / tắt. */
  tiGia: number
  /** Lượng → xu còn đổi được hôm nay cho cả tài khoản (đã trừ yêu cầu đang chờ). */
  conXu: number
  /** Pokémon này đang có yêu cầu đổi chờ chưa xử lý. */
  dangCho: boolean
}

/** Lý do không cho gửi yêu cầu (null = hợp lệ). Máy chủ game kiểm lại tất cả lúc đổi. */
export function loiDoi(loai: LoaiDoi, so: number, d: DieuKienDoi): string | null {
  const n = Math.trunc(Number(so))
  if (!Number.isFinite(n) || n <= 0 || n > DOI_LUONG_TOI_DA_MOT_LAN) return `Số lượng phải từ 1 đến ${DOI_LUONG_TOI_DA_MOT_LAN.toLocaleString('vi-VN')}.`
  if (d.dangCho) return 'Pokémon này đang có một yêu cầu đổi chờ xử lý. Đợi xong hoặc huỷ yêu cầu đó trước.'
  if (n > d.luong) return `Không đủ lượng thường (đang có ${d.luong.toLocaleString('vi-VN')}). Lượng khoá không dùng để đổi.`
  if (loai === 'khoa') {
    if (!(d.tiLe > 0)) return 'Đổi lượng → lượng khoá đang tắt.'
    return null
  }
  if (!(d.tiGia > 0)) return 'Chưa có tỉ giá hôm nay (máy chủ tính lúc 00:00) hoặc chức năng đang tắt.'
  if (n > d.conXu) return `Hôm nay tài khoản chỉ còn đổi được ${Math.max(0, d.conXu).toLocaleString('vi-VN')} lượng → xu.`
  return null
}

/** Giá trung bình (làm tròn) từ tổng xu / tổng lượng đã khớp; 0 = chưa có. */
export function giaTrungBinh(tongXu: number, tongLuong: number): number {
  return tongLuong > 0 ? Math.round(tongXu / tongLuong) : 0
}

/** Mô tả một dòng lệnh đổi lượng (bảng "Yêu cầu đổi"). */
export function moTaLenhDoi(loai: string, so: number): string {
  const n = Math.trunc(Number(so) || 0).toLocaleString('vi-VN')
  if (loai === LENH_DOI_LUONG_KHOA) return `${n} lượng → lượng khoá`
  if (loai === LENH_DOI_LUONG_XU) return `${n} lượng → xu`
  return loai
}
