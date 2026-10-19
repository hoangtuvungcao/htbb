/**
 * Biến đổi THUẦN: dòng bảng SQL của game (htbb_db) -> đúng hình dạng JSON mà website đang dùng.
 *
 * Nguồn duy nhất của các con số này là CSDL game (bảng sinh bởi tools/luu_tru_sinh_du_lieu/pokemon/sinh_sql_lau_dai.py /
 * sinh_noi_dung.py). Lúc chạy, web đọc bảng qua server/utils/duLieuSql.ts (có cache) rồi gọi các hàm ở
 * đây; JSON trong shared/data/ chỉ còn là bản dự phòng lúc build (CSDL không có / bảng trống).
 *
 * tests/du-lieu-sql.test.ts chạy CHÍNH các hàm này trên dòng đọc từ sql/htbb_day_du.sql và so khớp
 * từng phần tử với JSON đóng gói — lệch một số là test hỏng.
 *
 * Quy ước: hàm nhận mảng dòng (thứ tự bất kỳ, tự sắp xếp) và trả null khi dữ liệu thiếu/lạ, để nơi gọi
 * giữ bản JSON thay vì hiện dữ liệu hỏng.
 */
import type { DongSql } from './sqlDump.ts'

export type DongBang = DongSql[] | null | undefined

const so = (v: unknown): number => Number(v)
/** Giống `soHoac` của scripts/dong-bo-cam-nang.mjs: rỗng / 0-chuỗi-rỗng -> null. */
const soHoac = (v: unknown): number | null => (v === '' || v == null || Number.isNaN(Number(v)) ? null : Number(v))
const chu = (v: unknown): string => (v == null ? '' : String(v))
const coDong = (r: DongBang): r is DongSql[] => Array.isArray(r) && r.length > 0
const theo = (...k: string[]) => (a: DongSql, b: DongSql) => {
  for (const x of k) {
    const d = so(a[x]) - so(b[x])
    if (d) return d
  }
  return 0
}

/** Tiền / kiểu trang trí: cùng bảng tên với tools/luu_tru_sinh_du_lieu/pokemon/sinh_noi_dung.py (ghi_web). */
const TEN_TIEN: Record<string, string> = { xu: 'xu', luong_khoa: 'lượng khoá', luong: 'lượng', khong_ban: 'không bán', huy_chuong_gym: 'Huy chương Gym' }
const TEN_KIEU_TRANG_TRI: Record<string, string> = { danh_hieu: 'Danh hiệu', khung: 'Khung', trang_phuc: 'Trang phục', hieu_ung: 'Hiệu ứng' }

// ================================================================ bảng cấp độ (khu quản trị)

export interface BangCapDoWeb { capDoToiDa: number, expMain: number[], expRequire: number[] }

/**
 * data_cap_do_lau_dai.exp_cu = real.LevelDetail.expRequire[cap] (bảng EXP CŨ, cấp 1..90; 0 từ cấp 91).
 * expRequire = [0, exp_cu(1..90), 0]; expMain[i] = tổng exp_cu(1..i) (expMain[0] = 0) — y như bang-cap-do.json.
 */
export function bangCapDoTuSql(rows: DongBang): BangCapDoWeb | null {
  if (!coDong(rows)) return null
  const r = [...rows].sort(theo('cap'))
  const exp: number[] = []
  for (let i = 0; i < r.length; i++) {
    if (so(r[i]!.cap) !== i + 1) return null
    const e = so(r[i]!.exp_cu)
    if (!(e > 0)) break
    exp.push(e)
  }
  if (exp.length === 0) return null
  const expMain = [0]
  for (const e of exp) expMain.push(expMain[expMain.length - 1]! + e)
  if (!expMain.every(Number.isSafeInteger)) return null
  return { capDoToiDa: exp.length, expMain, expRequire: [0, ...exp, 0] }
}

// ================================================================ giftcode an toàn

/** Trang bị phát được bằng giftcode: id_item (> 0) của danh mục data_noi_dung_trang_bi, tăng dần, không trùng. */
export function trangBiGiftcodeTuSql(rows: DongBang): number[] | null {
  if (!coDong(rows)) return null
  const ids = new Set<number>()
  for (const r of rows) {
    const id = Number(r.id_item)
    if (Number.isInteger(id) && id > 0) ids.add(id)
  }
  return ids.size ? [...ids].sort((a, b) => a - b) : null
}

