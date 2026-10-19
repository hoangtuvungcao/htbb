/**
 * Bảng xếp hạng công khai — phần THUẦN (không đụng CSDL, không import gì): danh mục bảng,
 * khuôn cài đặt (web_cai_dat khoá 'xepHang'), luật loại trừ, các công thức chép từ game.
 * Dùng chung cho server (server/utils/xepHang.ts), trang /ranking, trang /admin/xep-hang và
 * tests/xep-hang.test.ts (chạy thẳng bằng node --test nên file này KHÔNG được import gì).
 *
 * Nguồn game (repo HTBB, src/):
 *   - Bảng Top trong game: real/MapXuLyLenh.java doSendTop (3 đại gia, 4 sức mạnh, 5 đội, 7 theo phe),
 *     real/Map.java getAllTopBegin + doSendTopNation, data/DatabaseXepHang.java getTopLV/getTopRich
 *     (+ dieuKienLoaiAdmin), data/Database.java getTopCountry/getTopClan/getTopEvent.
 *     Game nạp lại các top này mỗi 60 giây (server/TeamServer.java, luồng "LAM MOI TOP").
 *   - Hệ thống mới (game CHƯA có bảng top): tob_pokedex*, tob_pvp_mua (ThangPvp.mua), Gym (gh= trong
 *     tob_char_lau_dai), tob_ham_nguc (NangLuong.ngay), tob_hst_nhan_vat (HoSoSinhThai, KyNangSong),
 *     thành tựu (data_quest_lau_dai kieu_muc_tieu thanh_tuu*, tob_nhiem_vu_lau_dai), chiếm Gym
 *     (tob_log_clan cgt/gcok/pcok, tob_chiem_thanh_tuan).
 */

// ------------------------------------------------------------------ danh mục bảng

export type XhMaBang =
  | 'suc-manh' | 'dai-gia' | 'cong-trang' | 'chuoi-thang' | 'doi' | 'chiem-gym'
  | 'pokedex' | 'pvp' | 'gym' | 'ham-nguc' | 'sinh-thai' | 'thanh-tuu' | 'su-kien'

export interface XhLuaChon { gt: string, nhan: string }
export interface XhBoLoc {
  khoa: 'vung' | 'kieu' | 'ky' | 'ngay' | 'mua'
  nhan: string
  /** Rỗng với 'mua' (danh sách mùa lấy từ CSDL lúc chạy). */
  lua: XhLuaChon[]
  macDinh: string
}

export interface XhBangDinhNghia {
  ma: XhMaBang
  ten: string
  icon: string
  nhom: 'Bảng Top trong game' | 'Hệ thống mới' | 'Đội & chiếm Gym' | 'Sự kiện' | 'Ủng hộ'
  moTa: string
  /** Nguồn dữ liệu (để admin đối chiếu với game). */
  nguon: string
  /** true = chép đúng một bảng có sẵn trong menu "Bảng Top" của game. */
  trongGame: boolean
  /** Số dòng tối đa game lấy (LIMIT trong Java); web không vượt quá để web == game. 0 = không giới hạn riêng. */
  gioiHanGame: number
  /** Bảng CSDL bắt buộc phải có (thiếu thì bảng tự ẩn). */
  bang: string[]
  boLoc: XhBoLoc[]
  macDinhHien: boolean
}

export const XH_VUNG = ['Kanto', 'Johto', 'Hoenn'] as const

const LOC_VUNG_BAT_BUOC: XhBoLoc = {
  khoa: 'vung', nhan: 'Vùng', macDinh: 'kanto',
  lua: [{ gt: 'kanto', nhan: 'Kanto' }, { gt: 'johto', nhan: 'Johto' }, { gt: 'hoenn', nhan: 'Hoenn' }],
}

