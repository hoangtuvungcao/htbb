/**
 * Lịch sự kiện năm theo ngày thật (chủ game duyệt 19/10/2026) — phần THUẦN dùng chung cho web (trang chủ, cẩm nang,
 * tin tự động, /admin/sukien). Nguồn lịch: máy chủ game (com.htbb.pokemon.LichNam) ghi mỗi lần diễn ra vào
 * htbb_db.tob_su_kien_lich; web chỉ đọc (API /api/su-kien) và tính trạng thái / thông báo ở đây.
 *
 * Mọi giờ là giờ Việt Nam (Asia/Ho_Chi_Minh, UTC+7, không có giờ mùa hè), dạng 'YYYY-MM-DD HH:mm:ss' như CSDL trả.
 * Mốc mở / đóng mặc định 04:00 (trước bảo trì hằng ngày 04:30–05:30) — sự kiện có hiệu lực sau lần khởi động lại.
 *
 * Test: tests/su-kien-nam.test.ts.
 */

/** Một lần diễn ra của một sự kiện (một dòng tob_su_kien_lich, đã áp ghi đè của admin). */
export interface SuKienLan {
  ma: string
  nam: number
  lan: number
  ten: string
  nhom: string
  ngayLe: string
  moTa: string
  phanThuong: string
  /** 'YYYY-MM-DD HH:mm:ss' giờ Việt Nam. */
  mo: string
  dong: string
  /** 0 theo lịch, 1 tắt năm nay, 2 admin đổi giờ. */
  ghiDe: number
}

export type GiaiDoan = 'xa' | 'sap' | 'dang' | 'ket'
export type KieuTin = 'sap' | 'dang' | 'ket'

/** Đăng tin "Sắp diễn ra" trước bao nhiêu ngày (lễ hội); tuần Pokémon chỉ báo trước 1 ngày. */
export const BAO_TRUOC_NGAY = 3
export const BAO_TRUOC_NGAY_TUAN = 1
/** Tin "đã kết thúc" chỉ đăng trong bấy nhiêu ngày sau khi đóng (máy chủ web tắt lâu thì bỏ qua, không đăng tin cũ). */
export const HAN_TIN_KET_NGAY = 2
/** Trễ quá bấy nhiêu giờ thì tin lấy giờ đăng = lúc đăng thật (không giả vờ đăng đúng giờ cũ). */
export const TRE_TOI_DA_GIO = 24

const NGAY = 86_400_000
const LECH_VN = 7 * 3_600_000

/**
 * Sự kiện phụ đi kèm sự kiện chính (cùng dịp): không đăng tin riêng, chỉ nhắc trong tin của sự kiện chính và gộp vào
 * thẻ trên trang chủ.
 */
export const SU_KIEN_PHU: Record<string, string> = {
  'tet123': 'tet2017',
  'demhalloween': 'halloween',
  'demnoel': 'noel',
  'blackfridaydaugia': 'blackfriday',
  'blackfridayban': 'blackfriday',
}

export function laTuanPokemon(l: Pick<SuKienLan, 'nhom'>): boolean {
  return l.nhom === 'Tuần Pokémon'
}

/** Cúp PvP tuần (0.9.7, mã moi:cuptuan): mỗi tuần một dòng, tên = loại cúp của tuần (com.htbb.pokemon.CupLogic). */
export const NHOM_CUP = 'Cúp PvP'
export function laCup(l: Pick<SuKienLan, 'nhom'>): boolean {
  return l.nhom === NHOM_CUP
}

/** Ngày cộng đồng hằng tháng (0.9.7, mã moi:congdong): một Chủ nhật 04:00 → 04:00, báo trước 3 ngày như lễ hội. */
export const NHOM_NGAY_CONG_DONG = 'Ngày cộng đồng'

export function laNgayCongDong(l: Pick<SuKienLan, 'nhom'>): boolean {
  return l.nhom === NHOM_NGAY_CONG_DONG
}

