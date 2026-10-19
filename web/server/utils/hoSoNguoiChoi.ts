/**
 * TRUNG TÂM NGƯỜI CHƠI — phần THUẦN (không import gì, không đụng CSDL) để `node --test` chạy được
 * (tests/nguoi-choi.test.ts). Mọi export mang tiền tố `hs` vì Nitro tự import mọi export trong server/utils.
 *
 * Định dạng đọc ở đây đều lấy từ CHÍNH mã Java của server game (repo HTBB, chỉ đọc):
 *   - pInfo (tob_char.pInfo): real/CharNapDuLieu.initPinfo — [8] mapID, [9] x, [10] y, [39] myCountry,
 *     [92] dayCamChat (MapChat.java, lệnh GM "camchat").
 *   - Điểm hồi sinh an toàn: real/CharNapDuLieu.initInfoFromDB (dòng ~418-446): khi nạp nhân vật,
 *     mapID == 0 (hoặc map không tồn tại / map sự kiện) bị đổi sang thị trấn khởi đầu của vùng
 *     mapstart[myCountry] = {70,1701} | {0,301} | {80,1901} với toạ độ ngẫu nhiên trong các ô hồi sinh
 *     pos[][] của chính game; myCountry = -1 thì về 117/2641 (624,512). Vì vậy "đưa về điểm an toàn"
 *     = ghi pInfo[8] = 0 (x,y = 0): game tự đặt toạ độ hợp lệ ở lần vào kế tiếp — web không tự bịa toạ độ.
 *   - Hồ sơ lâu dài (tob_char_lau_dai.du_lieu): com/htbb/pokemon/HoSoLauDai.maHoa/giaiMa — "k=v;k=v".
 *   - Hồ sơ hệ sinh thái (tob_hst_nhan_vat.du_lieu): com/htbb/pokemon/HoSoSinhThai.chuoi/doc —
 *     "v1|ng=..|tl=..|kn=a,b,c,d,e|tui=k:v,..|tr=..|nt=..|gi=..|nc=..|al=a~b|vx=..|dem=..|bf=..|tt=..|mg=..|dh=0~3".
 *   - Nhiệm vụ cũ (tob_char_quest.isfinish): real/Char.initQuest — các nhiệm vụ ngăn bằng '|',
 *     mỗi cái "idQuest,trạngThái,mainSub" (mainSub 0 = nhiệm vụ chính).
 */

// ------------------------------------------------------------------ pInfo: vị trí

export const HS_PINFO_MAP = 8
export const HS_PINFO_X = 9
export const HS_PINFO_Y = 10
export const HS_PINFO_QUOC_GIA = 39

/** Bản đồ ghi vào khi "đưa về điểm an toàn" — xem ghi chú đầu file (CharNapDuLieu.initInfoFromDB). */
export const HS_MAP_AN_TOAN = 0

/** Thị trấn khởi đầu theo vùng (myCountry) — chép từ mapstart / mapstartDun của CharNapDuLieu. */
export const HS_THI_TRAN_KHOI_DAU: Record<string, number[]> = { '0': [70, 1701], '1': [0, 301], '2': [80, 1901], '-1': [117, 2641] }

export interface HsViTri { map: number, x: number, y: number, quocGia: number }

function soNguyen(s: string | undefined): number | null {
  const v = (s ?? '').trim()
  return /^-?\d+$/.test(v) ? Number(v) : null
}

/** Đọc vị trí đã lưu trong pInfo; null nếu pInfo hỏng. */
export function hsDocViTri(pInfo: string | null | undefined): HsViTri | null {
  const f = String(pInfo ?? '').split(',')
  if (f.length <= HS_PINFO_Y) return null
  const map = soNguyen(f[HS_PINFO_MAP]), x = soNguyen(f[HS_PINFO_X]), y = soNguyen(f[HS_PINFO_Y])
  if (map === null || x === null || y === null) return null
  return { map, x, y, quocGia: soNguyen(f[HS_PINFO_QUOC_GIA]) ?? -1 }
}

/** pInfo mới với vị trí = điểm an toàn; null nếu pInfo không đủ trường / hỏng (không sửa gì). */
export function hsDatViTriAnToan(pInfo: string | null | undefined): string | null {
  if (!hsDocViTri(pInfo)) return null
  const f = String(pInfo).split(',')
  f[HS_PINFO_MAP] = String(HS_MAP_AN_TOAN)
  f[HS_PINFO_X] = '0'
  f[HS_PINFO_Y] = '0'
  return f.join(',')
}

