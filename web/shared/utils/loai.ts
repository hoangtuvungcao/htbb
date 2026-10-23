/**
 * Pokédex đầy đủ 1.025 loài — phần THUẦN (không CSDL, không JSON) dùng chung cho API, trang và test.
 *
 * Dữ liệu nằm trong các bảng `data_loai*` do `sql/htbb.sql` (khối `them_loai_day_du.sql`) tạo (sinh bởi
 * `tools/pokemon/loai/sinh_loai.py` từ PokeAPI ghim commit). Mã hệ = `HePokemon` 0..17 của game.
 */

export const SO_LOAI_TOI_DA = 1025

export const TEN_HE_LOAI = ['Thường', 'Lửa', 'Nước', 'Điện', 'Cỏ', 'Băng', 'Giác đấu', 'Độc', 'Đất', 'Bay', 'Siêu linh', 'Bọ', 'Đá', 'Ma', 'Rồng', 'Bóng tối', 'Thép', 'Tiên'] as const

/** Bậc loài T1..T6 (he_thong_day_du.md 2.3): tên + màu (DoHiemSinh). */
export const BAC_LOAI: Record<number, { ma: string, ten: string, mau: string }> = {
  1: { ma: 'T1', ten: 'Phổ biến', mau: '#e5e7eb' },
  2: { ma: 'T2', ten: 'Không phổ biến', mau: '#4ade80' },
  3: { ma: 'T3', ten: 'Hiếm', mau: '#60a5fa' },
  4: { ma: 'T4', ten: 'Rất hiếm', mau: '#a855f7' },
  5: { ma: 'T5', ten: 'Cực hiếm', mau: '#fb923c' },
  6: { ma: 'T6', ten: 'Huyền thoại / Huyền ảo', mau: '#ef4444' },
}

export const TEN_NHOM_LOAI = ['Thường', 'Huyền thoại', 'Huyền ảo', 'Ultra Beast', 'Nghịch lý'] as const

/** Vùng sinh thái (SinhThaiTheGioi.V_*). */
export const TEN_VUNG_SINH_THAI = ['Thị trấn', 'Tuyến đường', 'Đồng cỏ', 'Rừng', 'Hồ', 'Bờ biển', 'Núi', 'Tuyết', 'Sa mạc', 'Hang', 'Trong nhà', 'Thành', 'Đấu trường', 'Tàu'] as const

export const TEN_THOI_TIET = ['Quang đãng', 'Nắng gắt', 'Mưa', 'Tuyết', 'Bão cát', 'Sương mù'] as const

export const TEN_NGAY_DEM = ['Mọi lúc', 'Ban ngày', 'Ban đêm'] as const

/** Tên vùng địa lý theo thế hệ (đợt mở). */
export const TEN_THE_HE: Record<number, string> = {
  1: 'Kanto', 2: 'Johto', 3: 'Hoenn', 4: 'Sinnoh', 5: 'Unova', 6: 'Kalos', 7: 'Alola', 8: 'Galar', 9: 'Paldea',
}

export const TEN_MOI_TRUONG: Record<string, string> = {
  hang: 'Hang động', rung: 'Rừng', dong_co: 'Đồng cỏ', nui: 'Núi', hiem: 'Hiếm gặp', go_ghe: 'Địa hình gồ ghề',
  bien: 'Biển', do_thi: 'Đô thị', ven_nuoc: 'Ven nước', theo_he: 'Theo hệ',
}

export const TEN_CO_CHE: Record<string, string> = {
  cap: 'Tự động khi đạt cấp',
  vat_pham: 'Dùng vật phẩm',
  trao_doi: 'Trao đổi (hoặc Dây Liên Kết)',
  than_thiet: 'Thân thiết',
  keo: 'Dùng Kẹo',
}

export const TEN_KIEU_CHIEU: Record<string, string> = { vat_ly: 'Vật lý', dac_biet: 'Đặc biệt', ho_tro: 'Hỗ trợ' }

