/**
 * NẠP TIỀN — hàm THUẦN (không database, không auto-import của Nuxt): hằng số, luật gói/món,
 * trần nạp, máy trạng thái đơn, mã đơn, xác thực chữ ký webhook (SePay / Casso / mock) và
 * đọc payload. Phần chạm database ở napTien.ts.
 *
 * Bản sao phía web của HTBB/src/com/htbb/pokemon/NapTienLogic.java — test
 * HTBB/test/KiemNapTien.java đối chiếu các hằng số dưới đây (sửa một bên phải sửa cả bên kia).
 * Thiết kế: docs/van-hanh.md.
 */
import { createHash, createHmac, randomInt, timingSafeEqual } from 'node:crypto'

// ---------------------------------------------------------------- hằng số (khớp Java)

/** 10 lượng / 1.000 đ — cố định, KHÔNG hệ số ngẫu nhiên, KHÔNG khuyến mãi %. */
export const LUONG_MOI_1000_VND = 10
export const TRAN_TUAN_VND = 1000000
export const TRAN_THANG_VND = 2000000
/** Cấu hình lớn hơn bị kẹp về đây. */
export const TRAN_CUNG_THANG_VND = 5000000
/** Nâng trần tự đặt chỉ có hiệu lực sau 72 giờ (người chơi tự bảo vệ mình). */
export const GIO_CHO_NANG_TRAN = 72
/** Đơn chờ thanh toán quá 24 giờ thì hết hạn (tiền đến muộn vẫn được nhận). */
export const GIO_HET_HAN_DON = 24

export const LOAI_GOI = ['luong', 'the_thang', 've_mua', 'thanh_vien'] as const
export type LoaiGoi = typeof LOAI_GOI[number]

export const TRAO_HOP_LE = ['trang_tri', 'mo_rong_tui', 'doi_ten', 'tang_toc', 'dat_ten_pet', 'trang_chieu', 'bua_chuyen_luyen'] as const
// = NapTienLogic.NHOM_CUA_HANG: 4 o tu do trang tri + tien ich + tang toc (dong bo 0.9.8, mot he trang tri)
// 'skin' / 'thu_cuoi' = nhom cu (mon truoc 0.9.8), game hien trong o Trang phuc / Skin thu cuoi
export const NHOM_CUA_HANG = ['trang_phuc', 'hao_quang', 'skin_thu', 'the_dong_hanh', 'tien_ich', 'tang_toc', 'skin', 'thu_cuoi'] as const

/**
 * Tăng tốc luyện cấp (chỉ EXP — khớp com.htbb.pokemon.TangToc + NapTienLogic.kiemTangToc): danh sách CỐ ĐỊNH,
 * giá cố định (lượng), giới hạn mua/tuần tối đa. Bản cao cấp và bản khoá (kiếm bằng chơi) dùng CHUNG trần
 * 60 điểm/ngày, 210 điểm/tuần trong game; không giao dịch, không chỉ số, không ảnh hưởng xu/tỉ lệ rơi.
 */
export const TANG_TOC: Record<string, { gia: number, tuanToiDa: number, diem: number }> = {
  ve_exp_15: { gia: 20, tuanToiDa: 14, diem: 15 },
  ve_exp_2: { gia: 40, tuanToiDa: 7, diem: 30 },
  bua_the_luc: { gia: 30, tuanToiDa: 0, diem: 45 }, // 31/10/2026: không giới hạn mua (0); diem 45 chỉ danh nghĩa — bùa không tính trần điểm, bậc 50% EXP / 50% xu
  uy_thac: { gia: 15, tuanToiDa: 17, diem: 12 },
  keo_hiem: { gia: 40, tuanToiDa: 2, diem: 30 },
}
export const TANG_TOC_DIEM_NGAY = 60
export const TANG_TOC_DIEM_TUAN = 210

