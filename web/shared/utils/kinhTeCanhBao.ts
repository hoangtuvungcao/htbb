/**
 * Chỉ số dẫn xuất + LUẬT CẢNH BÁO của trang Kinh tế (/admin/kinh-te).
 *
 * Nguồn số liệu:
 *   - tob_kinh_te_ngay: game ghi mỗi đêm 04:00 (HTBB/src/data/DatabaseLauDai.java, com.htbb.pokemon.ChiSoKinhTe).
 *     so_nguoi = người hoạt động 7 ngày; chi_so_gia = xuMoiGio(cấp trung vị) = số xu 1 giờ cày ở cấp trung vị;
 *     tong_xu = gold + kho_xu (của người hoạt động); cung_tien_thuc = tong_xu / so_nguoi / chi_so_gia (giờ thu nhập
 *     mỗi người); top1 = % xu nằm ở 1% người giàu nhất; xu_vao / xu_ra = tổng Char.addXu / subXu kể từ lần ghi trước;
 *     muc = mức của chính game: 0 xanh (cung tiền thực ≤ 10 giờ), 1 vàng (≤ 20 giờ), 2 đỏ (> 20 giờ).
 *   - tob_log_all_item aclog 'nhanxu' / 'truxu': "trước + n = sau tu vi tri <nguồn>" (Char.addXu / subXu).
 *
 * File thuần (không import gì) — tests/kinh-te-canh-bao.test.ts gọi trực tiếp.
 */

export interface NgayKinhTe {
  ngay: string
  soNguoi: number
  capTrungVi: number
  chiSoGia: number
  tongXu: number
  tongLuong: number
  tongLuongKhoa: number
  cungTienThuc: number
  /** % (đã chia 100). */
  top1: number
  xuVao: number
  xuRa: number
  muc: number
}

export type MucCanhBao = 'thong_tin' | 'canh_bao' | 'nghiem_trong'

export interface CanhBaoKinhTe {
  ma: string
  muc: MucCanhBao
  tieuDe: string
  chiTiet: string
}

/** Ngưỡng — trang hiện nguyên bảng này để admin biết luật. */
export const NGUONG_KINH_TE = {
  /** Xu ròng vào trong ngày (xu_vao − xu_ra) / tổng xu: % cảnh báo / nghiêm trọng. */
  xuRongPhanTram: [5, 10],
  /** Tỉ phần top 1%: % cảnh báo / nghiêm trọng. */
  top1PhanTram: [40, 60],
  /** Tổng xu tăng sau 7 ngày: % cảnh báo / nghiêm trọng. */
  cungXu7NgayPhanTram: [30, 60],
  /** Tổng lượng tăng sau 7 ngày: % cảnh báo. */
  cungLuong7NgayPhanTram: 30,
  /** Một nhân vật nhận trong 24 giờ: bội số của chỉ số giá (= số giờ thu nhập ở cấp trung vị). */
  dotBienGioThuNhap: [24, 100],
  /** Đột biến so với 24 giờ trước đó: gấp bao nhiêu lần (kèm điều kiện ≥ 10 giờ thu nhập). */
  dotBienBoiSo: 5,
  dotBienBoiSoToiThieuGio: 10,
  /** Dòng tob_kinh_te_ngay mới nhất cũ hơn bao nhiêu ngày thì báo job đêm không chạy. */
  duLieuCuNgay: 2,
  /** Chỉ số giá dùng khi chưa có dòng nào (xu / giờ). */
  chiSoGiaMacDinh: 100000,
} as const