export const TEN_VAI_CHIEU: Record<string, string> = {
  stab: 'Cùng hệ',
  phu_he: 'Phủ hệ',
  ho_tro: 'Hỗ trợ',
  dac_trung: 'Đặc trưng',
}

export const TEN_CHI_SO = [
  { ma: 'hp', ten: 'HP' },
  { ma: 'tanCong', ten: 'Tấn công' },
  { ma: 'phongThu', ten: 'Phòng thủ' },
  { ma: 'tanCongDb', ten: 'Tấn công ĐB' },
  { ma: 'phongThuDb', ten: 'Phòng thủ ĐB' },
  { ma: 'tocDo', ten: 'Tốc độ' },
] as const

export interface LoaiTomTat {
  dex: number
  ten: string
  he: number[]
  bac: number
  theHe: number
  bat: boolean
  nhom: number
}

export interface ChiSoNen { hp: number, tanCong: number, phongThu: number, tanCongDb: number, phongThuDb: number, tocDo: number }

export interface ThanhVienHo { dex: number, ten: string, he: number[], giaiDoan: number, bat: boolean, tienHoaTu: number }

export interface CanhTienHoa {
  tu: number
  sang: number
  kieu: string
  coChe: string
  cap: number
  tenVatPham: string
  ngayDem: number
  keo: number
  dieuKien: string
}

export interface ChieuLoai {
  o: number
  ma: string
  vai: string
  capHoc: number
  ten: string
  he: number
  kieu: string
  vaiTro: string
}

export interface VungLoai {
  khuVuc: string
  vung: number
  capMin: number
  capMax: number
  trongSo: number
  ngayDem: number
  thoiTiet: number
  bacGap: number
}

export interface ChiTietLoai {
  loai: LoaiTomTat & {
    cs: ChiSoNen
    tong: number
    gocBat: number
    batDuoc: boolean
    chiSuKien: boolean
    giaiDoan: number
    dexGoc: number
    nhomTrung: string[]
    moiTruong: string
    tiLeCai: number
    chieuCao: number
    canNang: number
    moTa: string
  }
  ho: { thanhVien: ThanhVienHo[], canh: CanhTienHoa[] }
  chieu: ChieuLoai[]
  vung: VungLoai[]
  /** Vùng hoang dã xoay vòng: các chu kỳ tuần loài xuất hiện (rỗng = không xoay vòng). */
  xoayVong: { chuKy: number, ten: string, capMin: number, capMax: number, thap: boolean }[]
  sprite: { trangThai: number, chanDung: boolean, trongJar: boolean, goiMayChu: boolean, ghiCong: string } | null
}

/** Số Pokédex từ tham số đường dẫn: chỉ số nguyên 1..1025 (chuỗi chữ số thuần). null = không hợp lệ. */
export function docSoDex(v: unknown): number | null {
  const s = String(Array.isArray(v) ? v[0] : (v ?? '')).trim()
  if (!/^\d{1,4}$/.test(s)) return null
  const n = Number(s)
  return n >= 1 && n <= SO_LOAI_TOI_DA ? n : null
}

/** Hệ của loài dạng mảng (he2 = -1 là đơn hệ). */
export function heCuaLoai(he1: number, he2: number): number[] {
  const h1 = Number(he1)
  const h2 = Number(he2)
  return h2 >= 0 && h2 !== h1 ? [h1, h2] : [h1]
}

export function tenHeLoai(he: number): string {
  return TEN_HE_LOAI[he] ?? ''
}

export function bacLoai(bac: number) {
  return BAC_LOAI[bac] ?? BAC_LOAI[1]!
}

export const soDex = (d: number) => `#${String(d).padStart(3, '0')}`

