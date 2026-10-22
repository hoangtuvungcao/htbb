/**
 * Đấu trường Bảo Bối 3v3 (D12) — phần THUẦN dùng chung cho web (/dau-truong, /api/dau-truong, /cam-nang/dau-truong).
 * Số liệu khớp máy chủ com.htbb.pokemon.DauTruongLogic / DauTruongTran / DauTruongDuLieu (server/test/KiemDauTruong
 * kiểm bên đó; tests/dau-truong.test.ts kiểm bên này). Tuần = tuần cúp (thứ Hai 04:00 giờ Việt Nam, CupLogic.tuan).
 * Bảng: tob_dau_truong_mua (điểm mùa), tob_dau_truong_tran (từng trận), tob_dau_truong_doi (tên hiện).
 */

export const DAU_TRUONG = {
  diemBanDau: 1000,
  k: 32,
  phanTramThu: 50,
  diemSan: 100,
  luotNgay: 5,
  lamMoiNgay: 10,
  luongThang: 3,
  thuongThangDau: 2,
  capToc: [20, 40] as const,
  tranLuongTuan: 25,
  tranToiThieuMua: 10,
  giayLuot: 20,
  luotToiDa: 50,
  capToiThieu: 20,
  capChuanHoa: 50,
  cuaSoDiem: [100, 400] as const,
  daiSucManh: [15, 50] as const,
  heSoLap: [100, 50, 25, 0] as const,
  tenBac: ['Tập sự', 'Đồng', 'Bạc', 'Vàng', 'Bạch kim', 'Kim cương', 'Huyền thoại'] as const,
  nguongBac: [900, 1100, 1250, 1400, 1550, 1700] as const,
  /** Sân đấu theo bậc (DauTruongLogic.TEN_SAN); mùa thứ 4 mỗi năm mọi trận ở sân đêm. */
  tenSan: ['Sân cỏ Viridian', 'Sân đá Pewter', 'Hồ nước Cerulean', 'Gym điện Vermilion', 'Sân băng Mahogany', 'Núi lửa Cinnabar', 'Đỉnh rồng Blackthorn', 'Sân đêm Lavender'] as const,
  danhHieu: [
    { id: 9870, ten: 'Quán quân Đấu trường', dieuKien: 'Hạng 1 cuối mùa' },
    { id: 9871, ten: 'Tinh anh Đấu trường', dieuKien: 'Hạng 2–10 cuối mùa' },
    { id: 9872, ten: 'Đấu thủ Đấu trường', dieuKien: 'Hạng 11–100 hoặc từ 1.300 điểm' },
    { id: 9873, ten: 'Đồng Đấu trường', dieuKien: 'Kết thúc mùa ở bậc Đồng' },
    { id: 9874, ten: 'Bạc Đấu trường', dieuKien: 'Kết thúc mùa ở bậc Bạc' },
    { id: 9875, ten: 'Vàng Đấu trường', dieuKien: 'Kết thúc mùa ở bậc Vàng' },
    { id: 9876, ten: 'Bạch kim Đấu trường', dieuKien: 'Kết thúc mùa ở bậc Bạch kim' },
    { id: 9877, ten: 'Kim cương Đấu trường', dieuKien: 'Kết thúc mùa ở bậc Kim cương' },
    { id: 9878, ten: 'Huyền thoại Đấu trường', dieuKien: 'Kết thúc mùa ở bậc Huyền thoại' },
  ],
  /** Uy lực / chính xác / PP chuẩn hoá theo vai trò chiêu (DauTruongDuLieu). */
  vaiTro: [
    { vai: 'Đòn thường', uyLuc: '50', chinhXac: 100, pp: 30 },
    { vai: 'Tấn công', uyLuc: '85', chinhXac: 95, pp: 15 },
    { vai: 'Nhiều đòn', uyLuc: '20 × số đòn', chinhXac: 90, pp: 15 },
    { vai: 'Đánh xa', uyLuc: '80', chinhXac: 100, pp: 15 },
    { vai: 'Vùng', uyLuc: '75', chinhXac: 100, pp: 15 },
    { vai: 'Tối thượng', uyLuc: '120 (sau đó −1 bậc chỉ số tấn công)', chinhXac: 90, pp: 5 },
    { vai: 'Đặc trưng', uyLuc: '100', chinhXac: 95, pp: 10 },
    { vai: 'Khống chế', uyLuc: 'gây trạng thái', chinhXac: 85, pp: 10 },
    { vai: 'Tăng chỉ số', uyLuc: '+1 Tấn công, +1 Tấn công đặc biệt', chinhXac: 100, pp: 10 },
    { vai: 'Khiên / hồi', uyLuc: 'hồi 50% HP', chinhXac: 100, pp: 5 },
  ],
} as const

export function bacDauTruong(diem: number): number {
  let b = 0
  DAU_TRUONG.nguongBac.forEach((n, i) => { if (diem >= n) b = i + 1 })
  return b
}

export function tenBacDauTruong(diem: number): string {
  return DAU_TRUONG.tenBac[bacDauTruong(diem)] ?? DAU_TRUONG.tenBac[0]
}

/** Điểm mới bên tấn công (khớp DauTruongLogic.diemCong): Elo K 32 × hệ số lặp (phần nghìn), sàn 100. */
export function diemCong(a: number, b: number, thang: 0 | 1, heSo = 1000): number {
  const ky = Math.round(1000 / (1 + 10 ** ((b - a) / 400)))
  const d = Math.trunc(Math.round(DAU_TRUONG.k * (thang * 1000 - ky) / 1000) * Math.max(0, Math.min(1000, heSo)) / 1000)
  return Math.max(DAU_TRUONG.diemSan, a + d)
}

export interface DongMua { idChar: number, ten: string, diem: number, thang: number, thua: number, soTran: number }
export interface DongTuan { idChar: number, ten: string, tran: number, thang: number, diemDoi: number, luong: number }

export function dongMuaTu(r: Record<string, unknown>): DongMua {
  return { idChar: Number(r.id_char) || 0, ten: String(r.ten ?? '') || `#${r.id_char}`, diem: Number(r.diem) || 0, thang: Number(r.thang) || 0, thua: Number(r.thua) || 0, soTran: Number(r.so_tran) || 0 }
}

export function dongTuanTu(r: Record<string, unknown>): DongTuan {
  return { idChar: Number(r.cong) || 0, ten: String(r.ten ?? '') || `#${r.cong}`, tran: Number(r.tran) || 0, thang: Number(r.thang) || 0, diemDoi: Number(r.diem_doi) || 0, luong: Number(r.luong) || 0 }
}

/** Bảng mùa: điểm cao trước, bằng điểm thì id nhỏ trước (khớp hạng thưởng mùa của máy chủ). Chỉ người đã đánh. */
export function xepMua(ds: DongMua[]): DongMua[] {
  return ds.filter(d => d.soTran > 0).sort((a, b) => b.diem - a.diem || a.idChar - b.idChar)
}

/** Bảng tuần: điểm mùa kiếm được trong tuần, rồi số trận thắng, rồi ít trận hơn. */
export function xepTuan(ds: DongTuan[]): DongTuan[] {
  return ds.filter(d => d.tran > 0).sort((a, b) => b.diemDoi - a.diemDoi || b.thang - a.thang || a.tran - b.tran || a.idChar - b.idChar)
}