// ------------------------------------------------------------------ hồ sơ lâu dài

export interface HsHoSoLauDai {
  nangLuong: { ngay: number, msDaDung: number, phut100: number }
  tinhThong: { diem: number, loai: number }
  dangNhap7Ngay: { buoc: number, ngayCuoi: number }
  veChuong: number
  shiny: { dem: number, so: number }
  giftcode: { thang: number, dem: number }
  hamNguc: { ngay: number, tangCao: number, daThuong: number }
  pvp: { mua: number, elo: number, thang: number, thua: number }
  ngayDauTien: number
  ngayGanNhat: number
  quayLaiDenNgay: number
  suPham: { thayId: number, mo: number }
  nhiemVu: { daChuyenChuoiMoi: boolean, viTriCu: number }
  keoHiem: { tuan: number, so: number }
  doGiu: number
  gym: { huyHieuBit: number, soHuyHieu: number, tuan: number }
  soMonDangNangCap: number
}

/** Bản TS của HoSoLauDai.giaiMa (mặc định giống hệt: np=180, pe=1000, vc thiếu = 0). */
export function hsGiaiMaLauDai(s: string | null | undefined): HsHoSoLauDai {
  const m = new Map<string, number>()
  let soTruot = 0
  for (const cap of String(s ?? '').split(';')) {
    const i = cap.indexOf('=')
    if (i <= 0) continue
    const k = cap.slice(0, i)
    const v = Number(cap.slice(i + 1).trim())
    if (!Number.isFinite(v) || !/^-?\d+$/.test(cap.slice(i + 1).trim())) continue
    if (/^t\d+$/.test(k)) { if (v > 0) soTruot++; continue }
    m.set(k, v)
  }
  const g = (k: string, macDinh = 0) => m.get(k) ?? macDinh
  const gh = g('gh')
  let soHuyHieu = 0
  for (let b = gh >>> 0; b; b >>>= 1) soHuyHieu += b & 1
  return {
    nangLuong: { ngay: g('nn'), msDaDung: g('nm'), phut100: g('np', 180) },
    tinhThong: { diem: g('td'), loai: g('tl') },
    dangNhap7Ngay: { buoc: g('db'), ngayCuoi: g('dn') },
    veChuong: g('vc'),
    shiny: { dem: g('sd'), so: g('ss') },
    giftcode: { thang: g('gt'), dem: g('gd') },
    hamNguc: { ngay: g('hn'), tangCao: g('ht'), daThuong: g('hd') },
    pvp: { mua: g('pm'), elo: g('pe', 1000), thang: g('pw'), thua: g('pl') },
    ngayDauTien: g('d1'),
    ngayGanNhat: g('d2'),
    quayLaiDenNgay: g('ql'),
    suPham: { thayId: g('th'), mo: g('sp') },
    nhiemVu: { daChuyenChuoiMoi: g('nc') === 1, viTriCu: g('nq') },
    keoHiem: { tuan: g('kt'), so: g('ks') },
    doGiu: g('dg'),
    gym: { huyHieuBit: gh, soHuyHieu, tuan: g('gw') },
    soMonDangNangCap: soTruot,
  }
}

/** Bậc PvP theo Elo — ThangPvp.bac / TEN_BAC. */
export function hsBacPvp(elo: number): string {
  const ten = ['Poké Ball', 'Great Ball', 'Ultra Ball', 'Master Ball']
  const nguong = [1200, 1400, 1600]
  let b = 0
  for (let i = 0; i < nguong.length; i++) if (elo >= nguong[i]!) b = i + 1
  return ten[b]!
}

// ------------------------------------------------------------------ hồ sơ hệ sinh thái

export const HS_KY_NANG_SONG = ['Câu cá', 'Trồng trọt', 'Khảo cổ', 'Nấu ăn', 'Chế thuốc'] as const
export const HS_DANH_HIEU_SINH_THAI = [
  'Nhiếp ảnh tập sự', 'Thợ săn khoảnh khắc', 'Nhà sinh thái học', 'Huyền thoại ống kính',
  'Ngư phủ bậc thầy', 'Nhà khảo cổ', 'Đầu bếp Poké', 'Dược sư', 'Nhà vườn xanh',
  'Nhà nhân giống', 'Thợ săn shiny', 'Vua tốc độ', 'Thánh đố Pokémon', 'Nhà vô địch giải tuần',
  'Nhà thám hiểm vùng', 'Bạn của muôn loài',
] as const
const CAP_KY_NANG_TOI_DA = 50

