/**
 * Đồ họ mới + trang phục (0.9.8) — logic THUẦN dùng chung cho API /api/do-trang-phuc và trang Cẩm nang.
 *
 * Nguồn dữ liệu: bảng CSDL do sql/them_do_trang_phuc.sql tạo (sinh bởi tools/do_trang_phuc/sinh.py) hoặc, khi CSDL chưa
 * có bảng, shared/data/danh-muc-do-trang-phuc.json (cùng bộ sinh). Hai nguồn ra CÙNG một hình dạng (xem hangThanhDanhMuc).
 * File .mjs (JS thuần) để test chạy được bằng `node --test` không cần bỏ kiểu TypeScript.
 */

/** Id ảnh icon đầu dải (máy chủ game: com.htbb.pokemon.IconDoMoi) — cột `icon` trong CSDL = ID_ANH_DAU + n. */
export const ID_ANH_DAU = 15000

/** Đường dẫn icon tĩnh trên web cho số thứ tự n (web/public/icon-do/<n>.png). */
export function duongIcon(n) {
  return `/icon-do/${Number(n)}.png`
}

export const TEN_BAC = ['sơ cấp', 'đồng', 'bạc', 'vàng', 'bạch kim', 'kim cương', 'chuyên gia', 'tinh anh', 'huyền thoại',
  'thần thoại', 'tối thượng']
export const CAP_BAC = [1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 98]
export const TEN_HE = ['Thường', 'Lửa', 'Nước', 'Điện', 'Cỏ', 'Băng', 'Giác đấu', 'Độc', 'Đất', 'Bay', 'Siêu linh', 'Bọ', 'Đá',
  'Ma', 'Rồng', 'Bóng tối', 'Thép', 'Tiên']

/** type data_item -> mã ô (khớp tools/do_trang_phuc/sinh.py). */
const O_THEO_TYPE = { 0: 'khan_quang', 1: 'dai', 2: 'bang_do', 3: 'vu_khi', 4: 'vu_khi', 5: 'vu_khi', 6: 'vu_khi', 7: 'vu_khi',
  8: 'huy_hieu', 9: 'vong_co', 10: 'long_vu', 11: 'gang_dam', 12: 'phien_da', 19: 'ruy_bang' }

export const TEN_O = {
  khan_quang: 'Khăn quàng', dai: 'Đai', bang_do: 'Băng đô', vu_khi: 'Đồ cầm tay', huy_hieu: 'Huy hiệu', vong_co: 'Vòng cổ',
  long_vu: 'Lông vũ', gang_dam: 'Găng', phien_da: 'Phiến đá', ruy_bang: 'Ruy băng',
}

export const TEN_NGUON_DO = {
  quai_he: 'Quái cùng hệ', quai_vung: 'Quái trong vùng', boss: 'Boss', che_tao: 'Chế tạo (Machamp)', gym: 'Gym',
}

export const TEN_CHI_SO_BO = {
  st_he: 'sát thương hệ', khang_he_khac: 'kháng hệ khắc', hp: 'HP', tan_cong: 'tấn công', chi_mang: 'chí mạng',
}

/** Tên sự kiện theo mã lịch năm (com.htbb.pokemon.LichNam.DANH_MUC). */
export const TEN_SU_KIEN = {
  'tetduonglich': 'Tết Dương lịch', 'tet2017': 'Tết Nguyên Đán', 'tet123': 'Ba ngày Tết', 'moi:valentine': 'Valentine',
  'quocte83': 'Quốc tế Phụ nữ 8/3', 'gioto': 'Giỗ Tổ Hùng Vương', 'moi:3004': 'Lễ 30/4 - 1/5',
  'moi:thieunhi': 'Quốc tế Thiếu nhi 1/6', 'he2017': 'Sự kiện Hè', 'moi:hamngucmua': 'Hầm ngục bí ẩn mùa hè',
  'vulan': 'Vu Lan', 'moi:quockhanh': 'Quốc khánh 2/9', 'trungthu': 'Trung Thu', 'moi:2010': 'Phụ nữ Việt Nam 20/10',
  'halloween': 'Halloween', 'demhalloween': 'Đêm Halloween', 'sinhnhat2011': 'Ngày Nhà giáo Việt Nam 20/11',
  'noel': 'Giáng sinh (Noel)', 'demnoel': 'Đêm Noel', 'moi:tongket': 'Tổng kết năm', 'moi:tuanhe': 'Tuần hệ',
  'moi:congdong': 'Ngày cộng đồng', 'moi:shiny': 'Tuần Shiny', 'moi:dotkich': 'Đột kích huyền thoại',
  'moi:muapvp': 'Kết thúc mùa PvP', 'moi:pokedex': 'Lễ hội Pokédex', 'moi:chienvung': 'Chiến vùng',
  'moi:bungphat': 'Bùng phát quần thể',
}