/** 'YYYY-MM-DD HH:mm:ss' (giờ VN) -> ms; NaN nếu sai. */
export function msTuChuoi(s: string | null | undefined): number {
  const m = /^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})(?::(\d{2}))?/.exec(String(s ?? ''))
  if (!m) return Number.NaN
  return Date.UTC(+m[1]!, +m[2]! - 1, +m[3]!, +m[4]!, +m[5]!, +(m[6] ?? 0)) - LECH_VN
}

/** ms -> 'YYYY-MM-DD HH:mm:ss' giờ VN. */
export function chuoiTuMs(ms: number): string {
  const d = new Date(ms + LECH_VN)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getUTCFullYear()}-${p(d.getUTCMonth() + 1)}-${p(d.getUTCDate())} ${p(d.getUTCHours())}:${p(d.getUTCMinutes())}:${p(d.getUTCSeconds())}`
}

const THU = ['Chủ nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy']

/** "04:00 Thứ Bảy 18/10/2026" (giờ VN). */
export function gioDep(ms: number): string {
  const d = new Date(ms + LECH_VN)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${p(d.getUTCHours())}:${p(d.getUTCMinutes())} ${THU[d.getUTCDay()]} ${p(d.getUTCDate())}/${p(d.getUTCMonth() + 1)}/${d.getUTCFullYear()}`
}

/** "18/10" (giờ VN). */
export function ngayNgan(ms: number): string {
  const d = new Date(ms + LECH_VN)
  return `${String(d.getUTCDate()).padStart(2, '0')}/${String(d.getUTCMonth() + 1).padStart(2, '0')}`
}

/** "từ 04:00 Thứ Bảy 18/10/2026 đến 04:00 Thứ Ba 03/11/2026" */
export function khoangThoiGian(l: Pick<SuKienLan, 'mo' | 'dong'>): string {
  return `từ ${gioDep(msTuChuoi(l.mo))} đến ${gioDep(msTuChuoi(l.dong))}`
}

/** Số ngày làm tròn (cửa sổ luôn tròn ngày ở 04:00). */
export function soNgaySuKien(l: Pick<SuKienLan, 'mo' | 'dong'>): number {
  return Math.max(1, Math.round((msTuChuoi(l.dong) - msTuChuoi(l.mo)) / NGAY))
}

export function baoTruocMs(l: Pick<SuKienLan, 'nhom'>): number {
  return (laTuanPokemon(l) || laCup(l) ? BAO_TRUOC_NGAY_TUAN : BAO_TRUOC_NGAY) * NGAY
}

/** Giai đoạn của một lần diễn ra lúc `bay` (ms). Lần bị admin tắt năm nay (ghiDe 1) luôn 'xa'. */
export function giaiDoan(l: SuKienLan, bay: number): GiaiDoan {
  if (Number(l.ghiDe) === 1) return 'xa'
  const mo = msTuChuoi(l.mo)
  const dong = msTuChuoi(l.dong)
  if (!(mo < dong)) return 'xa'
  if (bay >= dong) return bay < dong + HAN_TIN_KET_NGAY * NGAY ? 'ket' : 'xa'
  if (bay >= mo) return 'dang'
  if (bay >= mo - baoTruocMs(l)) return 'sap'
  return 'xa'
}

/**
 * Tin cần đăng lúc `bay` cho lần `l`: đúng MỘT tin của giai đoạn hiện tại (không đăng bù tin "sắp" khi sự kiện đã mở —
 * máy chủ web tắt qua mốc thì người chơi chỉ cần biết trạng thái mới nhất). Sự kiện phụ không có tin riêng; tuần
 * Pokémon không có tin "đã kết thúc".
 */
export function tinCanDang(l: SuKienLan, bay: number): { kieu: KieuTin, han: number } | null {
  if (SU_KIEN_PHU[l.ma]) return null
  const g = giaiDoan(l, bay)
  if (g === 'xa') return null
  if (g === 'ket' && laTuanPokemon(l)) return null
  const mo = msTuChuoi(l.mo)
  const dong = msTuChuoi(l.dong)
  const han = g === 'sap' ? mo - baoTruocMs(l) : g === 'dang' ? mo : dong
  return { kieu: g, han }
}