/**
 * Dựng chuỗi tiến hoá theo TẦNG từ loài gốc: tầng 0 = [gốc], tầng k = các loài tiến hoá từ tầng k-1.
 * Mỗi nút kèm cạnh đi vào (điều kiện). Nhánh (Eevee, Tyrogue, Gloom...) nằm cùng tầng.
 * Bỏ qua cạnh trỏ ra ngoài họ và chống vòng (dữ liệu hỏng không treo trang).
 */
export function dungChuoiTienHoa(thanhVien: readonly ThanhVienHo[], canh: readonly CanhTienHoa[], goc: number):
  { dex: number, loai: ThanhVienHo | undefined, vao: CanhTienHoa | null }[][] {
  const theoDex = new Map(thanhVien.map(t => [t.dex, t]))
  const tu = new Map<number, CanhTienHoa[]>()
  for (const c of canh) {
    if (!theoDex.has(c.sang)) continue
    const ds = tu.get(c.tu) ?? []
    ds.push(c)
    tu.set(c.tu, ds)
  }
  const tang: { dex: number, loai: ThanhVienHo | undefined, vao: CanhTienHoa | null }[][] = []
  const daCo = new Set<number>([goc])
  let hienTai = [{ dex: goc, loai: theoDex.get(goc), vao: null as CanhTienHoa | null }]
  while (hienTai.length && tang.length < 10) {
    tang.push(hienTai)
    const sau: typeof hienTai = []
    for (const n of hienTai) {
      for (const c of [...(tu.get(n.dex) ?? [])].sort((a, b) => a.sang - b.sang)) {
        if (daCo.has(c.sang)) continue
        daCo.add(c.sang)
        sau.push({ dex: c.sang, loai: theoDex.get(c.sang), vao: c })
      }
    }
    hienTai = sau
  }
  return tang
}

/** Một dòng mô tả điều kiện tiến hoá cho người chơi. */
export function moTaTienHoa(c: CanhTienHoa): string {
  const phan: string[] = []
  if (c.dieuKien) phan.push(c.dieuKien)
  else if (c.coChe === 'cap') phan.push(`Cấp ${c.cap}`)
  if (c.coChe !== 'cap') phan.push(`trong game: từ cấp ${c.cap}${c.keo ? `, ${c.keo} Kẹo` : ''}`)
  return phan.join(' · ')
}

/** Tổng chỉ số nền. */
export function tongChiSo(cs: ChiSoNen): number {
  return cs.hp + cs.tanCong + cs.phongThu + cs.tanCongDb + cs.phongThuDb + cs.tocDo
}

/** Độ rộng thanh chỉ số (%), thang 0..255 như game chính bản. */
export function phanTramChiSo(v: number): number {
  return Math.max(2, Math.min(100, Math.round((v / 255) * 100)))
}

export interface BoLocLoai { tim: string, he: number, theHe: number, bac: number, trangThai: 'tat-ca' | 'da-mo' | 'chua-mo' }

/** Lọc danh sách loài (tìm theo tên đã bỏ dấu hoặc số dex). `boDau` truyền vào để giữ hàm thuần. */
export function locLoai<T extends LoaiTomTat>(ds: readonly T[], loc: BoLocLoai, boDau: (s: string) => string = s => s.toLowerCase()): T[] {
  const tho = boDau(loc.tim.trim()).replace(/^#/, '')
  const t = tho.replace(/^0+(?=\d)/, '')
  const so = /^\d+$/.test(t)
  // số có số 0 đầu (#025) = đúng số đó; số trần (25) = các số bắt đầu bằng 25
  const khopSo = (dex: number) => so && (tho !== t ? String(dex) === t : String(dex).startsWith(t))
  return ds.filter(l =>
    (!t || khopSo(l.dex) || (!so && boDau(l.ten).includes(t)))
    && (loc.he < 0 || l.he.includes(loc.he))
    && (loc.theHe <= 0 || l.theHe === loc.theHe)
    && (loc.bac <= 0 || l.bac === loc.bac)
    && (loc.trangThai === 'tat-ca' || (loc.trangThai === 'da-mo') === l.bat))
}