/** Nguyên liệu chế tạo trang trí (data_item). */
export const TEN_NGUYEN_LIEU = { 1034: 'Bụi sao', 1035: 'Mảnh sao', 1036: 'Mảnh thiên thạch', 1037: 'Vảy rồng' }

export const TEN_DO_HIEM = ['Thường', 'Hiếm', 'Quý', 'Sử thi', 'Huyền thoại']

/** Phần nghìn -> "1,5" (phần trăm kiểu Việt). */
export function phanTram(phanNghin) {
  const n = Number(phanNghin) || 0
  return n % 10 === 0 ? String(n / 10) : `${Math.trunc(n / 10)},${Math.abs(n % 10)}`
}

/** "a:1;b:x,y" -> { a: '1', b: 'x,y' } */
function tachThamSo(s) {
  const ra = {}
  for (const phan of String(s ?? '').split(';')) {
    const i = phan.indexOf(':')
    if (i > 0) ra[phan.slice(0, i).trim()] = phan.slice(i + 1).trim()
  }
  return ra
}

/** Mô tả nguồn của một món trang trí cho người chơi đọc. */
export function moTaNguon(t) {
  const ts = tachThamSo(t.thamSo)
  switch (t.nguon) {
    case 'mien_phi':
      return `Miễn phí khi đạt cấp ${ts.cap ?? '?'}`
    case 'su_kien': {
      // lich:<ma>,<ma>  (mã có thể chứa ':' như moi:valentine -> tách thủ công)
      const lich = String(t.thamSo ?? '').split(';').find(p => p.startsWith('lich:'))?.slice(5) ?? ''
      const ten = lich.split(',').filter(Boolean).map(m => TEN_SU_KIEN[m] ?? m)
      return `Sự kiện ${ten.join(' / ')}: hạ ${ts.quai ?? '?'} Pokémon hoang dã trong thời gian sự kiện`
    }
    case 'thanh_tuu':
      return `Thành tựu: danh hiệu ${ts.danh_hieu ?? '?'}`
    case 'cua_hang':
      return `Cửa hàng: ${Number(t.giaLuong) || 0} lượng`
    case 'che_tao': {
      const nl = String(ts.nl ?? '').split(',').filter(Boolean).map((p) => {
        const [id, n] = p.split('x')
        return `${n} ${TEN_NGUYEN_LIEU[Number(id)] ?? `vật phẩm ${id}`}`
      })
      const gio = ts.gio ? ` + xu bằng ${ts.gio} giờ thu nhập` : ''
      return `Chế tạo (Machamp): ${nl.join(', ')}${gio}`
    }
    default:
      return t.nguon || '—'
  }
}

function soHoac(v, macDinh) {
  const n = Number(v)
  return Number.isFinite(n) ? n : macDinh
}

/**
 * Hàng CSDL (5 bảng) -> danh mục cùng hình dạng shared/data/danh-muc-do-trang-phuc.json.
 * Chỉ dòng bat = 1 (bảng có cột bat). Không có cột / dữ liệu riêng tư nào của người chơi.
 *
 * @param {{ ho: any[], thuong: any[], moi: any[], o: any[], trangPhuc: any[] }} h
 * @param {{ nganSachDanhHieu?: number, tranTong?: number }} [them]
 */