/** null = hợp lệ; món trao 'tang_toc' (giống NapTienLogic.kiemTangToc). */
function kiemTangToc(m: MonCuaHang): string | null {
  const t = TANG_TOC[m.ma]
  if (!t) return `Món tăng tốc chỉ gồm: ${Object.keys(TANG_TOC).join(', ')}.`
  if (m.nhom !== 'tang_toc') return 'Món tăng tốc phải thuộc nhóm tang_toc.'
  if (m.gia_luong !== t.gia) return `Giá ${m.ma} cố định ${t.gia} lượng.`
  if (t.tuanToiDa === 0) {
    if (!Number.isInteger(m.gioi_han) || m.gioi_han < 0) return `Giới hạn tuần ${m.ma} phải >= 0 (0 = không giới hạn).`
  } else if (!Number.isInteger(m.gioi_han) || m.gioi_han < 1 || m.gioi_han > t.tuanToiDa) return `Giới hạn tuần ${m.ma} phải trong 1..${t.tuanToiDa}.`
  const gd = Number(m.giao_dich_duoc) === 1 || m.giao_dich_duoc === true
  if (gd || m.han_ngay !== 0 || m.nhom_xoay !== 0) return 'Món tăng tốc: không giao dịch, không hạn, luôn bán.'
  return null
}
/**
 * Thẻ đặt tên Pokémon (khớp NapTienLogic.kiemDatTen): chỉ mã the_dat_ten_pet, nhóm tien_ich, không giao dịch, không hạn,
 * luôn bán, gioi_han = 0. Giá + số lần mua / tuần thật lấy từ cấu hình dat_ten_pet.gia_luong / dat_ten_pet.mua_tuan_toi_da.
 */
/**
 * Bùa chuyển luyện (khớp NapTienLogic.kiemBuaChuyenLuyen): chỉ mã bua_chuyen_luyen, nhóm tien_ich, không hạn, luôn bán,
 * gioi_han = 0, giao_dich_duoc = 0 (bùa mua bằng lượng vào túi KHÔNG khoá; Kecleon bán cùng bùa bằng lượng khoá, đồ khoá).
 */
function kiemBuaChuyenLuyen(m: MonCuaHang): string | null {
  if (m.ma !== 'bua_chuyen_luyen') return 'Món bua_chuyen_luyen chỉ có mã bua_chuyen_luyen.'
  if (m.nhom !== 'tien_ich') return 'Bùa chuyển luyện thuộc nhóm tien_ich.'
  if (!Number.isInteger(m.gia_luong) || m.gia_luong < GIA_MON_TOI_THIEU || m.gia_luong > GIA_MON_TOI_DA) {
    return `Giá phải trong ${GIA_MON_TOI_THIEU}–${GIA_MON_TOI_DA} lượng.`
  }
  const gd = Number(m.giao_dich_duoc) === 1 || m.giao_dich_duoc === true
  if (gd || m.han_ngay !== 0 || m.nhom_xoay !== 0 || m.gioi_han !== 0) {
    return 'Bùa chuyển luyện: giao_dich_duoc = 0, không hạn, luôn bán, giới hạn = 0.'
  }
  return null
}
function kiemDatTen(m: MonCuaHang): string | null {
  if (m.ma !== 'the_dat_ten_pet') return 'Món dat_ten_pet chỉ có mã the_dat_ten_pet.'
  if (m.nhom !== 'tien_ich') return 'Thẻ đặt tên thuộc nhóm tien_ich.'
  if (!Number.isInteger(m.gia_luong) || m.gia_luong < GIA_MON_TOI_THIEU || m.gia_luong > GIA_MON_TOI_DA) {
    return `Giá phải trong ${GIA_MON_TOI_THIEU}–${GIA_MON_TOI_DA} lượng.`
  }
  const gd = Number(m.giao_dich_duoc) === 1 || m.giao_dich_duoc === true
  if (gd || m.han_ngay !== 0 || m.nhom_xoay !== 0 || m.gioi_han !== 0) {
    return 'Thẻ đặt tên: không giao dịch, không hạn, luôn bán, giới hạn = 0 (đặt ở cấu hình dat_ten_pet.mua_tuan_toi_da).'
  }
  return null
}
export const GIA_MON_TOI_THIEU = 10
export const GIA_MON_TOI_DA = 5000
export const SO_NHOM_XOAY = 4

/** Bảng chữ của mã đơn — bỏ 0/O, 1/I (người chơi có khi gõ tay). */
export const KY_TU_MA = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'