/** KyNangSong.expLenCap: 40 × 1,105^cap (cấp 1..49). */
export function hsExpLenCapKyNang(cap: number): number {
  if (cap < 1 || cap >= CAP_KY_NANG_TOI_DA) return 0
  return Math.round(40 * 1.105 ** cap)
}

/** KyNangSong.capTuExp. */
export function hsCapKyNang(exp: number): number {
  let cap = 1
  let con = Math.max(0, exp)
  while (cap < CAP_KY_NANG_TOI_DA && con >= hsExpLenCapKyNang(cap)) {
    con -= hsExpLenCapKyNang(cap)
    cap++
  }
  return cap
}

export interface HsHoSoSinhThai {
  hopLe: boolean
  ngay: number
  theLuc: number
  kyNang: { ten: string, exp: number, cap: number }[]
  soLoaiVatLieu: number
  tongVatLieu: number
  soTrung: number
  nhaTre: { pet1: number, pet2: number, buoc: number }
  soDongGiong: number
  soLoaiNghienCuu: number
  soAnhAlbum: number
  soVungHoanThanh: number
  thanThien: number
  danhHieu: string[]
}

function soDs(s: string | undefined, n: number): number[] {
  const a = String(s ?? '').split(',')
  const r: number[] = []
  for (let i = 0; i < n; i++) {
    const v = Number((a[i] ?? '').trim())
    r.push(Number.isFinite(v) ? Math.trunc(v) : 0)
  }
  return r
}

function banDo(s: string | undefined): Map<number, number> {
  const m = new Map<number, number>()
  for (const p of String(s ?? '').split(',')) {
    const i = p.indexOf(':')
    if (i <= 0) continue
    const k = Number(p.slice(0, i)), v = Number(p.slice(i + 1))
    if (Number.isInteger(k) && Number.isInteger(v) && v > 0) m.set(k, v)
  }
  return m
}

/** Bản TS (chỉ đọc) của HoSoSinhThai.doc. Chuỗi rỗng/sai phiên bản -> hồ sơ mới (hopLe=false). */
export function hsGiaiMaSinhThai(s: string | null | undefined): HsHoSoSinhThai {
  const chuoi = String(s ?? '')
  const hopLe = chuoi.startsWith('v1')
  const m = new Map<string, string>()
  if (hopLe) {
    for (const p of chuoi.split('|')) {
      const i = p.indexOf('=')
      if (i > 0) m.set(p.slice(0, i), p.slice(i + 1))
    }
  }
  const kn = soDs(m.get('kn'), HS_KY_NANG_SONG.length)
  const tui = banDo(m.get('tui'))
  const nt = soDs(m.get('nt'), 6)
  const mg = soDs(m.get('mg'), 4)
  const vx = soDs(m.get('vx'), 1)[0]!
  let soVung = 0
  for (let b = vx >>> 0; b; b >>>= 1) soVung += b & 1
  const dem = (k: string, tach: string) => (m.get(k) ? m.get(k)!.split(tach).filter(x => x !== '').length : 0)
  const danhHieu: string[] = []
  for (const d of String(m.get('dh') ?? '').split('~')) {
    const i = /^\d+$/.test(d) ? Number(d) : -1
    const ten = HS_DANH_HIEU_SINH_THAI[i]
    if (ten && !danhHieu.includes(ten)) danhHieu.push(ten)
  }
  return {
    hopLe,
    ngay: soDs(m.get('ng'), 1)[0]!,
    theLuc: m.has('tl') ? soDs(m.get('tl'), 1)[0]! : 200,
    kyNang: HS_KY_NANG_SONG.map((ten, i) => ({ ten, exp: kn[i]!, cap: hsCapKyNang(kn[i]!) })),
    soLoaiVatLieu: tui.size,
    tongVatLieu: [...tui.values()].reduce((a, b) => a + b, 0),
    soTrung: Math.min(6, dem('tr', ',')),
    nhaTre: { pet1: nt[0]!, pet2: nt[1]!, buoc: nt[2]! },
    soDongGiong: dem('gi', ','),
    soLoaiNghienCuu: banDo(m.get('nc')).size,
    soAnhAlbum: Math.min(200, dem('al', '~')),
    soVungHoanThanh: soVung,
    thanThien: mg[3]!,
    danhHieu,
  }
}

// ------------------------------------------------------------------ nhiệm vụ cũ (tob_char_quest)

export interface HsNhiemVuCu { id: number, trangThai: string, chinh: boolean }

