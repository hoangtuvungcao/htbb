/**
 * Loài Pokémon của nhân vật người chơi (nhánh pokemon/chuyen-doi).
 *
 * Bản TS thuần của quy tắc trong game server `src/com/kpah/pokemon/LoaiPokemon.java`
 * (repo KPAH) — sửa bên đó thì sửa cả ở đây. Kế hoạch: KPAH/docs/pokemon/KE_HOACH_CHUYEN_DOI.md
 * mục 4.1.
 *
 * - `tob_char.class` (0..4) = dòng khởi đầu: 0 Charmander, 1 Bulbasaur, 2 Eevee, 3 Squirtle, 4 Pichu.
 * - `tob_char.dex` = loài hiện tại; 0 = tự suy ra từ class + cấp (cột thêm bởi
 *   sql/them_cot_pokemon.sql của repo KPAH).
 * - Eevee chọn nhánh ở cấp 25 (người chơi chọn) nên không suy ra được từ cấp: chưa có
 *   `dex` thì vẫn là Eevee.
 */

import { tenNghe } from './kpah'

export interface LoaiPokemonWeb {
  dex: number
  ten: string
  /** Hệ, tiếng Việt (1 hoặc 2 hệ). */
  he: string[]
  /** Cấp tiến hoá sang `sau`; 0 = dạng cuối (hoặc Eevee: nhánh do người chơi chọn). */
  capTienHoa: number
  sau: number
}

/** Dex khởi đầu theo class cũ: index = class 0..4 (LoaiPokemon.KHOI_DAU_THEO_CLASS). */
export const DEX_KHOI_DAU = [4, 1, 133, 7, 172] as const

/** Cấp Eevee được chọn nhánh tiến hoá. */
export const CAP_NHANH_EEVEE = 25

/** Các nhánh Eevee. */
export const NHANH_EEVEE = [134, 135, 136, 196, 197, 470, 471, 700] as const

const DS: LoaiPokemonWeb[] = [
  // class 0 (Kiếm khách cũ) -> Charmander
  { dex: 4, ten: 'Charmander', he: ['Lửa'], capTienHoa: 16, sau: 5 },
  { dex: 5, ten: 'Charmeleon', he: ['Lửa'], capTienHoa: 36, sau: 6 },
  { dex: 6, ten: 'Charizard', he: ['Lửa', 'Bay'], capTienHoa: 0, sau: 0 },
  // class 1 (Chiến binh cũ) -> Bulbasaur
  { dex: 1, ten: 'Bulbasaur', he: ['Cỏ', 'Độc'], capTienHoa: 16, sau: 2 },
  { dex: 2, ten: 'Ivysaur', he: ['Cỏ', 'Độc'], capTienHoa: 32, sau: 3 },
  { dex: 3, ten: 'Venusaur', he: ['Cỏ', 'Độc'], capTienHoa: 0, sau: 0 },
  // class 3 (Đấu sĩ cũ) -> Squirtle
  { dex: 7, ten: 'Squirtle', he: ['Nước'], capTienHoa: 16, sau: 8 },
  { dex: 8, ten: 'Wartortle', he: ['Nước'], capTienHoa: 36, sau: 9 },
  { dex: 9, ten: 'Blastoise', he: ['Nước'], capTienHoa: 0, sau: 0 },
  // class 4 (Cung thủ cũ) -> Pichu
  { dex: 172, ten: 'Pichu', he: ['Điện'], capTienHoa: 10, sau: 25 },
  { dex: 25, ten: 'Pikachu', he: ['Điện'], capTienHoa: 30, sau: 26 },
  { dex: 26, ten: 'Raichu', he: ['Điện'], capTienHoa: 0, sau: 0 },
  // class 2 (Pháp sư cũ) -> Eevee, nhánh do người chơi chọn ở cấp 25
  { dex: 133, ten: 'Eevee', he: ['Thường'], capTienHoa: 0, sau: 0 },
  { dex: 134, ten: 'Vaporeon', he: ['Nước'], capTienHoa: 0, sau: 0 },
  { dex: 135, ten: 'Jolteon', he: ['Điện'], capTienHoa: 0, sau: 0 },
  { dex: 136, ten: 'Flareon', he: ['Lửa'], capTienHoa: 0, sau: 0 },
  { dex: 196, ten: 'Espeon', he: ['Siêu linh'], capTienHoa: 0, sau: 0 },
  { dex: 197, ten: 'Umbreon', he: ['Bóng tối'], capTienHoa: 0, sau: 0 },
  { dex: 470, ten: 'Leafeon', he: ['Cỏ'], capTienHoa: 0, sau: 0 },
  { dex: 471, ten: 'Glaceon', he: ['Băng'], capTienHoa: 0, sau: 0 },
  { dex: 700, ten: 'Sylveon', he: ['Tiên'], capTienHoa: 0, sau: 0 },
]

const THEO_DEX = new Map(DS.map(l => [l.dex, l]))

/** Mọi loài người chơi có thể là (21 loài, có ảnh ở public/pokemon/<dex>.png). */
export const LOAI_NGUOI_CHOI: readonly LoaiPokemonWeb[] = DS

export function loaiPokemon(dex: number): LoaiPokemonWeb | undefined {
  return THEO_DEX.get(dex)
}

/** Tên loài theo số Pokédex; không có trong bảng thì "#<dex>". */
export function tenLoai(dex: number): string {
  return THEO_DEX.get(dex)?.ten ?? `#${dex}`
}

/** Loài ứng với cấp hiện tại, đi từ dạng đầu (LoaiPokemon.dangTheoCap). */
export function dangTheoCap(dexDau: number, capDo: number): number {
  let dex = dexDau
  for (let i = 0; i < 5; i++) {
    const l = THEO_DEX.get(dex)
    if (!l || l.capTienHoa <= 0 || capDo < l.capTienHoa) break
    dex = l.sau
  }
  return dex
}

/**
 * Số dex hiện tại của nhân vật: `dex` > 0 thì dùng luôn, không thì suy ra từ class + cấp.
 * 0 = không xác định (class ngoài 0..4).
 */
export function dexHienTai(
  classId: number | string | null | undefined,
  level: number | string | null | undefined,
  dex?: number | string | null,
): number {
  const d = Math.trunc(Number(dex ?? 0))
  if (Number.isFinite(d) && d > 0) return d
  const dau = DEX_KHOI_DAU[Math.trunc(Number(classId)) as 0 | 1 | 2 | 3 | 4]
  if (dau === undefined) return 0
  return dangTheoCap(dau, Math.trunc(Number(level) || 0))
}

/** Tên loài hiện tại của nhân vật (vd. class 0, cấp 20, dex 0 -> "Charmeleon"). */
export function speciesName(
  classId: number | string | null | undefined,
  level: number | string | null | undefined,
  dex?: number | string | null,
): string {
  const d = dexHienTai(classId, level, dex)
  return d > 0 ? tenLoai(d) : tenNghe(classId)
}

/** Đường dẫn chân dung 40×40 (PMDCollab SpriteCollab); '' khi không có ảnh. */
export function anhPokemon(dex: number): string {
  return THEO_DEX.has(dex) ? `/pokemon/${dex}.png` : ''
}

/** Chuỗi tiến hoá của một dòng khởi đầu, vd. [4, 5, 6]. Eevee chỉ trả [133]. */
export function chuoiTienHoa(dexDau: number): number[] {
  const ra = [dexDau]
  let l = THEO_DEX.get(dexDau)
  while (l && l.capTienHoa > 0 && ra.length < 5) {
    ra.push(l.sau)
    l = THEO_DEX.get(l.sau)
  }
  return ra
}