export const TRANG_THAI_NAP = ['cho_thanh_toan', 'da_thanh_toan', 'da_cong', 'that_bai', 'hoan_tien', 'cho_thu_hoi', 'da_thu_hoi'] as const
export type TrangThaiNap = typeof TRANG_THAI_NAP[number]

/** Từ cấm trong tên món bán bằng lượng (thêm cho whitelist cách trao) — khớp NapTienLogic.TU_CAM. */
export const TU_CAM = ['exp', 'kinh nghiệm', 'kinh nghiem', 'x2', 'x3', 'tấn công', 'tan cong', 'phòng thủ', 'phong thu', 'hp',
  'chí mạng', 'chi mang', 'tỉ lệ', 'ti le', 'tỷ lệ', 'may mắn', 'may man', 'bảo hộ', 'bao ho', 'cường hoá', 'cuong hoa',
  'nâng cấp', 'nang cap', 'đá', 'chỉ số', 'chi so', 'sức mạnh', 'suc manh', 'tiềm năng', 'tiem nang', 'vé quay', 've quay',
  'rương', 'ruong', 'hộp quà', 'hop qua']

// ---------------------------------------------------------------- gói / món

export function luongTheoGia(giaVnd: number): number {
  return giaVnd > 0 ? Math.trunc(giaVnd * LUONG_MOI_1000_VND / 1000) : 0
}

/** null = hợp lệ; chuỗi = lý do (giống NapTienLogic.kiemGoi). */
export function kiemGoi(loai: string, giaVnd: number, luong: number): string | null {
  if (!Number.isInteger(giaVnd) || giaVnd < 10000 || giaVnd > 2000000) return 'Giá phải trong 10.000 – 2.000.000 đ.'
  if (giaVnd % 1000 !== 0) return 'Giá phải chẵn nghìn.'
  if (loai === 'luong') {
    return luong === luongTheoGia(giaVnd) ? null : `Gói lượng phải đúng tỷ giá ${LUONG_MOI_1000_VND} lượng / 1.000 đ (không khuyến mãi %).`
  }
  if (loai === 'the_thang' || loai === 've_mua' || loai === 'thanh_vien') return luong === 0 ? null : 'Thẻ tháng / vé mùa / thành viên không kèm lượng.'
  return 'Loại gói không hợp lệ.'
}

export interface MonCuaHang {
  ma: string
  ten: string
  nhom: string
  trao: string
  gia_luong: number
  han_ngay: number
  gioi_han: number
  nhom_xoay: number
  giao_dich_duoc: number | boolean
  co_chi_so: number | boolean
}

function chuaTu(s: string, tu: string): boolean {
  let i = s.indexOf(tu)
  while (i >= 0) {
    const dau = i === 0 || !/[\p{L}\p{N}]/u.test(s[i - 1]!)
    const j = i + tu.length
    const cuoi = j >= s.length || !/[\p{L}\p{N}]/u.test(s[j]!)
    if (dau && cuoi) return true
    i = s.indexOf(tu, i + 1)
  }
  return false
}

/** null = hợp lệ (giống NapTienLogic.kiemMon): whitelist cách trao, không chỉ số, từ cấm, giá. */
export function kiemMon(m: MonCuaHang): string | null {
  if (!/^[a-z0-9_]{2,40}$/.test(m.ma ?? '')) return 'Mã món chỉ gồm a-z 0-9 _ (2–40 ký tự).'
  if (Number(m.co_chi_so) !== 0 && m.co_chi_so !== false) return 'Cấm bán món có chỉ số.'
  if (!(TRAO_HOP_LE as readonly string[]).includes(m.trao)) return 'Cách trao phải là trang_tri / mo_rong_tui / doi_ten / tang_toc / dat_ten_pet / trang_chieu / bua_chuyen_luyen.'
  if (m.trao === 'tang_toc') return kiemTangToc(m)
  if (m.trao === 'bua_chuyen_luyen') return kiemBuaChuyenLuyen(m)
  if (m.trao === 'dat_ten_pet') return kiemDatTen(m)
  if (!(NHOM_CUA_HANG as readonly string[]).includes(m.nhom)) return 'Nhóm không hợp lệ.'
  if (!Number.isInteger(m.gia_luong) || m.gia_luong < GIA_MON_TOI_THIEU || m.gia_luong > GIA_MON_TOI_DA) {
    return `Giá phải trong ${GIA_MON_TOI_THIEU}–${GIA_MON_TOI_DA} lượng.`
  }
  if (!Number.isInteger(m.nhom_xoay) || m.nhom_xoay < 0 || m.nhom_xoay > SO_NHOM_XOAY) return `Nhóm xoay 0..${SO_NHOM_XOAY}.`
  const ten = (m.ten ?? '').toLowerCase()
  if (ten.trim() === '') return 'Thiếu tên.'
  for (const c of TU_CAM) {
    if (chuaTu(ten, c)) return `Tên chứa từ cấm "${c}" (nghi là đồ sức mạnh).`
  }
  const gd = Number(m.giao_dich_duoc) === 1 || m.giao_dich_duoc === true
  if (gd && (m.han_ngay > 0 || m.trao !== 'trang_tri')) return 'Chỉ trang trí vĩnh viễn mới được giao dịch.'
  return null
}