/** Tách isfinish: "id,trạngThái,mainSub|id,..." (Char.initQuest). Bỏ mẩu hỏng và id 0. */
export function hsTachNhiemVuCu(isfinish: string | null | undefined): HsNhiemVuCu[] {
  const ra: HsNhiemVuCu[] = []
  for (const p of String(isfinish ?? '').split('|')) {
    const t = p.split(',').map(x => x.trim())
    if (t.length < 3 || !/^\d+$/.test(t[0]!) || !/^-?\d+$/.test(t[2]!)) continue
    const id = Number(t[0])
    if (id <= 0) continue
    ra.push({ id, trangThai: t[1]!, chinh: Number(t[2]) === 0 })
  }
  return ra
}

// ------------------------------------------------------------------ cấp / EXP

/**
 * % vào cấp hiện tại theo bảng expMain (chỉ số = cấp - 1, xem server/utils/capDo.ts).
 * null khi không tính được (cấp ngoài bảng / exp hỏng).
 */
export function hsPhanTramCap(xp: number | string | null | undefined, cap: number, expMain: readonly number[]): number | null {
  const x = Number(xp)
  const c = Math.trunc(Number(cap))
  if (!Number.isFinite(x) || c < 1 || c >= expMain.length) return null
  const dau = expMain[c - 1]!, sau = expMain[c]!
  if (!(sau > dau)) return null
  const p = ((x - dau) / (sau - dau)) * 100
  return Math.max(0, Math.min(99.99, Math.round(p * 100) / 100))
}

// ------------------------------------------------------------------ trạng thái online

export interface HsTinHieuOnline {
  /** Giây kể từ dòng nhịp tim mới nhất của tob_chi_so_may_chu (server ghi mỗi 30 s); null = không có. */
  giayNhipTim: number | null
  /** Số nhân vật online theo dòng nhịp tim đó. */
  soOnlineNhipTim: number | null
  /** Giây kể từ lần ghi mới nhất của tob_vi_tri_online (mỗi 30 s, bảng rỗng khi không ai online); null = rỗng/không có. */
  giayViTri: number | null
  /** Nhân vật có trong tob_vi_tri_online. */
  coTrongViTri: boolean
}

export const HS_NGUONG_TUOI_GIAY = 120
/** Nhịp tim im lâu hơn thế coi như máy chủ game đã tắt (mọi nhân vật đang offline). */
export const HS_NGUONG_TAT_GIAY = 600

/**
 * Nhân vật có đang online không: true / false / null (không chắc -> thao tác chỉ-offline phải từ chối).
 *   - vị trí online còn tươi: có tên = online, không có = offline;
 *   - nhịp tim máy chủ còn tươi và báo 0 người online = offline (bảng vị trí bị xoá trắng khi không ai online);
 *   - nhịp tim im quá HS_NGUONG_TAT_GIAY và vị trí cũng không tươi = máy chủ đã tắt = offline;
 *   - còn lại: không chắc.
 */
export function hsQuyetDinhOnline(t: HsTinHieuOnline): { online: boolean | null, lyDo: string } {
  const viTriTuoi = t.giayViTri !== null && t.giayViTri <= HS_NGUONG_TUOI_GIAY
  if (viTriTuoi) return t.coTrongViTri ? { online: true, lyDo: 'Có trong danh sách online (cập nhật mỗi 30 giây).' } : { online: false, lyDo: 'Không có trong danh sách online.' }
  const nhipTuoi = t.giayNhipTim !== null && t.giayNhipTim <= HS_NGUONG_TUOI_GIAY
  if (nhipTuoi && t.soOnlineNhipTim === 0) return { online: false, lyDo: 'Máy chủ đang chạy, không có ai online.' }
  if (t.giayNhipTim !== null && t.giayNhipTim > HS_NGUONG_TAT_GIAY && !viTriTuoi) return { online: false, lyDo: 'Máy chủ game không báo nhịp tim hơn 10 phút — coi như đang tắt.' }
  if (t.giayNhipTim === null && t.giayViTri === null) return { online: null, lyDo: 'Chưa có dữ liệu nhịp tim máy chủ.' }
  return { online: null, lyDo: 'Máy chủ có người online nhưng danh sách vị trí chưa cập nhật.' }
}

// ------------------------------------------------------------------ dữ liệu vào

export const HS_KIEU_TIM = ['tat_ca', 'tai_khoan', 'nhan_vat', 'email', 'ip', 'ma_may'] as const
export type HsKieuTim = typeof HS_KIEU_TIM[number]