export const XH_DANH_MUC: readonly XhBangDinhNghia[] = [
  {
    ma: 'suc-manh', ten: 'Top sức mạnh', icon: 'i-lucide-trophy', nhom: 'Bảng Top trong game', trongGame: true, gioiHanGame: 100,
    moTa: 'Xếp theo tổng kinh nghiệm — đúng bảng "Top sức mạnh" trong game. Chọn vùng để xem "Top sức mạnh Kanto/Johto/Hoenn".',
    nguon: 'Toàn server: DatabaseXepHang.getTopLV (tob_char ORDER BY xp DESC LIMIT 100). Theo vùng: Database.getTopCountry(vùng, 0) (tob_char_luong ORDER BY exp DESC LIMIT 50).',
    bang: ['tob_char'], macDinhHien: true,
    boLoc: [{ khoa: 'vung', nhan: 'Vùng', macDinh: 'tat-ca', lua: [{ gt: 'tat-ca', nhan: 'Toàn server' }, ...LOC_VUNG_BAT_BUOC.lua] }],
  },
  {
    ma: 'dai-gia', ten: 'Top đại gia', icon: 'i-lucide-coins', nhom: 'Bảng Top trong game', trongGame: true, gioiHanGame: 100,
    moTa: 'Xếp theo số xu đang có — đúng bảng "Top đại gia" trong game.',
    nguon: 'DatabaseXepHang.getTopRich (tob_char ORDER BY gold DESC LIMIT 100). Sự kiện "anxuluongtop" bật thì game ẩn số xu — web cũng ẩn.',
    bang: ['tob_char'], macDinhHien: true, boLoc: [],
  },
  {
    ma: 'cong-trang', ten: 'Top công trạng', icon: 'i-lucide-shield', nhom: 'Bảng Top trong game', trongGame: true, gioiHanGame: 50,
    moTa: 'Công trạng (danh dự) kiếm được khi hạ Pokémon vùng khác — "Top công trạng Kanto/Johto/Hoenn".',
    nguon: 'Database.getTopCountry(vùng, 1) (tob_char_luong WHERE country=? ORDER BY honor DESC LIMIT 50).',
    bang: ['tob_char_luong', 'tob_char'], macDinhHien: true, boLoc: [LOC_VUNG_BAT_BUOC],
  },
  {
    ma: 'chuoi-thang', ten: 'Top chuỗi thắng', icon: 'i-lucide-flame', nhom: 'Bảng Top trong game', trongGame: true, gioiHanGame: 10,
    moTa: 'Chuỗi hạ đối thủ dài nhất — "Top chuỗi thắng Kanto/Johto/Hoenn" (game đặt lại lúc 20:00 khi mở chiến vùng).',
    nguon: 'Database.getTopCountry(vùng, 2) (tob_char_luong WHERE country=? ORDER BY nPK DESC LIMIT 10).',
    bang: ['tob_char_luong', 'tob_char'], macDinhHien: true, boLoc: [LOC_VUNG_BAT_BUOC],
  },
  {
    ma: 'doi', ten: 'Top đội', icon: 'i-lucide-users', nhom: 'Đội & chiếm Gym', trongGame: true, gioiHanGame: 100,
    moTa: 'Các đội mạnh nhất theo kinh nghiệm đội — đúng bảng "Top đội" trong game.',
    nguon: 'Database.getTopClan (tob_clan ORDER BY xp DESC LIMIT 100); số thành viên đếm như game (Pokémon có idClan = đội).',
    bang: ['tob_clan', 'tob_char'], macDinhHien: true, boLoc: [],
  },
  {
    ma: 'chiem-gym', ten: 'Chiếm Gym', icon: 'i-lucide-castle', nhom: 'Đội & chiếm Gym', trongGame: false, gioiHanGame: 0,
    moTa: 'Số lần chiếm Gym của đội, số lần giao thẻ / phá thẻ và điểm đóng góp chiến Gym của từng Pokémon.',
    nguon: 'tob_log_clan (actioninfo cgt = đội chiếm Gym, gcok = giao thẻ thành công, pcok = phá thẻ; NpcReceiveCard.java, MapDanhNhau.java) và tob_chiem_thanh_tuan (ChiemThanhLauDai.java).',
    bang: ['tob_log_clan', 'tob_char'], macDinhHien: true,
    boLoc: [
      { khoa: 'kieu', nhan: 'Bảng', macDinh: 'giao-the', lua: [{ gt: 'giao-the', nhan: 'Giao thẻ' }, { gt: 'pha-the', nhan: 'Phá thẻ' }, { gt: 'doi', nhan: 'Đội chiếm Gym' }, { gt: 'dong-gop', nhan: 'Đóng góp' }] },
      { khoa: 'ky', nhan: 'Thời gian', macDinh: 'moc', lua: [{ gt: 'moc', nhan: 'Đợt đua hiện tại' }, { gt: 'tuan', nhan: 'Tuần này' }, { gt: 'tat-ca', nhan: 'Mọi thời điểm' }] },
    ],
  },
  {
    ma: 'pokedex', ten: 'Pokédex', icon: 'i-lucide-book-open', nhom: 'Hệ thống mới', trongGame: false, gioiHanGame: 0,
    moTa: 'Ai đã gặp / đã hạ nhiều loài Pokémon nhất, ai bắt gặp nhiều loài shiny nhất. Bằng nhau thì ai đạt trước đứng trước.',
    nguon: 'tob_pokedex (một dòng = một loài đã gặp; so_ha > 0 = đã hạ; data/DatabasePokedex.java), tob_pokedex_shiny (DatabaseLauDai.congShiny).',
    bang: ['tob_pokedex', 'tob_char'], macDinhHien: true,
    boLoc: [{ khoa: 'kieu', nhan: 'Tiêu chí', macDinh: 'da-ha', lua: [{ gt: 'da-ha', nhan: 'Loài đã hạ' }, { gt: 'da-gap', nhan: 'Loài đã gặp' }, { gt: 'shiny', nhan: 'Loài shiny' }] }],
  },
  {
    ma: 'pvp', ten: 'PvP mùa giải', icon: 'i-lucide-swords', nhom: 'Hệ thống mới', trongGame: false, gioiHanGame: 0,
    moTa: 'Điểm Elo PvP theo mùa (13 tuần một mùa). Bậc: Poké Ball < 1200 ≤ Great Ball < 1400 ≤ Ultra Ball < 1600 ≤ Master Ball.',
    nguon: 'tob_pvp_mua (DatabaseLauDai.ghiPvp). Mùa = ngày từ khai trương / 91 (ThangPvp.mua, LauDai.ngayHienTai).',
    bang: ['tob_pvp_mua', 'tob_char'], macDinhHien: true,
    boLoc: [{ khoa: 'mua', nhan: 'Mùa', macDinh: '', lua: [] }],
  },
  {
    ma: 'gym', ten: 'Huy hiệu Gym', icon: 'i-lucide-medal', nhom: 'Hệ thống mới', trongGame: false, gioiHanGame: 0,
    moTa: 'Số huy hiệu Gym đã giành (tối đa 16).',
    nguon: 'tob_char_lau_dai.du_lieu khoá gh= (bitmask huy hiệu, real/GymLauDai.java) — đếm số bit.',
    bang: ['tob_char_lau_dai', 'tob_char'], macDinhHien: true, boLoc: [],
  },
  {
    ma: 'ham-nguc', ten: 'Hầm Ngục Bí Ẩn', icon: 'i-lucide-mountain', nhom: 'Hệ thống mới', trongGame: false, gioiHanGame: 0,
    moTa: 'Tầng cao nhất vượt được trong ngày (hầm đổi mỗi 00:00). Bằng tầng thì ai tới trước đứng trước.',
    nguon: 'tob_ham_nguc WHERE ngay = yyyyMMdd (NangLuong.ngay, DatabaseLauDai.ghiHamNguc).',
    bang: ['tob_ham_nguc', 'tob_char'], macDinhHien: true,
    boLoc: [{ khoa: 'ngay', nhan: 'Ngày', macDinh: 'hom-nay', lua: [{ gt: 'hom-nay', nhan: 'Hôm nay' }, { gt: 'hom-qua', nhan: 'Hôm qua' }] }],
  },
  {
    ma: 'sinh-thai', ten: 'Kỹ năng sinh thái', icon: 'i-lucide-fish', nhom: 'Hệ thống mới', trongGame: false, gioiHanGame: 0,
    moTa: 'Cấp kỹ năng sống (câu cá, trồng trọt, khảo cổ, nấu ăn, chế thuốc) và số ảnh trong album. Chỉ hiện khi máy chủ bật hệ sinh thái.',
    nguon: 'tob_hst_nhan_vat.du_lieu (HoSoSinhThai: kn= EXP 5 kỹ năng, al= album); cấp tính như KyNangSong.capTuExp.',
    bang: ['tob_hst_nhan_vat', 'tob_char'], macDinhHien: true,
    boLoc: [{ khoa: 'kieu', nhan: 'Kỹ năng', macDinh: 'cau-ca', lua: [
      { gt: 'cau-ca', nhan: 'Câu cá' }, { gt: 'trong-trot', nhan: 'Trồng trọt' }, { gt: 'khao-co', nhan: 'Khảo cổ' },
      { gt: 'nau-an', nhan: 'Nấu ăn' }, { gt: 'che-thuoc', nhan: 'Chế thuốc' }, { gt: 'album', nhan: 'Album ảnh' },
    ] }],
  },
  {
    ma: 'thanh-tuu', ten: 'Thành tựu', icon: 'i-lucide-star', nhom: 'Hệ thống mới', trongGame: false, gioiHanGame: 0,
    moTa: 'Số thành tựu đã hoàn thành (game không lưu "điểm thành tựu" nên đếm số thành tựu).',
    nguon: 'tob_nhiem_vu_lau_dai (trang_thai ≥ 1) với mã thuộc data_quest_lau_dai có kieu_muc_tieu bắt đầu bằng thanh_tuu.',
    bang: ['tob_nhiem_vu_lau_dai', 'data_quest_lau_dai', 'tob_char'], macDinhHien: true, boLoc: [],
  },
  {
    ma: 'su-kien', ten: 'Sự kiện', icon: 'i-lucide-party-popper', nhom: 'Sự kiện', trongGame: true, gioiHanGame: 10,
    moTa: 'Điểm sự kiện đang chạy (top 10 như NPC sự kiện trong game) và top thả đèn Trung Thu.',
    nguon: 'Điểm: Database.getTopEvent (tob_top_event ORDER BY point1 DESC LIMIT 10). Thả đèn: DatabaseCanMocThaDen.layTopCaNhan (tob_canmoc_thadeon_canhan ORDER BY so_lan DESC, cap_nhat_luc ASC LIMIT 10).',
    bang: ['tob_top_event', 'tob_char'], macDinhHien: true,
    boLoc: [{ khoa: 'kieu', nhan: 'Bảng', macDinh: 'diem', lua: [{ gt: 'diem', nhan: 'Điểm sự kiện' }, { gt: 'tha-den', nhan: 'Thả đèn Trung Thu' }] }],
  },
]