// ---------------------------------------------------------------- trần nạp

export function kepTran(cauHinh: number, macDinh: number): number {
  if (!(cauHinh > 0)) return macDinh
  return Math.min(cauHinh, TRAN_CUNG_THANG_VND)
}

/** null = được tạo đơn; chuỗi = lý do (giống NapTienLogic.kiemTran). */
export function kiemTran(daTuan: number, daThang: number, giaMoi: number, tranTuan: number, tranThang: number, tranTuDat: number): string | null {
  if (!(giaMoi > 0)) return 'Số tiền không hợp lệ.'
  let thang = kepTran(tranThang, TRAN_THANG_VND)
  if (tranTuDat > 0) thang = Math.min(thang, tranTuDat)
  const tuan = Math.min(kepTran(tranTuan, TRAN_TUAN_VND), thang)
  if (daTuan + giaMoi > tuan) return `Vượt trần nạp tuần (${vndThuan(tuan)}). Tuần này đã ${vndThuan(daTuan)}.`
  if (daThang + giaMoi > thang) return `Vượt trần nạp tháng (${vndThuan(thang)}). Tháng này đã ${vndThuan(daThang)}.`
  return null
}

function vndThuan(n: number): string {
  return `${Math.trunc(n).toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.')} đ`
}

// ---------------------------------------------------------------- trạng thái

/** Máy trạng thái (giống NapTienLogic.chuyenHopLe). */
export function chuyenHopLe(tu: string, den: string): boolean {
  switch (tu) {
    case 'cho_thanh_toan': return den === 'da_thanh_toan' || den === 'that_bai'
    case 'that_bai': return den === 'da_thanh_toan'
    case 'da_thanh_toan': return den === 'da_cong' || den === 'hoan_tien'
    case 'da_cong': return den === 'cho_thu_hoi'
    case 'cho_thu_hoi': return den === 'da_thu_hoi'
    default: return false
  }
}

// ---------------------------------------------------------------- mã đơn

export function taoMaDon(): string {
  let s = 'HT'
  for (let i = 0; i < 8; i++) s += KY_TU_MA[randomInt(0, KY_TU_MA.length)]
  return s
}

/** Tìm mã HTxxxxxxxx trong nội dung chuyển khoản (giống NapTienLogic.timMaDon). */
export function timMaDon(noiDung: string | null | undefined): string | null {
  const m = (noiDung ?? '').match(/(?<![A-Z0-9])HT([A-HJ-NP-Z2-9]{8})(?![A-Z0-9])/i)
  return m ? `HT${m[1]!.toUpperCase()}` : null
}

/** Khoá idempotent từ trình duyệt: 8–64 ký tự an toàn, còn lại bỏ. */
export function khoaIdemHopLe(v: unknown): string | null {
  return typeof v === 'string' && /^[A-Za-z0-9_-]{8,64}$/.test(v) ? v : null
}

// ---------------------------------------------------------------- thời gian (ứng dụng ghi, không NOW())

function hai(n: number): string {
  return n < 10 ? `0${n}` : String(n)
}

