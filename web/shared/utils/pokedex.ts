/**
 * Dữ liệu Pokédex của game (mọi loài có trong game: khởi đầu, quái, boss, thú cưỡi, pet, NPC).
 *
 * File JSON sinh bởi `scripts/sinh-du-lieu-pokemon.mjs` từ repo KPAH — KHÔNG sửa tay;
 * sửa dữ liệu bên game rồi chạy lại script. Import tĩnh nên SSR và client dùng chung,
 * không cần gọi API.
 */
import raw from '../data/pokedex.json'

export type VaiTroLoai = 'khoi-dau' | 'quai' | 'boss' | 'thu-cuoi' | 'pet' | 'npc'

export interface HeInfo { id: number, ten: string, en: string }
export interface NoiXuatHien { map: number, ten: string, soLuong: number }
export interface QuaiTemplate {
  id: number
  cap: number
  hp: number
  exp: number
  shiny: boolean
  boss: boolean
  noi: NoiXuatHien[]
  ghiChuNoi: string
}
export interface BuocTienHoa { dex: number, dieuKien: string }
export interface TienHoaInfo { kieu: 'cap' | 'eevee' | 'pet' | 'ho', buoc: BuocTienHoa[], nhanh: BuocTienHoa[] }
export interface ChieuInfo { so: number, ten: string, he: number }
export interface LoaiDex {
  dex: number
  ten: string
  he: number[]
  vaiTro: VaiTroLoai[]
  anh: string | null
  anhShiny: string | null
  /** Giai đoạn tiến hoá của dòng người chơi (1..3), 0 = không phải loài người chơi. */
  giaiDoan: number
  dong: { class: number, ten: string, dexDau: number } | null
  tienHoa: TienHoaInfo | null
  chieu: ChieuInfo[] | null
  quai: QuaiTemplate[]
  thuCuoi: { ten: string, shiny: boolean, nguon: string, id: number }[]
  pet: { idItem: number, ten: string, cap: number, trung: boolean, shiny: boolean }[]
  npc: { ten: string, suKien: boolean }[]
}
export interface MapQuai { id: number, ten: string, dex: number[], capThap: number, capCao: number }
export interface PokedexData {
  he: HeInfo[]
  /** khacChinhXac[congKich][phongThu] = 200 | 100 | 50 | 0 (chuẩn Gen 6+). */
  khacChinhXac: number[][]
  /** Hệ số chính xác -> hệ số dùng trong game (đã làm dịu), đơn vị phần trăm. */
  lamDiu: Record<string, number>
  stabPhanTram: number
  khoiDau: { class: number, dex: number, dong: string }[]
  capNhanhEevee: number
  phanTramGiaiDoan: { 2: number, 3: number, eevee: number }
  daTienHoa: { idItem: number, ten: string, dex: number }[]
  tiLeRoiDaPhanNghin: number
  lechCapRoiDa: number
  mocPokedex: { moc: number, thuong: { ten: string, soLuong: number }[] }[]
  vung: string[]
  huongDanGame: string[]
  map: MapQuai[]
  loai: LoaiDex[]
}

export const POKEDEX = raw as unknown as PokedexData

const THEO_DEX_DEX = new Map(POKEDEX.loai.map(l => [l.dex, l]))

export function loaiTheoDex(dex: number): LoaiDex | undefined {
  return THEO_DEX_DEX.get(dex)
}

/** Ảnh chân dung (40×40, vẽ pixelated); loài chỉ có bản shiny thì dùng bản shiny. */
export function anhLoaiDex(l: Pick<LoaiDex, 'anh' | 'anhShiny'>): string {
  return l.anh ?? l.anhShiny ?? ''
}

export const TEN_VAI_TRO: Record<VaiTroLoai, string> = {
  'khoi-dau': 'Khởi đầu',
  'quai': 'Quái',
  'boss': 'Boss',
  'thu-cuoi': 'Thú cưỡi',
  'pet': 'Pet',
  'npc': 'NPC',
}
export const DS_VAI_TRO = Object.keys(TEN_VAI_TRO) as VaiTroLoai[]