// ================================================================ cam-nang.json

/**
 * capDo.bang: mỗi cấp 1..100 (data_cap_do_lau_dai). Giờ chơi (gioTichLuy, gioCap) là số thiết kế làm tròn
 * 0,01 giờ từ số thực của bộ sinh; SQL chỉ giữ gio_tich_luy_phan_nghin (làm tròn 0,001) nên làm tròn lại
 * lệch 0,01 ở vài cấp -> hai cột giờ lấy từ bản JSON `goc` theo cấp, mọi cột còn lại từ SQL.
 */
export function bangCapTuSql<D extends { cap: number, gioTichLuy: number, gioCap: number }>(rows: DongBang, goc: D[]) {
  if (!coDong(rows)) return null
  return [...rows].sort(theo('cap')).map((x) => {
    const q = so(x.quai_moi_gio)
    const xu = so(x.xu_moi_quai)
    const g = goc.find(d => d.cap === so(x.cap))
    return {
      cap: so(x.cap),
      expLenCap: so(x.exp_len_cap),
      gioTichLuy: g?.gioTichLuy ?? Number((so(x.gio_tich_luy_phan_nghin) / 1000).toFixed(2)),
      gioCap: g?.gioCap ?? 0,
      quaiMoiGio: q,
      expMoiQuai: so(x.mob_exp),
      xuMoiQuai: xu,
      xuMoiGio: q * xu,
    }
  })
}

/** Bảng khoá -> giá trị của data_kinh_te_lau_dai. */
function kinhTeMap(rows: DongBang): Map<string, string> | null {
  if (!coDong(rows)) return null
  return new Map(rows.map(r => [chu(r.khoa), chu(r.gia_tri)]))
}

const soKhoa = (m: Map<string, string>, k: string): number | undefined => {
  const v = m.get(k)
  return v !== undefined && v.trim() !== '' && Number.isFinite(Number(v)) ? Number(v) : undefined
}

/** Các số kinh tế có trong data_kinh_te_lau_dai (phần còn lại của kinhTe không có trong SQL). */
export function kinhTeTuSql(rows: DongBang): Partial<{ tiLeRoiXu: number, thueCho: number, phiDangBan: number, tieuHaoPhanTram: number, suaDoPhanTram: number, giftcodeToiDaThang: number }> | null {
  const m = kinhTeMap(rows)
  if (!m) return null
  const ra: Record<string, number> = {}
  const dat = (ten: string, khoa: string) => {
    const v = soKhoa(m, khoa)
    if (v !== undefined) ra[ten] = v
  }
  dat('tiLeRoiXu', 'roi.xu.phan_tram')
  dat('thueCho', 'thue.cho')
  dat('phiDangBan', 'thue.dang_ban')
  dat('tieuHaoPhanTram', 'tieu_hao.thuoc_phan_tram_thu_nhap')
  dat('suaDoPhanTram', 'tieu_hao.sua_do_phan_tram_thu_nhap')
  dat('giftcodeToiDaThang', 'giftcode.toi_da_thang')
  return Object.keys(ra).length ? ra : null
}

/** capDo.nangLuong (exp.nang_luong_phut, exp.nang_luong_tich_phut, exp.bac_met "120:20,120:5"). */
export function nangLuongTuSql(rows: DongBang): { phutNgay: number, phutTichToiDa: number, bacMet: { phut: number, tocDo: number }[] } | null {
  const m = kinhTeMap(rows)
  if (!m) return null
  const phutNgay = soKhoa(m, 'exp.nang_luong_phut')
  const phutTichToiDa = soKhoa(m, 'exp.nang_luong_tich_phut')
  const met = m.get('exp.bac_met')
  if (phutNgay === undefined || phutTichToiDa === undefined || !met || !/^\d+:\d+(,\d+:\d+)*$/.test(met)) return null
  return { phutNgay, phutTichToiDa, bacMet: met.split(',').map((p) => { const [a, b] = p.split(':'); return { phut: Number(a), tocDo: Number(b) } }) }
}

/** Bậc trang bị trong data_nang_cap_lau_dai: bac -> {ten, cap}. */
function bacNangCap(rows: DongSql[]): Map<number, { ten: string, cap: number }> {
  const m = new Map<number, { ten: string, cap: number }>()
  for (const r of rows) if (!m.has(so(r.bac))) m.set(so(r.bac), { ten: chu(r.ten_bac), cap: so(r.cap_bac) })
  return m
}