/** 'YYYY-MM-DD HH:MM:SS' giờ máy chủ (VPS đặt Asia/Ho_Chi_Minh) — NOW() của NovaDB là UTC. */
export function gioDb(d: Date = new Date()): string {
  return `${d.getFullYear()}-${hai(d.getMonth() + 1)}-${hai(d.getDate())} ${hai(d.getHours())}:${hai(d.getMinutes())}:${hai(d.getSeconds())}`
}

/** Đầu tuần (thứ Hai 00:00) và đầu tháng của d, dạng gioDb. */
export function dauTuanThang(d: Date = new Date()): { tuan: string, thang: string } {
  const t = new Date(d.getFullYear(), d.getMonth(), d.getDate())
  t.setDate(t.getDate() - ((t.getDay() + 6) % 7))
  return { tuan: gioDb(t), thang: gioDb(new Date(d.getFullYear(), d.getMonth(), 1)) }
}

// ---------------------------------------------------------------- chữ ký webhook

/** So sánh thời gian hằng (băm về cùng độ dài trước). */
export function soKhoa(that: string, goiLen: string): boolean {
  const a = createHash('sha256').update(that, 'utf8').digest()
  const b = createHash('sha256').update(goiLen, 'utf8').digest()
  return timingSafeEqual(a, b) && that.length === goiLen.length
}

/** SePay: header `Authorization: Apikey <key>`. */
export function xacThucSepay(authHeader: string | null | undefined, apiKey: string): boolean {
  if (!apiKey) return false
  const m = (authHeader ?? '').match(/apikey\s+(\S+)/i)
  return !!m && soKhoa(apiKey, m[1]!)
}

/** Đọc header dạng `t=<giây>,v1=<hex>`. */
export function docChuKy(h: string | null | undefined): { t: number, v1: string } | null {
  const m = (h ?? '').match(/t=(\d{9,13}),\s*v1=([0-9a-f]{32,256})/i)
  return m ? { t: Number(m[1]), v1: m[2]!.toLowerCase() } : null
}

function giayTu(t: number): number {
  return t > 1e12 ? Math.floor(t / 1000) : t
}

/** Sắp khoá object đệ quy (Casso ký trên JSON đã sắp khoá). */
export function sapKhoa(v: unknown): unknown {
  if (Array.isArray(v)) return v.map(sapKhoa)
  if (v && typeof v === 'object') {
    const o: Record<string, unknown> = {}
    for (const k of Object.keys(v as Record<string, unknown>).sort()) o[k] = sapKhoa((v as Record<string, unknown>)[k])
    return o
  }
  return v
}

/**
 * Casso webhook v2: `X-Casso-Signature: t=<ts>,v1=<hex HMAC-SHA512(secret, ts + "." + JSON đã sắp khoá của data)>`.
 * PHẢI đối chiếu lại với tài liệu Casso hiện hành trước khi bật thật (docs/van-hanh.md).
 * Chênh giờ > 5 phút = từ chối (chống phát lại).
 */
export function xacThucCasso(header: string | null | undefined, data: unknown, secret: string, bayGioGiay = Math.floor(Date.now() / 1000)): boolean {
  if (!secret) return false
  const ck = docChuKy(header)
  if (!ck || Math.abs(bayGioGiay - giayTu(ck.t)) > 300) return false
  const ky = createHmac('sha512', secret).update(`${ck.t}.${JSON.stringify(sapKhoa(data))}`).digest('hex')
  return soKhoa(ky, ck.v1)
}

/** Mock / sandbox: `X-HTBB-Signature: t=<ts>,v1=<hex HMAC-SHA256(secret, ts + "." + thân thô)>`. */
export function kyMock(secret: string, t: number, raw: string): string {
  return createHmac('sha256', secret).update(`${t}.${raw}`).digest('hex')
}

export function xacThucMock(header: string | null | undefined, raw: string, secret: string, bayGioGiay = Math.floor(Date.now() / 1000)): boolean {
  if (!secret) return false
  const ck = docChuKy(header)
  if (!ck || Math.abs(bayGioGiay - giayTu(ck.t)) > 300) return false
  return soKhoa(kyMock(secret, ck.t, raw), ck.v1)
}

// ---------------------------------------------------------------- đọc payload