/** Giờ ghi vào dang_luc của tin: đúng mốc nếu trễ ít, ngược lại là lúc đăng thật. */
export function gioDangTin(han: number, bay: number): number {
  return bay - han > TRE_TOI_DA_GIO * 3_600_000 ? bay : han
}

/** slug duy nhất cho một tin: su-kien-<ma>-<nam>[-<lan>]-<kieu>, chỉ chữ thường / số / gạch ngang. */
export function slugTin(l: Pick<SuKienLan, 'ma' | 'nam' | 'lan'>, kieu: KieuTin): string {
  const ma = l.ma.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '')
  return `su-kien-${ma}-${l.nam}${Number(l.lan) > 0 ? `-${l.lan}` : ''}-${kieu}`
}

export interface TinSuKien {
  slug: string
  tieuDe: string
  tomTat: string
  noiDung: string
  loai: string
  the: string
}

const NHAN_KIEU: Record<KieuTin, string> = { sap: 'Sắp diễn ra', dang: 'Đang diễn ra', ket: 'Đã kết thúc' }

/** Viết tin (tiêu đề, tóm tắt, nội dung Markdown) từ mô tả, phần thưởng và cửa sổ của sự kiện. */
export function vietTin(l: SuKienLan, kieu: KieuTin, kem: SuKienLan[] = []): TinSuKien {
  const mo = msTuChuoi(l.mo)
  const dong = msTuChuoi(l.dong)
  const n = soNgaySuKien(l)
  const tieuDe = `${NHAN_KIEU[kieu]}: ${l.ten}`
  const khoang = khoangThoiGian(l)
  let tomTat: string
  const doan: string[] = []
  if (kieu === 'sap') {
    tomTat = `${l.ten} bắt đầu lúc ${gioDep(mo)} và kéo dài ${n} ngày. ${l.moTa}`
    doan.push(`**${l.ten}** sắp đến! Sự kiện mở **${khoang}** (${n} ngày, giờ Việt Nam).`)
  }
  else if (kieu === 'dang') {
    tomTat = `${l.ten} đang diễn ra tới ${gioDep(dong)}. ${l.moTa}`
    doan.push(`**${l.ten}** đã mở! Sự kiện diễn ra **${khoang}** (${n} ngày, giờ Việt Nam). Đăng nhập ngay để không bỏ lỡ.`)
  }
  else {
    tomTat = `${l.ten} đã khép lại lúc ${gioDep(dong)}. Cảm ơn các huấn luyện viên đã tham gia!`
    doan.push(`**${l.ten}** đã kết thúc lúc ${gioDep(dong)}. Cảm ơn mọi huấn luyện viên đã tham gia!`)
  }
  if (laCup(l)) {
    doan.push(kieu === 'ket'
      ? 'Xem bảng điểm và nhà vô địch ở trang [Cúp PvP tuần](/cup). Phần thưởng đã tự gửi vào game (ai offline nhận khi đăng nhập).'
      : 'Đăng ký trong game: Sổ tay huấn luyện → Cúp PvP tuần. Bảng điểm trực tiếp: [Cúp PvP tuần](/cup).')
  }
  if (kieu !== 'ket') {
    doan.push(`## Có gì trong sự kiện\n\n${l.moTa}`)
    if (l.phanThuong) doan.push(`## Phần thưởng nổi bật\n\n${l.phanThuong}`)
    const phu = kem.filter(k => k.ma !== l.ma && Number(k.ghiDe) !== 1)
    if (phu.length) {
      doan.push(`## Đi kèm\n\n${phu.map(k => `- **${k.ten}**: ${khoangThoiGian(k)}. ${k.moTa}`).join('\n')}`)
    }
    doan.push(`## Thời gian\n\n- Mở: ${gioDep(mo)}\n- Đóng: ${gioDep(dong)}${l.ngayLe ? `\n- Dịp: ${l.ngayLe}` : ''}\n\n`
      + 'Sự kiện bật / tắt tự động theo lịch, áp dụng ở lần bảo trì hằng ngày (khoảng 04:30–05:30).')
  }
  else {
    doan.push('Phần thưởng sự kiện chưa dùng vẫn ở trong hành trang; vật phẩm có hạn sẽ hết hạn theo mô tả của từng món. '
      + 'Xem lịch các sự kiện sắp tới ở [Cẩm nang › Sự kiện](/cam-nang/su-kien).')
  }
  return {
    slug: slugTin(l, kieu),
    tieuDe,
    tomTat: tomTat.slice(0, 500),
    noiDung: doan.join('\n\n'),
    loai: 'Sự kiện',
    the: `sự kiện,${laCup(l) ? 'cúp PvP' : laTuanPokemon(l) ? 'tuần Pokémon' : laNgayCongDong(l) ? 'ngày cộng đồng' : 'lễ hội'},${NHAN_KIEU[kieu].toLowerCase()}`,
  }
}

