/**
 * VÙNG HOANG DÃ xoay vòng (loai-bo-sung) — cùng công thức với máy chủ (com.htbb.pokemon.LoaiDuLieu.chuKy):
 * tuần k = floor((ngày − ngày gốc) / 7), chu kỳ = k mod n (ngày theo giờ máy chủ, Asia/Ho_Chi_Minh). Lớp THUẦN.
 */
export interface TuanXoay { chuKy: number, ten: string, moTa: string, theHe: number }
export interface LichVungHoangDa {
  ngayGoc: string
  tuan: TuanXoay[]
  /** nhom 0: bản đồ cấp 52–84; 1: bản đồ cấp thấp (chỉ dạng đầu T1–T3) */
  banDo: { id: number, ten: string, vung: number, nhom: number }[]
  soLoai: Record<number, number>
}

const MS_NGAY = 86_400_000

/** Số ngày epoch của ngày lịch "YYYY-MM-DD". */
export function ngayEpoch(iso: string): number {
  const [y, m, d] = iso.slice(0, 10).split('-').map(Number)
  return Math.floor(Date.UTC(y!, m! - 1, d!) / MS_NGAY)
}

/** Giờ đổi tuần chung của máy chủ (thứ Hai 05:00, LichNam.GIO_DOI_TUAN): lịch xoay đổi ngày lúc 05:00. */
export const GIO_DOI_TUAN = 5

/** Ngày epoch của lịch xoay theo giờ máy chủ (UTC+7), đổi ngày lúc 05:00 (khớp LoaiQuaiGame.ngayHienTai). */
export function homNayMayChu(bay = Date.now()): number {
  return Math.floor((bay + 7 * 3_600_000 - GIO_DOI_TUAN * 3_600_000) / MS_NGAY)
}

export function chuKyXoay(ngay: number, goc: number, n: number): number {
  if (n <= 0) return -1
  const k = Math.floor((ngay - goc) / 7)
  return ((k % n) + n) % n
}

/** Tuần hiện tại và các tuần tiếp theo: { chuKy, batDau (ngày epoch thứ Hai) }. */
export function cacTuanToi(lich: LichVungHoangDa, ngay: number, so = 3): { chuKy: number, batDau: number }[] {
  const goc = ngayEpoch(lich.ngayGoc)
  // chưa tới ngày gốc (thứ Hai mở lịch): chưa có tuần nào chạy - liệt kê từ tuần đầu tiên (máy chủ cũng chưa xoay)
  const dau = ngay < goc ? goc : goc + Math.floor((ngay - goc) / 7) * 7
  const kq: { chuKy: number, batDau: number }[] = []
  for (let i = 0; i < so; i++) kq.push({ chuKy: chuKyXoay(dau + i * 7, goc, lich.tuan.length), batDau: dau + i * 7 })
  return kq
}

export function ngayTuEpoch(ngay: number): string {
  const d = new Date(ngay * MS_NGAY)
  return `${String(d.getUTCDate()).padStart(2, '0')}/${String(d.getUTCMonth() + 1).padStart(2, '0')}`
}