export interface GiaoDichVao {
  /** Mã giao dịch phía cổng (trùng = gọi lặp). */
  maGd: string
  soTien: number
  noiDung: string
  /** Mã thanh toán SePay tự nhận ra (cột `code`) — khớp TRƯỚC nội dung. */
  maThanhToan?: string
  /** Số tài khoản nhận tiền theo cổng — phải đúng tài khoản của mình. */
  soTaiKhoan?: string
}

function soNguyen(v: unknown): number {
  if (typeof v === 'number') return Number.isFinite(v) ? Math.trunc(v) : 0
  if (typeof v === 'string') {
    const m = v.match(/^\s*([+-]?\d+)/)
    return m ? Number(m[1]) : 0
  }
  return 0
}

function chuoi(v: unknown): string {
  return typeof v === 'string' ? v : (typeof v === 'number' ? String(v) : '')
}

/**
 * SePay webhook (docs.sepay.vn/tich-hop-webhooks.html): { id, gateway, transactionDate, accountNumber,
 * code, content, transferType: 'in' | 'out', transferAmount, accumulated, subAccount, referenceCode,
 * description }. Tiền ra -> 'bo_qua'. maGd = `id` của SePay (cũng là id trong user API -> job dò
 * giao dịch lỡ webhook trùng khoá, không cộng hai lần).
 */
export function docSepay(d: Record<string, unknown>): GiaoDichVao | null | 'bo_qua' {
  if (d.transferType !== 'in') return 'bo_qua'
  const maGd = chuoi(d.id)
  if (!maGd) return null
  return {
    maGd,
    soTien: soNguyen(d.transferAmount),
    noiDung: `${chuoi(d.content)} ${chuoi(d.description)}`.trim(),
    maThanhToan: chuoi(d.code),
    soTaiKhoan: chuoi(d.subAccount) || chuoi(d.accountNumber),
  }
}

/**
 * Một dòng SePay user API GET /userapi/transactions/list: { id, account_number, transaction_date,
 * amount_in, amount_out, transaction_content, reference_number, code, sub_account }.
 */
export function docSepayApi(d: Record<string, unknown>): GiaoDichVao | null {
  const soTien = soNguyen(d.amount_in)
  const maGd = chuoi(d.id)
  if (!maGd || soTien <= 0) return null
  return {
    maGd,
    soTien,
    noiDung: chuoi(d.transaction_content),
    maThanhToan: chuoi(d.code),
    soTaiKhoan: chuoi(d.sub_account) || chuoi(d.account_number),
  }
}

/** Mã đơn của một giao dịch: cột code (SePay) trước, rồi tới nội dung. */
export function maDonCuaGiaoDich(gd: GiaoDichVao): string | null {
  return timMaDon(gd.maThanhToan) ?? timMaDon(gd.noiDung)
}

/** Tài khoản nhận có đúng của mình không (bỏ khoảng trắng; cổng không gửi số TK = không kiểm được -> chấp nhận). */
export function dungTaiKhoan(gd: GiaoDichVao, cuaMinh: string): boolean {
  const a = (gd.soTaiKhoan ?? '').replace(/\s+/g, '')
  return a === '' || a === cuaMinh.replace(/\s+/g, '')
}

// ---------------------------------------------------------------- cấu hình máy thật (fail closed)

export const MK_NAP_MAC_DINH = 'doi-mat-khau-nay'

/**
 * Lý do KHÔNG được bật nạp trên máy thật (rỗng = được). config.ts gọi lúc khởi động: có lý do thì
 * ép HTBB_NAP_BAT=false và in log rõ ràng.
 */