export const MO_TA_LUAT_KINH_TE: { ma: string, ten: string, luat: string }[] = [
  { ma: 'muc_game', ten: 'Mức của game', luat: 'Cột muc trong tob_kinh_te_ngay: vàng khi cung tiền thực > 10 giờ thu nhập/người, đỏ khi > 20 giờ.' },
  { ma: 'xu_rong', ten: 'Xu ròng vào', luat: `(xu vào − xu ra) trong ngày > ${NGUONG_KINH_TE.xuRongPhanTram[0]}% tổng xu: cảnh báo; > ${NGUONG_KINH_TE.xuRongPhanTram[1]}%: nghiêm trọng.` },
  { ma: 'top1', ten: 'Tập trung tài sản', luat: `1% người giàu nhất giữ > ${NGUONG_KINH_TE.top1PhanTram[0]}% tổng xu: cảnh báo; > ${NGUONG_KINH_TE.top1PhanTram[1]}%: nghiêm trọng.` },
  { ma: 'cung_xu_7', ten: 'Cung xu 7 ngày', luat: `Tổng xu tăng > ${NGUONG_KINH_TE.cungXu7NgayPhanTram[0]}% so với 7 ngày trước: cảnh báo; > ${NGUONG_KINH_TE.cungXu7NgayPhanTram[1]}%: nghiêm trọng.` },
  { ma: 'cung_luong_7', ten: 'Cung lượng 7 ngày', luat: `Tổng lượng (thường + khoá) tăng > ${NGUONG_KINH_TE.cungLuong7NgayPhanTram}% so với 7 ngày trước: cảnh báo.` },
  { ma: 'dot_bien', ten: 'Đột biến một Pokémon', luat: `Nhận > ${NGUONG_KINH_TE.dotBienGioThuNhap[0]} giờ thu nhập (chỉ số giá × ${NGUONG_KINH_TE.dotBienGioThuNhap[0]}) xu trong 24 giờ: cảnh báo; > ${NGUONG_KINH_TE.dotBienGioThuNhap[1]} giờ: nghiêm trọng; hoặc gấp ≥ ${NGUONG_KINH_TE.dotBienBoiSo} lần 24 giờ trước đó và ≥ ${NGUONG_KINH_TE.dotBienBoiSoToiThieuGio} giờ thu nhập: cảnh báo. Đếm từ log nhanxu.` },
  { ma: 'du_lieu_cu', ten: 'Dữ liệu cũ', luat: `Dòng tob_kinh_te_ngay mới nhất cũ hơn ${NGUONG_KINH_TE.duLieuCuNgay} ngày: job 04:00 có thể không chạy (sv.lau_dai tắt?).` },
]

/** % thay đổi từ a sang b; null khi a ≤ 0. */
export function phanTramDoi(a: number | null | undefined, b: number | null | undefined): number | null {
  if (a == null || b == null || !Number.isFinite(a) || !Number.isFinite(b) || a <= 0) return null
  return Math.round(((b - a) / a) * 1000) / 10
}

/** Dòng cách dòng cuối `n` ngày lịch (dòng cuối cùng có ngày ≤ ngày cuối − n). */
export function dongTruoc(ds: NgayKinhTe[], n: number): NgayKinhTe | null {
  const cuoi = ds.at(-1)
  if (!cuoi) return null
  const moc = Date.parse(`${cuoi.ngay}T00:00:00Z`) - n * 86400000
  for (let i = ds.length - 2; i >= 0; i--) {
    if (Date.parse(`${ds[i]!.ngay}T00:00:00Z`) <= moc) return ds[i]!
  }
  return null
}

export interface ChiSoDanXuat {
  xuMoiNguoi: number | null
  doiXu7: number | null
  doiXu30: number | null
  doiXuMoiNguoi7: number | null
  doiXuMoiNguoi30: number | null
  doiGia7: number | null
  doiGia30: number | null
  doiLuong7: number | null
  /** (xu vào − xu ra) / tổng xu của ngày mới nhất, %. */
  xuRongPhanTram: number | null
}

const xmn = (d: NgayKinhTe | null) => (d && d.soNguoi > 0 ? d.tongXu / d.soNguoi : null)

export function chiSoDanXuat(ds: NgayKinhTe[]): ChiSoDanXuat {
  const c = ds.at(-1) ?? null
  const t7 = dongTruoc(ds, 7)
  const t30 = dongTruoc(ds, 30)
  const luong = (d: NgayKinhTe | null) => (d ? d.tongLuong + d.tongLuongKhoa : null)
  return {
    xuMoiNguoi: xmn(c) == null ? null : Math.round(xmn(c)!),
    doiXu7: phanTramDoi(t7?.tongXu, c?.tongXu),
    doiXu30: phanTramDoi(t30?.tongXu, c?.tongXu),
    doiXuMoiNguoi7: phanTramDoi(xmn(t7), xmn(c)),
    doiXuMoiNguoi30: phanTramDoi(xmn(t30), xmn(c)),
    doiGia7: phanTramDoi(t7?.chiSoGia, c?.chiSoGia),
    doiGia30: phanTramDoi(t30?.chiSoGia, c?.chiSoGia),
    doiLuong7: phanTramDoi(luong(t7), luong(c)),
    xuRongPhanTram: c && c.tongXu > 0 ? Math.round(((c.xuVao - c.xuRa) / c.tongXu) * 1000) / 10 : null,
  }
}

// ---------------------------------------------------------------------------- log xu