export const XH_MA_BANG: readonly XhMaBang[] = XH_DANH_MUC.map(b => b.ma)

export function xhBang(ma: string): XhBangDinhNghia | undefined {
  return XH_DANH_MUC.find(b => b.ma === ma)
}

// ------------------------------------------------------------------ cài đặt (web_cai_dat 'xepHang')

export const XH_KHOA_CAI_DAT = 'xepHang'

export interface XhCaiDat {
  /** Thứ tự = thứ tự tab. Mỗi bảng đúng một lần. */
  bang: Array<{ ma: XhMaBang, hien: boolean }>
  /** Số dòng tối đa mỗi bảng (10..200); bảng chép từ game còn bị chặn bởi LIMIT của game. */
  soDong: number
  /** Số dòng mỗi trang trên web (5..50). */
  moiTrang: number
  /** Bộ nhớ đệm phía server, giây (15..3600). Game tự làm mới mỗi 60 giây. */
  lamMoiGiay: number
  loaiTru: {
    /** Tên nhân vật; kết thúc bằng * = theo tiền tố (vd. tai0*). Không phân biệt hoa/thường. */
    tenNhanVat: string[]
    /** Tên đăng nhập; kết thúc bằng * = theo tiền tố. Chỉ dùng ở server, không bao giờ trả ra trang công khai. */
    taiKhoan: string[]
    /** Ẩn nhân vật của tài khoản bị khoá (account.team_user.ban = 1). */
    anTaiKhoanKhoa: boolean
    /** Ẩn GM trong sv.adminAccounts (cau_hinh) — game cũng ẩn ở Top sức mạnh / đại gia. */
    anGm: boolean
  }
  /** Mốc bắt đầu đợt đua "Chiếm Gym" ('YYYY-MM-DD HH:MM:SS', giờ máy chủ). */
  mocChiemGym: string
}