export function kiemCauHinhNapThat(c: {
  secret: string
  adminNaptienPassword: string
  accountNo: string
  accountName: string
  bankBin: string
  sepayApiKey: string
  sepayApiToken: string
  sandbox: boolean
}): string[] {
  const loi: string[] = []
  const yeu = (s: string, n: number) => s.length < n || /^(.)\1+$/.test(s) || /^(123|abc|password|changeme|doi)/i.test(s)
  if (yeu(c.secret, 32)) loi.push('HTBB_SECRET trống/yếu (cần >= 32 ký tự ngẫu nhiên: openssl rand -hex 32)')
  if (c.adminNaptienPassword === MK_NAP_MAC_DINH || yeu(c.adminNaptienPassword, 12)) {
    loi.push('HTBB_ADMIN_NAPTIEN_PASSWORD đang là mặc định hoặc < 12 ký tự')
  }
  if (!/^\d{6,19}$/.test(c.accountNo) || c.accountNo === '0000000000') loi.push('HTBB_NAP_ACCOUNT_NO (số tài khoản nhận tiền) chưa đặt')
  if (c.accountName === '' || c.accountName === 'NGUYEN VAN A') loi.push('HTBB_NAP_ACCOUNT_NAME (tên chủ tài khoản, viết hoa không dấu) chưa đặt')
  if (!/^\d{6}$/.test(c.bankBin)) loi.push('HTBB_NAP_BANK_BIN phải là mã BIN 6 số (VD 970423 TPBank)')
  if (yeu(c.sepayApiKey, 24)) loi.push('HTBB_SEPAY_WEBHOOK_APIKEY trống/yếu (>= 24 ký tự) — nạp tự động cần webhook SePay')
  if (c.sepayApiToken !== '' && c.sepayApiToken.length < 20) loi.push('HTBB_SEPAY_API_TOKEN quá ngắn (để trống nếu không dùng job dò giao dịch)')
  if (c.sandbox) loi.push('HTBB_NAP_SANDBOX=1 không được bật trên máy thật')
  return loi
}

// ---------------------------------------------------------------- VietQR deep link (mở app ngân hàng)

/** Vài app phổ biến (mã app theo api.vietqr.io/v2/android-app-deeplinks — kiểm lại khi đổi). */
export const APP_NGAN_HANG: { app: string, ten: string }[] = [
  { app: 'vcb', ten: 'Vietcombank' }, { app: 'tcb', ten: 'Techcombank' }, { app: 'mb', ten: 'MB Bank' },
  { app: 'tpb', ten: 'TPBank' }, { app: 'bidv', ten: 'BIDV' }, { app: 'icb', ten: 'VietinBank' },
  { app: 'acb', ten: 'ACB' }, { app: 'vpb', ten: 'VPBank' },
]

/** https://dl.vietqr.io/pay?app=<app>&ba=<số TK>@<BIN>&am=<tiền>&tn=<nội dung>. */
export function vietQrDeepLink(app: string, bin: string, soTk: string, soTien: number, noiDung: string): string {
  return `https://dl.vietqr.io/pay?${new URLSearchParams({ app, ba: `${soTk}@${bin}`, am: String(Math.trunc(soTien)), tn: noiDung })}`
}

/** Casso v2: { error: 0, data: { id, reference, description, amount } } (v1: data là mảng — lấy từng phần tử). */
export function docCasso(d: Record<string, unknown>): GiaoDichVao[] {
  const ds = Array.isArray(d.data) ? d.data : (d.data && typeof d.data === 'object' ? [d.data] : [])
  const ra: GiaoDichVao[] = []
  for (const x of ds as Record<string, unknown>[]) {
    const soTien = soNguyen(x.amount)
    if (soTien <= 0) continue // tiền ra
    const maGd = chuoi(x.id) || chuoi(x.reference) || chuoi(x.tid)
    if (!maGd) continue
    ra.push({ maGd, soTien, noiDung: `${chuoi(x.description)} ${chuoi(x.reference)}` })
  }
  return ra
}

/** Mock: { ma_gd, so_tien, noi_dung }. */
export function docMock(d: Record<string, unknown>): GiaoDichVao | null {
  const maGd = chuoi(d.ma_gd)
  if (!/^[A-Za-z0-9_.:-]{1,64}$/.test(maGd)) return null
  return { maGd, soTien: soNguyen(d.so_tien), noiDung: chuoi(d.noi_dung) }
}

/** Khớp số tiền: đủ (hoặc dư) = xác nhận; thiếu = KHÔNG xác nhận, admin xử lý tay. */
export function khopSoTien(phaiTra: number, daNhan: number): 'du' | 'du_thua' | 'thieu' {
  if (daNhan < phaiTra) return 'thieu'
  return daNhan === phaiTra ? 'du' : 'du_thua'
}