export interface DongLogXu {
  truoc: number
  /** Có dấu: + nhận, − trừ. */
  delta: number
  sau: number
  nguon: string
}

const MAU_LOG_XU = /^(-?\d+) ([+-]) (-?\d+) = (-?\d+) tu vi tri ?(.*)$/

/** Đọc info của aclog nhanxu / truxu; sai định dạng -> null. */
export function docLogXu(info: string): DongLogXu | null {
  const m = MAU_LOG_XU.exec(String(info ?? '').trim())
  if (!m) return null
  const n = Number(m[3])
  return { truoc: Number(m[1]), delta: m[2] === '-' ? -n : n, sau: Number(m[4]), nguon: chuanNguon(m[5] ?? '') }
}

/** Gộp nguồn: số trong nguồn (id quái, id NPC...) thay bằng #, tối đa 60 ký tự. */
export function chuanNguon(s: string): string {
  const t = s.trim().replace(/\d+/g, '#').replace(/\s+/g, ' ')
  return (t || '(không rõ)').slice(0, 60)
}

export interface NguonXu { nguon: string, soLan: number, tong: number }

export function tongHopNguon(infos: string[], gioiHan = 30): NguonXu[] {
  const m = new Map<string, NguonXu>()
  for (const i of infos) {
    const d = docLogXu(i)
    if (!d) continue
    const g = m.get(d.nguon) ?? { nguon: d.nguon, soLan: 0, tong: 0 }
    g.soLan++
    g.tong += Math.abs(d.delta)
    m.set(d.nguon, g)
  }
  return [...m.values()].sort((a, b) => b.tong - a.tong).slice(0, gioiHan)
}

export interface NhanXuNhanVat { ten: string, nhan24: number, nhan24Truoc: number }

/** Tổng xu mỗi nhân vật nhận trong [bayGio−24h, bayGio] và 24 giờ trước đó (giây UNIX). */
export function nhanXuTheoNhanVat(dong: { ten: string, info: string, t: number }[], bayGio: number): NhanXuNhanVat[] {
  const m = new Map<string, NhanXuNhanVat>()
  for (const r of dong) {
    const d = docLogXu(r.info)
    if (!d || d.delta <= 0) continue
    const tuoi = bayGio - r.t
    if (tuoi < 0 || tuoi > 172800) continue
    const g = m.get(r.ten) ?? { ten: r.ten, nhan24: 0, nhan24Truoc: 0 }
    if (tuoi <= 86400) g.nhan24 += d.delta
    else g.nhan24Truoc += d.delta
    m.set(r.ten, g)
  }
  return [...m.values()].sort((a, b) => b.nhan24 - a.nhan24)
}

export interface DotBien extends NhanXuNhanVat { gioThuNhap: number, muc: MucCanhBao, lyDo: string }

export function timDotBien(ds: NhanXuNhanVat[], chiSoGia: number): DotBien[] {
  const gia = chiSoGia > 0 ? chiSoGia : NGUONG_KINH_TE.chiSoGiaMacDinh
  const [g1, g2] = NGUONG_KINH_TE.dotBienGioThuNhap
  const ra: DotBien[] = []
  for (const d of ds) {
    const gio = d.nhan24 / gia
    let muc: MucCanhBao | null = null
    let lyDo = ''
    if (gio > g2) { muc = 'nghiem_trong'; lyDo = `nhận ${Math.round(gio)} giờ thu nhập trong 24 giờ` }
    else if (gio > g1) { muc = 'canh_bao'; lyDo = `nhận ${Math.round(gio)} giờ thu nhập trong 24 giờ` }
    else if (gio >= NGUONG_KINH_TE.dotBienBoiSoToiThieuGio && d.nhan24 >= NGUONG_KINH_TE.dotBienBoiSo * Math.max(1, d.nhan24Truoc)) {
      muc = 'canh_bao'
      lyDo = d.nhan24Truoc > 0 ? `gấp ${Math.round(d.nhan24 / d.nhan24Truoc)} lần 24 giờ trước` : 'tăng vọt (24 giờ trước không nhận gì)'
    }
    if (muc) ra.push({ ...d, gioThuNhap: Math.round(gio * 10) / 10, muc, lyDo })
  }
  return ra
}

// ---------------------------------------------------------------------------- luật

const MUC_GAME: Record<number, string> = { 0: 'xanh', 1: 'vàng', 2: 'đỏ' }
const so = (n: number) => Math.round(n).toLocaleString('vi-VN')