export const XH_CAI_DAT_MAC_DINH: XhCaiDat = {
  bang: XH_DANH_MUC.map(b => ({ ma: b.ma, hien: b.macDinhHien })),
  soDong: 100,
  moiTrang: 10,
  lamMoiGiay: 60,
  loaiTru: { tenNhanVat: [], taiKhoan: [], anTaiKhoanKhoa: true, anGm: true },
  mocChiemGym: '2026-09-12 20:12:29',
}

export const XH_GIOI_HAN = { soDong: [10, 200], moiTrang: [5, 50], lamMoiGiay: [15, 3600], soMauLoaiTru: 200 } as const

const MAU_TEN = /^[A-Za-z0-9_]{1,30}\*?$/

function soNguyen(v: unknown, min: number, max: number, macDinh: number): number {
  const n = Math.trunc(Number(v))
  if (!Number.isFinite(n)) return macDinh
  return Math.min(max, Math.max(min, n))
}

/** Danh sách mẫu tên: tách dấu phẩy / xuống dòng, bỏ trùng, viết thường; mẫu sai trả về ở `loi`. */
export function xhTachMau(v: unknown): { mau: string[], loi: string[] } {
  const tho = Array.isArray(v) ? v.map(x => String(x ?? '')) : String(v ?? '').split(/[\n,;]+/)
  const mau: string[] = []
  const loi: string[] = []
  for (const t of tho) {
    const s = t.trim().toLowerCase()
    if (!s) continue
    if (!MAU_TEN.test(s) || s === '*') { loi.push(t.trim().slice(0, 40)); continue }
    if (!mau.includes(s)) mau.push(s)
  }
  return { mau: mau.slice(0, XH_GIOI_HAN.soMauLoaiTru), loi }
}