/** Chuẩn hoá ô tìm: cắt khoảng trắng, tối đa 64 ký tự; kiểu lạ -> 'tat_ca'. */
export function hsChuanHoaTim(q: unknown, kieu: unknown): { q: string, kieu: HsKieuTim } {
  const s = (typeof q === 'string' ? q : '').trim().slice(0, 64)
  const k = (HS_KIEU_TIM as readonly string[]).includes(String(kieu)) ? kieu as HsKieuTim : 'tat_ca'
  return { q: s, kieu: k }
}

/**
 * Trông giống mã máy không — để "Tất cả" tự tìm thêm theo mã máy (client HTBB gửi "j2me-" + 24 ký tự;
 * máy chủ chỉ nhận chữ/số/-/_ tối đa 64 — data.CamTheoMay.lamSach). Tên tài khoản cũng khớp mẫu này:
 * tìm thêm chỉ thêm kết quả, không bớt.
 */
export function hsGiongMaMay(s: string): boolean {
  return /^[A-Za-z0-9_-]{8,64}$/.test(s)
}

/** Trông giống địa chỉ IP (hoặc đầu IP) không — để "Tất cả" tự tìm thêm trong lịch sử đăng nhập. */
export function hsGiongIp(s: string): boolean {
  return /^\d{1,3}(\.\d{0,3}){1,3}$/.test(s) || (/^[0-9a-f:]+$/i.test(s) && s.includes(':') && s.length >= 3)
}

/** Thoát ký tự đặc biệt của LIKE (dùng với ESCAPE '!'). */
export function hsLike(s: string, kieu: 'chua' | 'dau' = 'chua'): string {
  const e = s.replace(/[!%_]/g, c => `!${c}`)
  return kieu === 'dau' ? `${e}%` : `%${e}%`
}

/** Số giờ hợp lệ: số nguyên 0..max; sai -> null. */
export function hsSoGio(v: unknown, max: number): number | null {
  const n = typeof v === 'number' ? v : (typeof v === 'string' && /^\d+$/.test(v.trim()) ? Number(v.trim()) : Number.NaN)
  return Number.isInteger(n) && n >= 0 && n <= max ? n : null
}

/** Che bớt email: "hoahv0903@gmail.com" -> "ho•••••03@gmail.com". */
export function hsAnEmail(email: string | null | undefined): string {
  const e = String(email ?? '').trim()
  const at = e.lastIndexOf('@')
  if (at < 1) return e ? '•••' : ''
  const ten = e.slice(0, at)
  const giu = ten.length <= 3 ? 1 : 2
  const cuoi = ten.length > 5 ? ten.slice(-2) : ''
  return `${ten.slice(0, giu)}${'•'.repeat(Math.max(3, ten.length - giu - cuoi.length))}${cuoi}${e.slice(at)}`
}

// ------------------------------------------------------------------ quà riêng (giftcode cá nhân)

export const HS_TIEN_TO_QUA_RIENG = 'qr'
const KY_TU_MA = 'abcdefghijkmnpqrstuvwxyz23456789'

/** Mã quà riêng: "qr" + 14 ký tự ngẫu nhiên (≈ 2^70, không dò được) — hợp gcChuanHoaMa (3..32, a-z0-9). */
export function hsTaoMaQuaRieng(ngauNhien: Uint8Array): string {
  let s = HS_TIEN_TO_QUA_RIENG
  for (let i = 0; i < 14; i++) s += KY_TU_MA[(ngauNhien[i] ?? 0) % KY_TU_MA.length]
  return s
}

/** Che mã khi ghi nhật ký / hiện cho người không phải siêu quản trị: "qrab…" (đủ để đối chiếu, không dùng được). */
export function hsAnMa(ma: string): string {
  return ma.length <= 6 ? '••••' : `${ma.slice(0, 6)}••••••`
}

/** Nội dung thư báo quà (tob_inbox.info tối đa 800 ký tự). */
export function hsNoiDungThuQua(moTa: string[], ma: string, han: string, loiNhan: string): string {
  const dau = `Ban quản trị gửi tặng bạn: ${moTa.join(', ')}.`
  const cach = ` Nhập mã quà tặng "${ma}" trong game (dùng 1 lần, hạn tới ${han}). Không chia sẻ mã cho người khác.`
  const nhan = loiNhan ? ` Lời nhắn: ${loiNhan}` : ''
  let s = dau + cach + nhan
  if (s.length > 800) s = (dau.slice(0, 800 - cach.length - 1) + cach).slice(0, 800)
  return s
}