export function hangThanhDanhMuc(h, them = {}) {
  const thuongTheoHo = new Map()
  for (const r of h.thuong ?? []) {
    const ds = thuongTheoHo.get(String(r.ma_ho)) ?? []
    ds.push({ soMon: soHoac(r.so_mon, 0), chiSo: String(r.chi_so), phanNghin: soHoac(r.phan_nghin, 0) })
    thuongTheoHo.set(String(r.ma_ho), ds)
  }
  for (const ds of thuongTheoHo.values()) ds.sort((a, b) => a.soMon - b.soMon || a.chiSo.localeCompare(b.chiSo))
  const ho = (h.ho ?? []).filter(r => soHoac(r.bat, 1) === 1)
    .sort((a, b) => soHoac(a.thu_tu, 0) - soHoac(b.thu_tu, 0))
  const coHo = new Set(ho.map(r => String(r.ma)))
  const bo = ho.map((r) => {
    const thuong = thuongTheoHo.get(String(r.ma)) ?? []
    if (String(r.loai) === 'vung') {
      return { ma: String(r.ma), ten: String(r.ten), loai: 'vung', vung: String(r.vung), bacTu: soHoac(r.bac_tu, 0),
        bacDen: soHoac(r.bac_den, 10), moTa: String(r.mo_ta ?? ''), thuong }
    }
    const he = soHoac(r.he, -1)
    const heKhac = soHoac(r.he_khac, -1)
    return { ma: String(r.ma), ten: String(r.ten), loai: 'he', he: TEN_HE[he] ?? '', heKhac: TEN_HE[heKhac] ?? '',
      moTa: String(r.mo_ta ?? ''), thuong }
  })
  const doMoi = (h.moi ?? []).filter(r => coHo.has(String(r.ma_ho)))
    .map(r => ({
      id: soHoac(r.id_item, 0), ten: String(r.ten), type: soHoac(r.type, 0), o: O_THEO_TYPE[soHoac(r.type, -1)] ?? 'khac',
      bac: soHoac(r.bac, 0), maHo: String(r.ma_ho), icon: soHoac(r.icon, ID_ANH_DAU) - ID_ANH_DAU,
      nguon: String(r.nguon ?? '').split(',').map(s => s.trim()).filter(Boolean),
    }))
    .sort((a, b) => a.id - b.id)
  const oTrangTri = [...(h.o ?? [])].sort((a, b) => soHoac(a.thu_tu, 0) - soHoac(b.thu_tu, 0)).map(r => ({
    o: String(r.o), ten: String(r.ten), phanNghin: soHoac(r.phan_nghin, 0),
    dong: String(r.dong ?? '').split(',').filter(Boolean).map((p) => {
      const i = p.indexOf(':')
      return i > 0 ? { ma: p.slice(0, i), ten: p.slice(i + 1) } : { ma: p, ten: p }
    }),
  }))
  const trangTri = (h.trangPhuc ?? []).filter(r => soHoac(r.bat, 1) === 1)
    .sort((a, b) => soHoac(a.thu_tu, 0) - soHoac(b.thu_tu, 0))
    .map(r => ({
      ma: String(r.ma), o: String(r.o), so: soHoac(r.so, 0), ten: String(r.ten), doHiem: soHoac(r.do_hiem, 0),
      icon: soHoac(r.icon, ID_ANH_DAU) - ID_ANH_DAU, nguon: String(r.nguon), thamSo: String(r.tham_so ?? ''),
      giaLuong: soHoac(r.gia_luong, 0), moTa: String(r.mo_ta ?? ''),
    }))
  return {
    idAnhDau: ID_ANH_DAU, bac: TEN_BAC, capBac: CAP_BAC, he: TEN_HE, bo, do: doMoi, oTrangTri,
    nganSachDanhHieu: soHoac(them.nganSachDanhHieu, 10), tranTong: soHoac(them.tranTong, 50), trangTri,
  }
}

/** Tổng ngân sách trang trí (phần nghìn) = các ô + danh hiệu. */
export function tongNganSach(dm) {
  return (dm.oTrangTri ?? []).reduce((s, o) => s + (Number(o.phanNghin) || 0), 0) + (Number(dm.nganSachDanhHieu) || 0)
}