const MAU_NGAY_GIO = /^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})(?::(\d{2}))?$/

/** Nắn cài đặt (dữ liệu hỏng không được làm vỡ trang): khoá lạ bỏ, sai kiểu dùng mặc định, bảng thiếu thêm vào cuối. */
export function xhChuanHoaCaiDat(raw: unknown): XhCaiDat {
  const r = (raw && typeof raw === 'object' && !Array.isArray(raw) ? raw : {}) as Record<string, any>
  const md = XH_CAI_DAT_MAC_DINH
  const bang: XhCaiDat['bang'] = []
  if (Array.isArray(r.bang)) {
    for (const b of r.bang) {
      const ma = String(b?.ma ?? '') as XhMaBang
      if (!XH_MA_BANG.includes(ma) || bang.some(x => x.ma === ma)) continue
      bang.push({ ma, hien: typeof b?.hien === 'boolean' ? b.hien : !!xhBang(ma)?.macDinhHien })
    }
  }
  for (const b of XH_DANH_MUC) if (!bang.some(x => x.ma === b.ma)) bang.push({ ma: b.ma, hien: b.macDinhHien })
  const lt = (r.loaiTru && typeof r.loaiTru === 'object' ? r.loaiTru : {}) as Record<string, any>
  const moc = typeof r.mocChiemGym === 'string' ? r.mocChiemGym.trim().match(MAU_NGAY_GIO) : null
  return {
    bang,
    soDong: soNguyen(r.soDong, XH_GIOI_HAN.soDong[0], XH_GIOI_HAN.soDong[1], md.soDong),
    moiTrang: soNguyen(r.moiTrang, XH_GIOI_HAN.moiTrang[0], XH_GIOI_HAN.moiTrang[1], md.moiTrang),
    lamMoiGiay: soNguyen(r.lamMoiGiay, XH_GIOI_HAN.lamMoiGiay[0], XH_GIOI_HAN.lamMoiGiay[1], md.lamMoiGiay),
    loaiTru: {
      tenNhanVat: xhTachMau(lt.tenNhanVat).mau,
      taiKhoan: xhTachMau(lt.taiKhoan).mau,
      anTaiKhoanKhoa: typeof lt.anTaiKhoanKhoa === 'boolean' ? lt.anTaiKhoanKhoa : md.loaiTru.anTaiKhoanKhoa,
      anGm: typeof lt.anGm === 'boolean' ? lt.anGm : md.loaiTru.anGm,
    },
    mocChiemGym: moc ? `${moc[1]}-${moc[2]}-${moc[3]} ${moc[4]}:${moc[5]}:${moc[6] ?? '00'}` : md.mocChiemGym,
  }
}