/**
 * trangBi.nangCap (+1..+15 của bậc 0; chi_so_phan_tram trong SQL là CỘNG DỒN -> lấy hiệu), chiPhiNangCap[bậc][k-1]
 * và tên/cấp của trangBi.bac (ngày mở / giá bậc chưa có trong SQL: giữ từ `bacGoc`).
 */
export function nangCapTuSql<B extends { bac: number, ten: string, cap: number }>(rows: DongBang, bacGoc: B[]) {
  if (!coDong(rows)) return null
  const r = [...rows].sort(theo('bac', 'cong'))
  const bac = bacNangCap(r)
  const b0 = r.filter(x => so(x.bac) === Math.min(...bac.keys()))
  let truoc = 0
  const nangCap = b0.map((x) => {
    const cs = so(x.chi_so_phan_tram)
    const d = { plus: so(x.cong), tiLe: so(x.ti_le), da: so(x.da), buaLuongKhoa: so(x.bua_luong_khoa), chiSoPhanTram: cs - truoc }
    truoc = cs
    return d
  })
  const chiPhiNangCap = [...bac.keys()].sort((a, b) => a - b).map(b => r.filter(x => so(x.bac) === b).map(x => so(x.xu)))
  const bacMoi = [...bac.entries()].sort((a, b) => a[0] - b[0]).map(([b, v]) => {
    const g = bacGoc.find(x => x.bac === b)
    return { ...(g ?? { ngayMo: 0, gia: 0 }), bac: b, ten: v.ten, cap: v.cap } as unknown as B
  })
  return { nangCap, chiPhiNangCap, bac: bacMoi }
}

/** suKien: lịch 52 tuần (data_su_kien_nam), cùng cách xử lý tên/cơ chế với scripts/dong-bo-cam-nang.mjs. */
export function suKienTuSql(rows: DongBang) {
  if (!coDong(rows)) return null
  const ds = [...rows].sort(theo('tuan')).map((r) => {
    const ten1Goc = chu(r.ten_nam1)
    const pt = chu(r.phan_thuong)
    return {
      tuan: so(r.tuan),
      thang: so(r.thang),
      ten1: ten1Goc.replace(/\s*\(tuần \d\/\d\)$/, '').replace(/\s*\(theo âm lịch: tuần [\d-]+\)/, ''),
      ten2: chu(r.ten_nam2),
      phan: ten1Goc.match(/\(tuần (\d\/\d)\)$/)?.[1] ?? '',
      loai: chu(r.loai),
      coChe: chu(r.co_che),
      soNgay: so(r.so_ngay),
      phanThuong: pt === '—' ? '' : pt,
      mocGiuChan: chu(r.moc_giu_chan),
    }
  })
  for (const sk of ds) {
    const he = sk.ten1.match(/hệ (\S+)/)?.[1]
    if (he && /hệ X\b/.test(sk.coChe)) sk.coChe = sk.coChe.replace(/hệ X\b/g, `hệ ${he}`)
  }
  for (const sk of ds) {
    const m = sk.coChe.match(/^Như tuần (\d+)(.*)$/)
    if (!m) continue
    const goc = ds.find(x => x.tuan === Number(m[1]))
    if (goc && !/^Như tuần/.test(goc.coChe)) sk.coChe = `${goc.coChe}${m[2]}`
  }
  return ds
}

// ================================================================ cam-nang-noi-dung.json

/** cuaHang: gom theo cua_hang, giữ thứ tự stt (data_noi_dung_cua_hang). */
export function cuaHangTuSql(rows: DongBang) {
  if (!coDong(rows)) return null
  const ra: { ten: string, hang: { vatPham: string, gia: number | null, tien: string, cap: number | null, gioiHan: string }[] }[] = []
  for (const r of [...rows].sort(theo('stt'))) {
    const ten = chu(r.cua_hang)
    if (!ten || ten === 'Cửa hàng') continue
    const tien = TEN_TIEN[chu(r.tien)]
    if (tien === undefined) return null
    let c = ra.find(x => x.ten === ten)
    if (!c) { c = { ten, hang: [] }; ra.push(c) }
    c.hang.push({ vatPham: chu(r.ten), gia: soHoac(r.gia), tien, cap: soHoac(r.cap), gioiHan: chu(r.gioi_han) })
  }
  return ra
}