/** Sự kiện phụ đi kèm lần `l` (cùng năm, cửa sổ giao nhau). */
export function suKienKem(l: SuKienLan, ds: SuKienLan[]): SuKienLan[] {
  const mo = msTuChuoi(l.mo)
  const dong = msTuChuoi(l.dong)
  return ds.filter(k => SU_KIEN_PHU[k.ma] === l.ma && msTuChuoi(k.mo) < dong && msTuChuoi(k.dong) > mo)
}

/** Trang chủ: sự kiện đang diễn ra và sắp tới trong `soNgaySapToi` ngày (bỏ sự kiện phụ và lần bị tắt). */
export function dangVaSapToi(ds: SuKienLan[], bay: number, soNgaySapToi = 30): { dang: SuKienLan[], sap: SuKienLan[] } {
  const chinh = ds.filter(l => !SU_KIEN_PHU[l.ma] && Number(l.ghiDe) !== 1)
  const dang = chinh.filter(l => msTuChuoi(l.mo) <= bay && bay < msTuChuoi(l.dong))
    .sort((a, b) => msTuChuoi(a.dong) - msTuChuoi(b.dong))
  const sap = chinh.filter(l => msTuChuoi(l.mo) > bay && msTuChuoi(l.mo) <= bay + soNgaySapToi * NGAY)
    .sort((a, b) => msTuChuoi(a.mo) - msTuChuoi(b.mo))
  return { dang, sap }
}

/** Đếm ngược "3 ngày 4 giờ", "5 giờ 12 phút", "8 phút". */
export function demNguoc(ms: number): string {
  if (!(ms > 0)) return 'ngay bây giờ'
  const phut = Math.floor(ms / 60_000)
  const ngay = Math.floor(phut / 1440)
  const gio = Math.floor((phut % 1440) / 60)
  const p = phut % 60
  if (ngay > 0) return `${ngay} ngày${gio ? ` ${gio} giờ` : ''}`
  if (gio > 0) return `${gio} giờ${p ? ` ${p} phút` : ''}`
  return `${Math.max(1, p)} phút`
}

/** Chuẩn hoá một dòng CSDL / JSON thành SuKienLan. */
export function suKienLanTuDong(r: Record<string, unknown>): SuKienLan {
  const s = (v: unknown) => (v == null ? '' : String(v))
  const gio = (v: unknown) => (v instanceof Date ? chuoiTuMs(v.getTime()) : s(v).replace('T', ' ').slice(0, 19))
  return {
    ma: s(r.ma),
    nam: Number(r.nam) || 0,
    lan: Number(r.lan) || 0,
    ten: s(r.ten),
    nhom: s(r.nhom),
    ngayLe: s(r.ngay_le ?? r.ngayLe),
    moTa: s(r.mo_ta ?? r.moTa),
    phanThuong: s(r.phan_thuong ?? r.phanThuong),
    mo: gio(r.mo_luc ?? r.mo),
    dong: gio(r.dong_luc ?? r.dong),
    ghiDe: Number(r.ghi_de ?? r.ghiDe) || 0,
  }
}