/**
 * Bảng có được hiện công khai không (chưa tính bảng CSDL có tồn tại hay không).
 * (Bảng "Top ủng hộ" cũ đã bỏ: nó đọc board_created — hàng chờ quà tạo nhân vật, không phải tiền ủng hộ.
 * Tham số thứ ba giữ cho tương thích chữ ký, không còn dùng.)
 */
export function xhBangDuocHien(cd: XhCaiDat, ma: XhMaBang, _napBat?: boolean): boolean {
  return !!cd.bang.find(b => b.ma === ma)?.hien
}

// ------------------------------------------------------------------ loại trừ

export interface XhLuatTen { chinhXac: string[], tienTo: string[] }

/** Tách mẫu thành tên chính xác + tiền tố (mẫu kết thúc '*'). */
export function xhLuatTen(mau: readonly string[]): XhLuatTen {
  const chinhXac: string[] = []
  const tienTo: string[] = []
  for (const m of mau) {
    const s = m.trim().toLowerCase()
    if (!s) continue
    if (s.endsWith('*')) { if (s.length > 1) tienTo.push(s.slice(0, -1)) }
    else chinhXac.push(s)
  }
  return { chinhXac: [...new Set(chinhXac)], tienTo: [...new Set(tienTo)] }
}

export function xhKhopLuatTen(ten: string, luat: XhLuatTen): boolean {
  const s = ten.trim().toLowerCase()
  return luat.chinhXac.includes(s) || luat.tienTo.some(t => s.startsWith(t))
}

/**
 * Điều kiện SQL loại trừ trên bảng tob_char (bí danh `a`). Chỉ dùng hàm có ở cả MariaDB lẫn NovaDB
 * (LOWER, SUBSTR) — không LIKE (dấu _ là ký tự đại diện, mỗi CSDL thoát khác nhau).
 * Tên luôn đi qua tham số; độ dài tiền tố là số nguyên tính từ chuỗi đã kiểm (^[a-z0-9_]+$).
 */
export function xhSqlLoaiTru(a: string, luat: XhLuatTen, userIdBiAn: readonly number[]): { sql: string, params: unknown[] } {
  if (!/^[a-z]+$/.test(a)) throw new Error('bi danh khong hop le')
  const phan: string[] = [`${a}.ban = 0`]
  const params: unknown[] = []
  if (luat.chinhXac.length) {
    phan.push(`LOWER(${a}.charname) NOT IN (${luat.chinhXac.map(() => '?').join(',')})`)
    params.push(...luat.chinhXac)
  }
  for (const t of luat.tienTo) {
    if (!/^[a-z0-9_]{1,30}$/.test(t)) continue
    phan.push(`SUBSTR(LOWER(${a}.charname), 1, ${t.length}) <> ?`)
    params.push(t)
  }
  const ids = [...new Set(userIdBiAn.map(n => Math.trunc(Number(n))).filter(n => Number.isFinite(n)))]
  if (ids.length) phan.push(`${a}.userId NOT IN (${ids.join(',')})`)
  return { sql: phan.join(' AND '), params }
}

// ------------------------------------------------------------------ công thức chép từ game

/** NangLuong.ngay: yyyyMMdd theo giờ máy (đổi lúc 00:00). */
export function xhNgaySo(d: Date): number {
  return d.getFullYear() * 10000 + (d.getMonth() + 1) * 100 + d.getDate()
}

