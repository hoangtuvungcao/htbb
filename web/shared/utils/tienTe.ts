/**
 * Tiền tệ trong game (cùng quy ước client game.pokemon.TienTe): biểu tượng /tien/<loai>_<32|64>.png
 * (client_jar/tools/tao_icon_tien.py), tên đầy đủ, chú thích, số kiểu Việt Nam. Không viết tắt "$", "lk", "lg".
 */
export type LoaiTien = 'xu' | 'luong' | 'luong_khoa' | 'su_kien' | 'vinh_du'

export const TIEN_TE: Record<LoaiTien, { ten: string, goiY: string }> = {
  xu: { ten: 'Xu', goiY: 'Xu: tiền chơi, kiếm từ quái / nhiệm vụ / bán đồ' },
  luong: { ten: 'Lượng', goiY: 'Lượng: tiền nạp / thưởng, mua đồ quý, giao dịch được' },
  luong_khoa: { ten: 'Lượng khoá', goiY: 'Lượng khoá: lượng thưởng, không giao dịch được' },
  su_kien: { ten: 'Điểm sự kiện', goiY: 'Điểm sự kiện: đổi quà trong sự kiện đang diễn ra' },
  vinh_du: { ten: 'Vinh dự', goiY: 'Vinh dự: kiếm từ PvP / sàn đấu, đổi quà vinh dự' },
}

/** Nhận loại tiền từ nhãn cột / chữ ("Xu", "Lượng", "Lượng khoá"...); null nếu không phải tiền. */
export function loaiTienTuNhan(nhan: string | null | undefined): LoaiTien | null {
  const s = (nhan ?? '').trim().toLowerCase()
  if (s === 'xu') return 'xu'
  if (s === 'lượng') return 'luong'
  if (s === 'lượng khoá' || s === 'lượng khóa') return 'luong_khoa'
  if (s === 'vinh dự') return 'vinh_du'
  return null
}

/** Số đầy đủ, dấu chấm nghìn (như so() của htbb.ts - tách riêng để kiểm thử không cần Nuxt). */
export function soTien(n: number | null | undefined): string {
  const x = Math.trunc(Number(n ?? 0))
  if (!Number.isFinite(x)) return '0'
  const s = Math.abs(x).toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.')
  return x < 0 ? `-${s}` : s
}

/** Số gọn: 12500 -> "12,5 N", 1250000 -> "1,25 Tr" (cắt, không làm tròn lên); < 10.000 viết đủ. */
export function soGon(n: number | null | undefined): string {
  const v = Math.trunc(Number(n ?? 0))
  if (!Number.isFinite(v)) return '0'
  const a = Math.abs(v)
  const dau = v < 0 ? '-' : ''
  const phan = (x: number, dv: number) => {
    const nguyen = Math.floor(x / dv)
    if (nguyen >= 100) return soTien(nguyen)
    const chuSo = nguyen >= 10 ? 1 : 2
    const le = Math.floor(((x % dv) / dv) * 10 ** chuSo)
    const leChu = le.toString().padStart(chuSo, '0').replace(/0+$/, '')
    return leChu ? `${nguyen},${leChu}` : `${nguyen}`
  }
  if (a < 10000) return dau + soTien(a)
  if (a < 1e6) return `${dau}${phan(a, 1e3)} N`
  if (a < 1e9) return `${dau}${phan(a, 1e6)} Tr`
  return `${dau}${phan(a, 1e9)} Tỉ`
}