/**
 * Tính danh sách cảnh báo. `homNay` = 'yyyy-mm-dd' (giờ máy chủ web) để biết dữ liệu cũ.
 * Thứ tự: nghiêm trọng trước.
 */
export function tinhCanhBaoKinhTe(ds: NgayKinhTe[], dotBien: DotBien[], homNay: string): CanhBaoKinhTe[] {
  const ra: CanhBaoKinhTe[] = []
  const c = ds.at(-1)
  const N = NGUONG_KINH_TE
  if (!c) {
    ra.push({ ma: 'du_lieu_cu', muc: 'thong_tin', tieuDe: 'Chưa có số liệu kinh tế hằng đêm', chiTiet: 'Game ghi dòng đầu tiên vào 04:00 sau khi bật chế độ dài hạn (sv.lau_dai).' })
  } else {
    const cu = Math.floor((Date.parse(`${homNay}T00:00:00Z`) - Date.parse(`${c.ngay}T00:00:00Z`)) / 86400000)
    if (cu > N.duLieuCuNgay) {
      ra.push({ ma: 'du_lieu_cu', muc: 'canh_bao', tieuDe: `Số liệu đã cũ ${cu} ngày`, chiTiet: `Dòng mới nhất là ${c.ngay}. Job 04:00 có thể không chạy.` })
    }
    if (c.muc >= 1) {
      ra.push({
        ma: 'muc_game', muc: c.muc >= 2 ? 'nghiem_trong' : 'canh_bao',
        tieuDe: `Game tự đánh giá mức ${MUC_GAME[c.muc] ?? c.muc}`,
        chiTiet: `Cung tiền thực ${c.cungTienThuc.toLocaleString('vi-VN')} giờ thu nhập/người (xanh ≤ 10, vàng ≤ 20).`,
      })
    }
    const d = chiSoDanXuat(ds)
    if (d.xuRongPhanTram != null && d.xuRongPhanTram > N.xuRongPhanTram[0]) {
      ra.push({
        ma: 'xu_rong', muc: d.xuRongPhanTram > N.xuRongPhanTram[1] ? 'nghiem_trong' : 'canh_bao',
        tieuDe: `Xu ròng vào ${d.xuRongPhanTram}% tổng xu trong ngày ${c.ngay}`,
        chiTiet: `Vào ${so(c.xuVao)}, ra ${so(c.xuRa)}, tổng đang giữ ${so(c.tongXu)}.`,
      })
    }
    if (c.top1 > N.top1PhanTram[0]) {
      ra.push({
        ma: 'top1', muc: c.top1 > N.top1PhanTram[1] ? 'nghiem_trong' : 'canh_bao',
        tieuDe: `Top 1% giữ ${c.top1.toLocaleString('vi-VN')}% tổng xu`,
        chiTiet: `Ngưỡng ${N.top1PhanTram[0]}% / ${N.top1PhanTram[1]}%. Xem bảng người giữ nhiều bên dưới.`,
      })
    }
    if (d.doiXu7 != null && d.doiXu7 > N.cungXu7NgayPhanTram[0]) {
      ra.push({
        ma: 'cung_xu_7', muc: d.doiXu7 > N.cungXu7NgayPhanTram[1] ? 'nghiem_trong' : 'canh_bao',
        tieuDe: `Tổng xu tăng ${d.doiXu7}% sau 7 ngày`,
        chiTiet: d.doiXuMoiNguoi7 == null ? '' : `Xu mỗi người hoạt động thay đổi ${d.doiXuMoiNguoi7}%.`,
      })
    }
    if (d.doiLuong7 != null && d.doiLuong7 > N.cungLuong7NgayPhanTram) {
      ra.push({ ma: 'cung_luong_7', muc: 'canh_bao', tieuDe: `Tổng lượng tăng ${d.doiLuong7}% sau 7 ngày`, chiTiet: 'Gồm lượng thường và lượng khoá của người hoạt động.' })
    }
  }
  for (const b of dotBien.slice(0, 20)) {
    ra.push({ ma: 'dot_bien', muc: b.muc, tieuDe: `${b.ten}: ${b.lyDo}`, chiTiet: `24 giờ qua nhận ${so(b.nhan24)} xu; 24 giờ trước đó ${so(b.nhan24Truoc)} xu.` })
  }
  const thuTu: Record<MucCanhBao, number> = { nghiem_trong: 0, canh_bao: 1, thong_tin: 2 }
  return ra.sort((a, b) => thuTu[a.muc] - thuTu[b.muc])
}