/** 'yyyy-MM-dd' -> millis 00:00 giờ máy (LauDai.docNgay); sai -> 0. */
export function xhDocNgay(s: string | null | undefined): number {
  const m = String(s ?? '').trim().match(/^(\d{4})-(\d{2})-(\d{2})$/)
  if (!m) return 0
  const d = new Date(Number(m[1]), Number(m[2]) - 1, Number(m[3]), 0, 0, 0, 0)
  return Number.isNaN(d.getTime()) ? 0 : d.getTime()
}

/** ThangPvp.TUAN_MOI_MUA. */
export const XH_TUAN_MOI_MUA = 13

/** Mùa PvP hiện tại (0 = mùa 1) = max(0, ngày từ khai trương + tua nhanh) / 91 (ThangPvp.mua + LauDai.ngayHienTai). */
export function xhMuaPvp(khaiTruongMs: number, bayGioMs: number, tuaNhanhNgay = 0): number {
  const ngay = (khaiTruongMs <= 0 || bayGioMs <= khaiTruongMs ? 0 : Math.floor((bayGioMs - khaiTruongMs) / 86_400_000)) + tuaNhanhNgay
  return Math.floor(Math.max(0, ngay) / (XH_TUAN_MOI_MUA * 7))
}

export const XH_TEN_BAC_PVP = ['Poké Ball', 'Great Ball', 'Ultra Ball', 'Master Ball'] as const
const NGUONG_BAC = [1200, 1400, 1600]

/** ThangPvp.bac. */
export function xhBacPvp(elo: number): number {
  let b = 0
  for (let i = 0; i < NGUONG_BAC.length; i++) if (elo >= NGUONG_BAC[i]!) b = i + 1
  return b
}

/** HoSoLauDai.maHoa: "k=v;k=v". */
export function xhDocLauDai(s: string | null | undefined): Record<string, string> {
  const ra: Record<string, string> = {}
  for (const p of String(s ?? '').split(';')) {
    const i = p.indexOf('=')
    if (i > 0) ra[p.slice(0, i).trim()] = p.slice(i + 1).trim()
  }
  return ra
}

/** Số huy hiệu Gym = số bit bật của gh (Integer.bitCount). */
export function xhSoHuyHieu(duLieu: string | null | undefined): number {
  const v = Math.trunc(Number(xhDocLauDai(duLieu).gh ?? 0))
  if (!Number.isFinite(v) || v <= 0) return 0
  let n = 0
  let x = v >>> 0
  while (x) { n += x & 1; x >>>= 1 }
  return n
}

export const XH_KY_NANG = ['cau-ca', 'trong-trot', 'khao-co', 'nau-an', 'che-thuoc'] as const
export const XH_CAP_KY_NANG_TOI_DA = 50

/** KyNangSong.expLenCap: EXP từ cấp L lên L+1 = round(40 × 1,105^L). */
export function xhExpLenCap(cap: number): number {
  return Math.round(40 * 1.105 ** cap)
}

/** KyNangSong.capTuExp. */
export function xhCapKyNang(exp: number): number {
  let cap = 1
  let con = Math.max(0, Math.trunc(Number(exp) || 0))
  while (cap < XH_CAP_KY_NANG_TOI_DA && con >= xhExpLenCap(cap)) {
    con -= xhExpLenCap(cap)
    cap++
  }
  return cap
}

/** HoSoSinhThai.doc: "v1|k=v|..." -> EXP 5 kỹ năng + số ảnh album. Sai phiên bản -> rỗng. */
export function xhDocSinhThai(s: string | null | undefined): { exp: number[], album: number } {
  const ra = { exp: [0, 0, 0, 0, 0], album: 0 }
  const t = String(s ?? '')
  if (!t.startsWith('v1')) return ra
  const m: Record<string, string> = {}
  for (const p of t.split('|')) {
    const i = p.indexOf('=')
    if (i > 0) m[p.slice(0, i)] = p.slice(i + 1)
  }
  const kn = (m.kn ?? '').split(',')
  for (let i = 0; i < 5; i++) {
    const v = Math.trunc(Number(kn[i] ?? 0))
    ra.exp[i] = Number.isFinite(v) && v > 0 ? v : 0
  }
  ra.album = (m.al ?? '').split('~').filter(Boolean).length
  return ra
}