/** gym: data_noi_dung_gym; bacThuong = tên bậc (data_nang_cap_lau_dai.ten_bac). */
export function gymTuSql(rows: DongBang, nangCap: DongBang) {
  if (!coDong(rows) || !coDong(nangCap)) return null
  const bac = bacNangCap(nangCap)
  return [...rows].sort(theo('stt')).map(r => ({
    so: so(r.stt),
    huyHieu: chu(r.huy_hieu),
    noi: chu(r.dia_danh),
    he: chu(r.he),
    cap: so(r.cap),
    truong: chu(r.ten_boss).replace(/^Trưởng Gym [^:]+:\s*/, ''),
    hp: soHoac(r.hp),
    tanCong: soHoac(r.tan_cong),
    phongThu: soHoac(r.phong_thu),
    bacThuong: bac.get(so(r.bac_thuong))?.ten ?? '',
  }))
}

/** cheTao: theo bậc rồi id thành phẩm (data_noi_dung_che_tao, khoá thanh_pham = "item:<id>"). */
export function cheTaoTuSql(rows: DongBang) {
  if (!coDong(rows)) return null
  const id = (r: DongSql) => Number(chu(r.thanh_pham).split(':')[1])
  return [...rows].sort((a, b) => so(a.bac) - so(b.bac) || id(a) - id(b)).map(r => ({
    ten: chu(r.ten), cap: so(r.cap), da: so(r.da), phu: `${so(r.so_nl_phu)} ${chu(r.nguyen_lieu_phu)}`, xu: so(r.xu), noi: chu(r.npc),
  }))
}

/** nhanhEevee: theo số Pokédex (data_noi_dung_chieu_nhanh_eevee). */
export function nhanhEeveeTuSql(rows: DongBang) {
  if (!coDong(rows)) return null
  return [...rows].sort(theo('dex')).map(r => ({
    ten: chu(r.ten), he: chu(r.he), vai: chu(r.vai_tro), chieu: `${chu(r.chieu_o2)}, ${chu(r.chieu_o8)}, ${chu(r.chieu_o10)}`, cap: so(r.cap_mo),
  }))
}

/** trangTri: data_noi_dung_trang_tri theo stt. Giá 0 = không bán (null). */
export function trangTriTuSql(rows: DongBang) {
  if (!coDong(rows)) return null
  const ra = []
  for (const r of [...rows].sort(theo('stt'))) {
    const loai = TEN_KIEU_TRANG_TRI[chu(r.kieu)]
    const tien = TEN_TIEN[chu(r.tien)]
    if (loai === undefined || tien === undefined) return null
    ra.push({ mua: chu(r.ten_mua), loai, ten: chu(r.ten), gia: so(r.gia) ? so(r.gia) : null, tien, nguon: chu(r.nguon) })
  }
  return ra
}

/** suKienVatPham: data_noi_dung_su_kien_vat_pham theo stt. */
export function suKienVatPhamTuSql(rows: DongBang) {
  if (!coDong(rows)) return null
  return [...rows].sort(theo('stt')).map(r => ({
    tuan: so(r.tuan), ten: chu(r.su_kien), ten2: chu(r.su_kien_nam2), vatPham: chu(r.vat_pham), toiDaTuan: soHoac(r.so_luong_toi_da_tuan), doiLay: chu(r.doi_lay),
  }))
}

/** bossTheGioi.lich: data_noi_dung_boss_the_gioi theo tuần. */
export function lichBossTuSql(rows: DongBang) {
  if (!coDong(rows)) return null
  return [...rows].sort(theo('tuan')).map(r => ({
    tuan: so(r.tuan), nam1: chu(r.ten_nam1), nam2: chu(r.ten_nam2), he: chu(r.he), luotNgay: so(r.lan_moi_ngay),
  }))
}

// ================================================================ ghép

