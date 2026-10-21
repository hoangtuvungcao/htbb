/**
 * Cúp PvP tuần (0.9.7) — phần THUẦN dùng chung cho web (/cup, /api/cup). Khớp server com.htbb.pokemon.CupLogic:
 * tuần cúp = thứ Hai 04:00 giờ Việt Nam → thứ Hai tuần sau 04:00 (cùng mốc LichNam.thuHaiTuanIso / "tuần này" của web),
 * chỉ số tuần liên tục `tuanCup` (tuần 0 = thứ Hai 05/01/1970 04:00). Bảng điểm: tob_cup_dang_ky (máy chủ game ghi).
 * Test: tests/cup-pvp.test.ts.
 */

const NGAY = 86_400_000
const LECH_VN = 7 * 3_600_000
const GIO_DOI_TUAN = 4

/** Chỉ số tuần cúp liên tục của thời điểm `ms`. */
export function tuanCup(ms: number): number {
  const ngay = Math.floor((ms + LECH_VN - GIO_DOI_TUAN * 3_600_000) / NGAY)
  return Math.floor((ngay - 4) / 7)
}

/** Mốc mở (ms) của tuần cúp `t`: thứ Hai 04:00 giờ Việt Nam. */
export function moTuanCup(t: number): number {
  return (t * 7 + 4) * NGAY + GIO_DOI_TUAN * 3_600_000 - LECH_VN
}

export interface DongCup { charId: number, doiId: number, ten: string, cap: number, diem: number, thang: number, hoa: number, thua: number, hang: number }
export interface DoiCup { doiId: number, ten: string, cap: number, diem: number, thang: number, hoa: number, thua: number, hang: number }

/** Gộp dòng đăng ký thành đội (Cúp Đôi: 2 người cùng doi_id), xếp: hạng chốt > điểm > thắng > ít thua. */
export function gopDoi(ds: DongCup[]): DoiCup[] {
  const m = new Map<number, DoiCup>()
  for (const d of ds) {
    const x = m.get(d.doiId)
    if (x) {
      x.ten = `${x.ten} & ${d.ten}`
      x.cap = Math.max(x.cap, d.cap)
      continue
    }
    m.set(d.doiId, { doiId: d.doiId, ten: d.ten, cap: d.cap, diem: d.diem, thang: d.thang, hoa: d.hoa, thua: d.thua, hang: d.hang })
  }
  return [...m.values()].sort((a, b) => {
    const ha = a.hang > 0 ? a.hang : 1e9
    const hb = b.hang > 0 ? b.hang : 1e9
    if (ha !== hb) return ha - hb
    if (a.diem !== b.diem) return b.diem - a.diem
    if (a.thang !== b.thang) return b.thang - a.thang
    return a.thua - b.thua
  })
}

/** Chuẩn hoá một dòng CSDL. */
export function dongCupTuDong(r: Record<string, unknown>): DongCup {
  const n = (v: unknown) => Number(v) || 0
  return {
    charId: n(r.char_id), doiId: n(r.doi_id), ten: String(r.ten ?? ''), cap: n(r.cap), diem: n(r.diem),
    thang: n(r.thang), hoa: n(r.hoa), thua: n(r.thua), hang: n(r.hang),
  }
}