/** Vùng của nhân vật từ tob_char.infotop (phần tử đầu, như getTopLV). -1 = chưa có. */
export function xhVungTuInfotop(infotop: string | null | undefined): number {
  const s = String(infotop ?? '').trim()
  if (!s || s === '-1') return -1
  const v = Math.trunc(Number(s.split(',')[0]))
  return v >= 0 && v <= 2 ? v : -1
}

/** gcok lưu "tên_idĐội": bỏ đuôi "_<số>" cuối cùng (thay SUBSTRING_INDEX mà NovaDB không có). */
export function xhTenGiaoThe(raw: string): string {
  const s = String(raw ?? '')
  const i = s.lastIndexOf('_')
  return i > 0 && /^-?\d+$/.test(s.slice(i + 1)) ? s.slice(0, i) : s
}

export interface XhDongDem { ten: string, so: number, cuoi: string, idCuoi: number }

/**
 * Gộp các dòng đã GROUP BY theo tên thô (sau khi chuẩn hoá tên) rồi xếp như bản cũ:
 * số lần giảm dần, ai đạt mốc sớm hơn (MAX(timeLog) nhỏ hơn) đứng trước, rồi theo id log, rồi tên.
 */
export function xhGopDem(rows: XhDongDem[], chuanTen: (s: string) => string = s => s): XhDongDem[] {
  const m = new Map<string, XhDongDem>()
  for (const r of rows) {
    const ten = chuanTen(r.ten)
    if (!ten) continue
    const c = m.get(ten)
    if (!c) { m.set(ten, { ten, so: r.so, cuoi: r.cuoi, idCuoi: r.idCuoi }); continue }
    c.so += r.so
    if (r.cuoi > c.cuoi) c.cuoi = r.cuoi
    if (r.idCuoi > c.idCuoi) c.idCuoi = r.idCuoi
  }
  return [...m.values()].sort((a, b) => b.so - a.so || (a.cuoi < b.cuoi ? -1 : a.cuoi > b.cuoi ? 1 : 0) || a.idCuoi - b.idCuoi || (a.ten < b.ten ? -1 : a.ten > b.ten ? 1 : 0))
}

const p2 = (n: number) => String(n).padStart(2, '0')
export function xhGioChuoi(d: Date): string {
  return `${d.getFullYear()}-${p2(d.getMonth() + 1)}-${p2(d.getDate())} ${p2(d.getHours())}:${p2(d.getMinutes())}:${p2(d.getSeconds())}`
}

/** Đầu tuần game: thứ Hai 05:00 giờ máy (NhiemVuLauDai.tuan lùi 5 giờ rồi tính tuần từ thứ Hai). */
export function xhDauTuanGame(bayGio: Date): Date {
  const t = new Date(bayGio.getTime() - 5 * 3600_000)
  const lui = (t.getDay() + 6) % 7 // thứ Hai = 0
  return new Date(t.getFullYear(), t.getMonth(), t.getDate() - lui, 5, 0, 0, 0)
}

/** Chọn giá trị bộ lọc hợp lệ từ query (sai/thiếu -> mặc định). 'mua' kiểm riêng theo danh sách mùa. */
export function xhChonBoLoc(bang: XhBangDinhNghia, query: Record<string, unknown>): Record<string, string> {
  const ra: Record<string, string> = {}
  for (const l of bang.boLoc) {
    const v = typeof query[l.khoa] === 'string' ? (query[l.khoa] as string).trim() : ''
    if (l.khoa === 'mua') { ra.mua = /^\d{1,4}$/.test(v) ? String(Number(v)) : ''; continue }
    ra[l.khoa] = l.lua.some(x => x.gt === v) ? v : l.macDinh
  }
  return ra
}

/** 0.9.7: id danh hiệu đang đeo trong tob_char.pInfo (vị trí 103, real.CharVongLap); -1 = không đeo / lỗi. */
export function xhIdDanhHieu(pInfo: unknown): number {
  const v = Number(String(pInfo ?? '').split(',')[103] ?? '')
  return Number.isInteger(v) && v > 0 ? v : -1
}