/** Tên bảng game mà cẩm nang đọc (danh sách trắng cho server/utils/duLieuSql.ts). */
export const BANG_CAM_NANG = [
  'data_cap_do_lau_dai', 'data_su_kien_nam', 'data_nang_cap_lau_dai', 'data_kinh_te_lau_dai',
  'data_noi_dung_cua_hang', 'data_noi_dung_gym', 'data_noi_dung_che_tao', 'data_noi_dung_chieu_nhanh_eevee',
  'data_noi_dung_trang_tri', 'data_noi_dung_su_kien_vat_pham', 'data_noi_dung_boss_the_gioi',
] as const

export type BangCamNang = Partial<Record<(typeof BANG_CAM_NANG)[number], DongBang>>

interface CamNangGoc {
  capDo: { bang: { cap: number, gioTichLuy: number, gioCap: number }[], nangLuong: unknown }
  trangBi: { bac: { bac: number, ten: string, cap: number }[], nangCap: unknown, chiPhiNangCap: unknown }
  kinhTe: Record<string, unknown>
  suKien: unknown
}
interface NoiDungGoc {
  cuaHang: unknown, gym: unknown, cheTao: unknown, nhanhEevee: unknown, trangTri: unknown, suKienVatPham: unknown
  bossTheGioi: { lich: unknown }
}

/**
 * Phần đè lên JSON đóng gói: chỉ gồm mục đọc được từ SQL (mục nào bảng thiếu/lạ thì vắng mặt -> giữ JSON).
 * `nguon` liệt kê mục đã lấy từ SQL (để trang quản trị / log biết đang dùng gì).
 */
export function camNangDeTuSql(b: BangCamNang, goc: CamNangGoc) {
  const camNang: Record<string, unknown> = {}
  const noiDung: Record<string, unknown> = {}
  const nguon: string[] = []
  const dat = (dich: Record<string, unknown>, ten: string, v: unknown) => {
    if (v != null) { dich[ten] = v; nguon.push(ten) }
  }
  const bangCap = bangCapTuSql(b.data_cap_do_lau_dai, goc.capDo.bang)
  const nangLuong = nangLuongTuSql(b.data_kinh_te_lau_dai)
  if (bangCap) nguon.push('capDo.bang')
  if (nangLuong) nguon.push('capDo.nangLuong')
  if (bangCap || nangLuong) camNang.capDo = { ...goc.capDo, ...(bangCap ? { bang: bangCap } : {}), ...(nangLuong ? { nangLuong } : {}) }
  const nc = nangCapTuSql(b.data_nang_cap_lau_dai, goc.trangBi.bac)
  if (nc) { camNang.trangBi = { ...goc.trangBi, ...nc }; nguon.push('trangBi.nangCap') }
  const kt = kinhTeTuSql(b.data_kinh_te_lau_dai)
  if (kt) { camNang.kinhTe = { ...goc.kinhTe, ...kt }; nguon.push('kinhTe') }
  dat(camNang, 'suKien', suKienTuSql(b.data_su_kien_nam))
  dat(noiDung, 'cuaHang', cuaHangTuSql(b.data_noi_dung_cua_hang))
  dat(noiDung, 'gym', gymTuSql(b.data_noi_dung_gym, b.data_nang_cap_lau_dai))
  dat(noiDung, 'cheTao', cheTaoTuSql(b.data_noi_dung_che_tao))
  dat(noiDung, 'nhanhEevee', nhanhEeveeTuSql(b.data_noi_dung_chieu_nhanh_eevee))
  dat(noiDung, 'trangTri', trangTriTuSql(b.data_noi_dung_trang_tri))
  dat(noiDung, 'suKienVatPham', suKienVatPhamTuSql(b.data_noi_dung_su_kien_vat_pham))
  const lich = lichBossTuSql(b.data_noi_dung_boss_the_gioi)
  if (lich) { noiDung.bossTheGioi = { lich }; nguon.push('bossTheGioi.lich') }
  return { camNang, noiDung, nguon }
}

/** Ghép phần đè (từ API) vào JSON gốc — nông ở cấp mục, riêng bossTheGioi ghép thêm một tầng. */
export function ghepCamNang<T extends object>(goc: T, de: Record<string, unknown> | null | undefined): T {
  if (!de) return goc
  const ra: Record<string, unknown> = { ...goc }
  for (const [k, v] of Object.entries(de)) {
    const cu = (goc as Record<string, unknown>)[k]
    ra[k] = k === 'bossTheGioi' && cu && typeof cu === 'object' && v && typeof v === 'object' ? { ...cu, ...v } : v
  }
  return ra as T
}