/** Màu hệ (nền, chữ) — cặp màu đủ tương phản cho chữ nhỏ. */
const MAU_HE: [string, string][] = [
  ['#a8a77a', '#111'], // Thường
  ['#ee8130', '#111'], // Lửa
  ['#6390f0', '#111'], // Nước
  ['#f7d02c', '#111'], // Điện
  ['#7ac74c', '#111'], // Cỏ
  ['#96d9d6', '#111'], // Băng
  ['#c22e28', '#fff'], // Giác đấu
  ['#a33ea1', '#fff'], // Độc
  ['#e2bf65', '#111'], // Đất
  ['#a98ff3', '#111'], // Bay
  ['#f95587', '#111'], // Siêu linh
  ['#a6b91a', '#111'], // Bọ
  ['#b6a136', '#111'], // Đá
  ['#735797', '#fff'], // Ma
  ['#6f35fc', '#fff'], // Rồng
  ['#705746', '#fff'], // Bóng tối
  ['#b7b7ce', '#111'], // Thép
  ['#d685ad', '#111'], // Tiên
]

export function mauHe(he: number): { backgroundColor: string, color: string } {
  const [bg, fg] = MAU_HE[he] ?? ['#888', '#111']
  return { backgroundColor: bg, color: fg }
}

export function tenHe(he: number): string {
  return POKEDEX.he[he]?.ten ?? ''
}

/** Tên hệ viết tắt cho bảng khắc chế 18×18 (đọc đầy đủ qua <abbr title>). */
const TEN_HE_NGAN = ['Thg', 'Lửa', 'Nước', 'Điện', 'Cỏ', 'Băng', 'Đấu', 'Độc', 'Đất', 'Bay', 'SLi', 'Bọ', 'Đá', 'Ma', 'Rồng', 'BTối', 'Thép', 'Tiên']
export function tenHeNgan(he: number): string {
  return TEN_HE_NGAN[he] ?? tenHe(he)
}

/** Hệ số CHÍNH XÁC (400/200/100/50/25/0) khi hệ `congKich` đánh vào loài hệ `he` (HePokemon.heSoChinhXac). */
export function heSoChinhXac(congKich: number, he: readonly number[]): number {
  const [h1, h2] = he
  let kq = POKEDEX.khacChinhXac[congKich]?.[h1 ?? -1] ?? 100
  if (h2 !== undefined && h2 !== h1) kq = Math.trunc(kq * (POKEDEX.khacChinhXac[congKich]?.[h2] ?? 100) / 100)
  return kq
}

/** Hệ số DÙNG TRONG GAME (đã làm dịu: 200/150/100/70/50/30) — HePokemon.heSoTrongGame. */
export function heSoTrongGame(congKich: number, he: readonly number[]): number {
  return POKEDEX.lamDiu[String(heSoChinhXac(congKich, he))] ?? 100
}

/** "×1,5" từ phần trăm. */
export function hienHeSo(phanTram: number): string {
  return `×${(phanTram / 100).toLocaleString('vi-VN', { maximumFractionDigits: 2 })}`
}

/** Hiệu quả của 18 hệ tấn công vào loài có hệ `he`, gom theo hệ số trong game (giảm dần). */
export function bangPhongThu(he: readonly number[]): { heSo: number, chinhXac: number, he: number[] }[] {
  const nhom = new Map<number, { chinhXac: number, he: number[] }>()
  for (let ck = 0; ck < POKEDEX.he.length; ck++) {
    const g = heSoTrongGame(ck, he)
    const n = nhom.get(g) ?? { chinhXac: heSoChinhXac(ck, he), he: [] }
    n.he.push(ck)
    nhom.set(g, n)
  }
  return [...nhom.entries()].sort((a, b) => b[0] - a[0]).map(([heSo, v]) => ({ heSo, ...v }))
}

/** Bỏ dấu tiếng Việt để tìm kiếm. */
export function boDau(s: string): string {
  return s.normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D').toLowerCase()
}
