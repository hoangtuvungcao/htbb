/**
 * Bảng xếp hạng công khai — phần đọc CSDL (chỉ SELECT, có LIMIT) + bộ nhớ đệm.
 * Danh mục bảng, cài đặt, công thức game: shared/utils/xepHangThuan.ts.
 *
 * Nguyên tắc:
 *   - Bảng chép từ menu "Bảng Top" của game dùng ĐÚNG bảng/thứ tự/LIMIT của Java (xem `nguon` trong
 *     XH_DANH_MUC); chỉ thêm khoá phụ cuối cùng (id tăng dần) để thứ tự cố định khi bằng điểm.
 *   - Loại trừ áp dụng cho MỌI bảng, phía server: tob_char.ban = 0 (như game), GM trong sv.adminAccounts
 *     (như DatabaseXepHang.dieuKienLoaiAdmin), tài khoản bị khoá (team_user.ban = 1) và danh sách admin
 *     tự nhập (tên nhân vật / tên đăng nhập, hỗ trợ tiền tố "tai0*").
 *   - Kết quả công khai chỉ có tên nhân vật / tên đội — không id, không tên đăng nhập, không IP.
 *   - SQL chạy được trên cả MariaDB (strict) lẫn NovaDB: không NOW()/TIMESTAMPDIFF/SUBSTRING_INDEX,
 *     thời gian tính ở Node rồi truyền tham số.
 */
import {
  XH_KHOA_CAI_DAT, XH_KY_NANG, XH_TEN_BAC_PVP, XH_VUNG, xhBacPvp, xhBang, xhBangDuocHien, xhCapKyNang, xhChonBoLoc, xhChuanHoaCaiDat,
  xhDauTuanGame, xhKhopLuatTen, xhDocNgay, xhDocSinhThai, xhGioChuoi, xhGopDem, xhLuatTen, xhMuaPvp, xhNgaySo, xhSoHuyHieu, xhSqlLoaiTru,
  xhIdDanhHieu, xhTenGiaoThe, xhVungTuInfotop,
} from '../../shared/utils/xepHangThuan'
import type { XhBangDinhNghia, XhCaiDat, XhDongDem, XhLuaChon, XhLuatTen, XhMaBang } from '../../shared/utils/xepHangThuan'
import { tenHienThiHlvAi } from '../../shared/utils/hlvAi'
import { dexHienTai, speciesName } from '../../shared/utils/pokemon'

// ------------------------------------------------------------------ kiểu kết quả công khai

export interface XhDong {
  hang: number
  /** Tên nhân vật (hoặc tên đội ở bảng đội). */
  ten: string
  /** Loài Pokémon hiện tại (rỗng với đội). */
  loai: string
  /** Số Pokédex của loài (0 = không có ảnh). */
  dex: number
  shiny: boolean
  /** 0 Kanto, 1 Johto, 2 Hoenn, -1 chưa rõ. */
  vung: number
  /** null = bị ẩn (sự kiện "ẩn xu/lượng ở bảng top" đang bật). */
  giaTri: number | null
  phu: string
  /** Bậc PvP (Poké/Great/Ultra/Master Ball). */
  bac?: string
  /** 0.9.7 danh hiệu đang đeo (data_danh_hieu): tên + độ hiếm 0..3. */
  danhHieu?: { ten: string, doHiem: number }
}

export interface XhKetQua {
  ma: XhMaBang
  ten: string
  laDoi: boolean
  cot: { ten: string, giaTri: string, phu: string }
  dong: XhDong[]
  loc: Record<string, string>
  ghiChu: string
  capNhatLuc: string
  loi: boolean
}

export interface XhBangCongKhai {
  ma: XhMaBang
  ten: string
  icon: string
  nhom: string
  moTa: string
  trongGame: boolean
  boLoc: Array<{ khoa: string, nhan: string, lua: XhLuaChon[], macDinh: string }>
}

// ------------------------------------------------------------------ cài đặt

/** Cài đặt đang dùng (đã nắn). Đệm 15 giây; admin lưu thì gọi xhXoaDem(). */
export function xhDocCaiDat(): Promise<XhCaiDat> {
  return quaDem('xep-hang-cai-dat', 15, xhDocCaiDatTuDb)
}

export async function xhDocCaiDatTuDb(): Promise<XhCaiDat> {
  return xhChuanHoaCaiDat(await xhDocCaiDatTho())
}

/** Giá trị JSON thô trong web_cai_dat (null = chưa lưu, dùng mặc định). */
export async function xhDocCaiDatTho(): Promise<unknown | null> {
  if (!(await coBangCms('web_cai_dat'))) return null
  try {
    const s = await qv<string>(dbGame(), 'SELECT gia_tri FROM web_cai_dat WHERE khoa = ? LIMIT 1', [XH_KHOA_CAI_DAT])
    return s == null ? null : JSON.parse(String(s))
  }
  catch { return null }
}

const demBang = new Map<string, { luc: number, kq?: XhKetQua, dang?: Promise<XhKetQua> }>()
let demLoaiTru: { luc: number, v: Promise<{ luat: XhLuatTen, userIds: number[] }> } | null = null

export function xhXoaDem() {
  demBang.clear()
  demLoaiTru = null
  xoaDemCms()
}

// ------------------------------------------------------------------ đọc cau_hinh của game (không tạo bảng)

async function cauHinhGame(khoas: string[]): Promise<Record<string, string>> {
  const ra: Record<string, string> = {}
  try {
    if (!(await coBang(dbGame(), 'cau_hinh'))) return ra
    const rows = await q<{ khoa: string, gia_tri: string }>(dbGame(),
      `SELECT khoa, gia_tri FROM cau_hinh WHERE khoa IN (${khoas.map(() => '?').join(',')})`, khoas)
    for (const r of rows) ra[String(r.khoa)] = String(r.gia_tri ?? '')
  }
  catch { /* thiếu cau_hinh: coi như chưa cấu hình */ }
  return ra
}

/** GM trong game (sv.adminAccounts — game so với TÊN NHÂN VẬT, viết thường). */
export async function xhGmGame(): Promise<string[]> {
  const v = (await cauHinhGame(['sv.adminAccounts']))['sv.adminAccounts'] ?? ''
  return v.split(',').map(s => s.trim().toLowerCase()).filter(Boolean)
}

/** Mùa PvP hiện tại theo đúng công thức game; null = không biết ngày khai trương. */
export async function xhMuaHienTai(): Promise<number | null> {
  const ch = await cauHinhGame(['sv.ngay_khai_truong', 'lau_dai.ngay_khai_truong', 'sv.tua_nhanh_ngay', 'sv.cho_phep_thu_nghiem'])
  const kt = xhDocNgay(ch['sv.ngay_khai_truong']) || xhDocNgay(ch['lau_dai.ngay_khai_truong'])
  if (!kt) return null
  // CongTac.tuaNhanhNgay chỉ có tác dụng khi sv.cho_phep_thu_nghiem = 1.
  const tua = ch['sv.cho_phep_thu_nghiem']?.trim() === '1' ? Math.max(0, Math.trunc(Number(ch['sv.tua_nhanh_ngay']) || 0)) : 0
  return xhMuaPvp(kt, Date.now(), tua)
}

// ------------------------------------------------------------------ loại trừ

const MAX_ID_LOAI_TRU = 5000

async function tinhLoaiTru(cd: XhCaiDat): Promise<{ luat: XhLuatTen, userIds: number[] }> {
  const ten = [...cd.loaiTru.tenNhanVat]
  if (cd.loaiTru.anGm) ten.push(...await xhGmGame())
  const luat = xhLuatTen(ten)
  const ids = new Set<number>()
  const tk = xhLuatTen(cd.loaiTru.taiKhoan)
  if (cd.loaiTru.anTaiKhoanKhoa || tk.chinhXac.length || tk.tienTo.length) {
    try {
      const acc = dbAccount()
      const them = (rows: Array<{ id: number }>) => { for (const r of rows) ids.add(Math.trunc(Number(r.id))) }
      if (cd.loaiTru.anTaiKhoanKhoa) {
        them(await q<{ id: number }>(acc, `SELECT id FROM team_user WHERE ban = 1 LIMIT ${MAX_ID_LOAI_TRU}`))
      }
      if (tk.chinhXac.length) {
        them(await q<{ id: number }>(acc, `SELECT id FROM team_user WHERE LOWER(username) IN (${tk.chinhXac.map(() => '?').join(',')}) LIMIT ${MAX_ID_LOAI_TRU}`, tk.chinhXac))
      }
      for (const t of tk.tienTo) {
        if (!/^[a-z0-9_]{1,30}$/.test(t)) continue
        them(await q<{ id: number }>(acc, `SELECT id FROM team_user WHERE SUBSTR(LOWER(username), 1, ${t.length}) = ? LIMIT ${MAX_ID_LOAI_TRU}`, [t]))
      }
    }
    catch (e: any) {
      console.error('[xep-hang] khong doc duoc team_user de loai tru:', e?.code ?? e?.message ?? e)
    }
  }
  await napTenHlvAi()
  return { luat, userIds: [...ids].slice(0, MAX_ID_LOAI_TRU * 3) }
}

/** Tên (chữ thường) nhân vật của Huấn luyện viên AI (account.team_user.la_bot = 1) - bảng xếp hạng hiện "bot_ai: tên". */
let tenHlvAi = new Set<string>()

async function napTenHlvAi(): Promise<void> {
  try {
    const ids = (await q<{ id: number }>(dbAccount(), `SELECT id FROM team_user WHERE la_bot = 1 LIMIT ${MAX_ID_LOAI_TRU}`))
      .map(r => Math.trunc(Number(r.id))).filter(n => Number.isFinite(n))
    const moi = new Set<string>()
    if (ids.length) {
      const rows = await q<{ charname: string }>(dbGame(), `SELECT charname FROM tob_char WHERE userid IN (${ids.join(',')})`)
      for (const r of rows) moi.add(String(r.charname).toLowerCase())
    }
    tenHlvAi = moi
  }
  catch {
    // CSDL chưa có cột la_bot (chưa nâng cấp): không có bot
  }
}

/** Điều kiện loại trừ trên tob_char bí danh `a` (đệm theo chu kỳ làm mới). */
async function loaiTru(cd: XhCaiDat): Promise<{ sql: string, params: unknown[] }> {
  if (!demLoaiTru || Date.now() - demLoaiTru.luc > cd.lamMoiGiay * 1000) {
    demLoaiTru = { luc: Date.now(), v: tinhLoaiTru(cd) }
  }
  const { luat, userIds } = await demLoaiTru.v
  return xhSqlLoaiTru('a', luat, userIds)
}

/** Danh sách nhân vật đang bị ẩn (cho trang admin xem trước), tối đa 500. */
export async function xhDanhSachBiAn(cd: XhCaiDat): Promise<{ ten: string, lyDo: string }[]> {
  const { luat, userIds } = await tinhLoaiTru(cd)
  const ex = xhSqlLoaiTru('a', luat, userIds)
  const rows = await q<{ charname: string, ban: number }>(dbGame(),
    `SELECT a.charname, a.ban FROM tob_char a WHERE NOT (${ex.sql}) ORDER BY a.xp DESC LIMIT 500`, ex.params)
  return rows.map((r) => {
    const ten = String(r.charname)
    const lyDo = Number(r.ban) !== 0
      ? 'Pokémon bị khoá (tob_char.ban)'
      : luat.chinhXac.includes(ten.toLowerCase()) ? 'Trùng tên trong danh sách / GM'
        : luat.tienTo.some(t => ten.toLowerCase().startsWith(t)) ? 'Khớp tiền tố tên'
          : 'Tài khoản bị khoá hoặc nằm trong danh sách tài khoản'
    return { ten, lyDo }
  })
}

// ------------------------------------------------------------------ tiện ích

const COT_NV = 'a.id, a.charname, a.class, a.lastLv, a.dex, a.shiny, a.infotop'

function soNguyen(v: unknown): number {
  const n = Math.trunc(Number(v))
  return Number.isFinite(n) ? n : 0
}

const dinhDang = (n: number) => n.toLocaleString('vi-VN')

/** Một dòng nhân vật -> dòng công khai (chỉ tên + loài + vùng). */
function dongNv(r: Record<string, any>, giaTri: number | null, phu = '', vung?: number): Omit<XhDong, 'hang'> {
  const lv = soNguyen(r.lastLv)
  const cls = soNguyen(r.class)
  return {
    ten: tenHienThiHlvAi(String(r.charname ?? ''), tenHlvAi.has(String(r.charname ?? '').toLowerCase())),
    loai: speciesName(cls, lv, r.dex),
    dex: dexHienTai(cls, lv, r.dex),
    shiny: soNguyen(r.shiny) > 0,
    vung: vung ?? xhVungTuInfotop(r.infotop),
    giaTri,
    phu,
  }
}

function chuoiGio(v: unknown): string {
  if (v instanceof Date) return Number.isNaN(v.getTime()) ? '' : xhGioChuoi(v)
  return v == null ? '' : String(v)
}

/** Lấy thông tin nhân vật theo danh sách tên, đã áp loại trừ. Khoá Map = tên viết thường. */
async function nhanVatTheoTen(ten: string[], ex: { sql: string, params: unknown[] }): Promise<Map<string, Record<string, any>>> {
  const ra = new Map<string, Record<string, any>>()
  const ds = [...new Set(ten.filter(Boolean))]
  for (let i = 0; i < ds.length; i += 500) {
    const phan = ds.slice(i, i + 500)
    const rows = await q(dbGame(),
      `SELECT ${COT_NV} FROM tob_char a WHERE a.charname IN (${phan.map(() => '?').join(',')}) AND ${ex.sql} LIMIT ${phan.length * 3}`,
      [...phan, ...ex.params])
    for (const r of rows) if (!ra.has(String(r.charname).toLowerCase())) ra.set(String(r.charname).toLowerCase(), r)
  }
  return ra
}

async function coDuBang(ds: string[]): Promise<boolean> {
  for (const b of ds) if (!(await coBang(dbGame(), b))) return false
  return true
}

/** Sự kiện "Bảng top: ẩn xu/lượng" (SuKien.AN_XU_LUONG_TOP) có đang bật không. */
async function anXuLuongTop(): Promise<boolean> {
  try {
    if (!(await coBang(dbGame(), 'tob_su_kien'))) return false
    return soNguyen(await qv(dbGame(), 'SELECT dang_bat FROM tob_su_kien WHERE ma = ? LIMIT 1', ['anxuluongtop'])) === 1
  }
  catch { return false }
}

/** Hệ sinh thái bật? Bảng tob_hst_nhan_vat phải có và cau_hinh sv.he_sinh_thai không phải '0'. */
async function sinhThaiBat(): Promise<boolean> {
  if (!(await coBang(dbGame(), 'tob_hst_nhan_vat'))) return false
  const v = (await cauHinhGame(['sv.he_sinh_thai']))['sv.he_sinh_thai']
  return v === undefined || v.trim() !== '0'
}

// ------------------------------------------------------------------ danh sách bảng công khai

/** Bảng có dùng được không (đủ bảng CSDL + cờ tính năng), bỏ qua cài đặt hiện/ẩn. */
export async function xhBangKhaDung(b: XhBangDinhNghia): Promise<boolean> {
  try {
    if (b.ma === 'sinh-thai') return await sinhThaiBat()
    return await coDuBang(b.bang)
  }
  catch { return false }
}

/** Các mùa PvP để chọn: mùa hiện tại + các mùa đã có dữ liệu (mới nhất trước, tối đa 20). */
export async function xhLuaMua(): Promise<{ lua: XhLuaChon[], macDinh: string }> {
  const hienTai = await xhMuaHienTai()
  const ds = new Set<number>()
  if (hienTai != null) ds.add(hienTai)
  try {
    const rows = await q<{ mua: number }>(dbGame(), 'SELECT DISTINCT mua FROM tob_pvp_mua ORDER BY mua DESC LIMIT 20')
    for (const r of rows) ds.add(soNguyen(r.mua))
  }
  catch { /* chưa có bảng */ }
  const sx = [...ds].sort((a, b) => b - a)
  const macDinh = String(hienTai ?? sx[0] ?? 0)
  if (!sx.length) sx.push(0)
  return {
    lua: sx.map(m => ({ gt: String(m), nhan: `Mùa ${m + 1}${m === hienTai ? ' (hiện tại)' : ''}` })),
    macDinh,
  }
}

export async function xhDanhSachCongKhai(): Promise<{ bang: XhBangCongKhai[], moiTrang: number, lamMoiGiay: number }> {
  const cd = await xhDocCaiDat()
  return quaDem('xep-hang-ds', Math.min(cd.lamMoiGiay, 60), async () => {
    const napBat = !!useHtbbConfig().nap.bat
    const ra: XhBangCongKhai[] = []
    for (const m of cd.bang) {
      if (!xhBangDuocHien(cd, m.ma, napBat)) continue
      const b = xhBang(m.ma)!
      if (!(await xhBangKhaDung(b))) continue
      const boLoc = []
      for (const l of b.boLoc) {
        if (l.khoa === 'mua') {
          const { lua, macDinh } = await xhLuaMua()
          boLoc.push({ khoa: l.khoa, nhan: l.nhan, lua, macDinh })
        }
        else boLoc.push({ khoa: l.khoa, nhan: l.nhan, lua: [...l.lua], macDinh: l.macDinh })
      }
      ra.push({ ma: b.ma, ten: b.ten, icon: b.icon, nhom: b.nhom, moTa: b.moTa, trongGame: b.trongGame, boLoc })
    }
    return { bang: ra, moiTrang: cd.moiTrang, lamMoiGiay: cd.lamMoiGiay }
  })
}

// ------------------------------------------------------------------ đọc từng bảng

interface Tho { dong: Array<Omit<XhDong, 'hang'>>, cot: XhKetQua['cot'], ghiChu?: string, laDoi?: boolean }

const COT_PKM = 'Pokémon'

async function docBang(ma: XhMaBang, loc: Record<string, string>, cd: XhCaiDat): Promise<Tho> {
  const b = xhBang(ma)!
  const n = Math.max(1, b.gioiHanGame ? Math.min(cd.soDong, b.gioiHanGame) : cd.soDong)
  const g = dbGame()
  const ex = await loaiTru(cd)
  const vungSo = (v: string) => ['kanto', 'johto', 'hoenn'].indexOf(v)

  switch (ma) {
    case 'suc-manh': {
      if (loc.vung && loc.vung !== 'tat-ca') {
        // Database.getTopCountry(country, 0): tob_char_luong ORDER BY exp DESC LIMIT 50.
        const v = vungSo(loc.vung)
        const rows = await q(g, `SELECT ${COT_NV}, t.lv, t.exp FROM tob_char_luong t JOIN tob_char a ON a.charname = t.charname
          WHERE t.country = ? AND ${ex.sql} ORDER BY t.exp DESC, t.id ASC LIMIT ${Math.min(n, 50)}`, [v, ...ex.params])
        return { cot: { ten: COT_PKM, giaTri: 'Cấp', phu: 'Kinh nghiệm' }, dong: rows.map(r => dongNv(r, soNguyen(r.lv), `${dinhDang(soNguyen(r.exp))} EXP`, v)),
          ghiChu: `Top sức mạnh ${XH_VUNG[v]} — cùng dữ liệu với game (cập nhật khi Pokémon thoát game).` }
      }
      // DatabaseXepHang.getTopLV: tob_char WHERE ban=0 [GM] ORDER BY xp DESC LIMIT 100.
      const rows = await q(g, `SELECT ${COT_NV}, a.xp FROM tob_char a WHERE ${ex.sql} ORDER BY a.xp DESC, a.id ASC LIMIT ${n}`, ex.params)
      return { cot: { ten: COT_PKM, giaTri: 'Cấp', phu: 'Kinh nghiệm' }, dong: rows.map(r => dongNv(r, soNguyen(r.lastLv), `${dinhDang(soNguyen(r.xp))} EXP`)) }
    }
    case 'dai-gia': {
      const an = await anXuLuongTop()
      const rows = await q(g, `SELECT ${COT_NV}, a.gold FROM tob_char a WHERE ${ex.sql} ORDER BY a.gold DESC, a.id ASC LIMIT ${n}`, ex.params)
      return { cot: { ten: COT_PKM, giaTri: 'Xu', phu: '' }, dong: rows.map(r => dongNv(r, an ? null : soNguyen(r.gold), `Cấp ${soNguyen(r.lastLv)}`)),
        ghiChu: an ? 'Sự kiện đang ẩn số xu ở bảng top (như trong game).' : undefined }
    }
    case 'cong-trang':
    case 'chuoi-thang': {
      const v = Math.max(0, vungSo(loc.vung ?? 'kanto'))
      const cot = ma === 'cong-trang' ? 'honor' : 'nPK'
      const rows = await q(g, `SELECT ${COT_NV}, t.lv, t.${cot} AS diem FROM tob_char_luong t JOIN tob_char a ON a.charname = t.charname
        WHERE t.country = ? AND t.${cot} > 0 AND ${ex.sql} ORDER BY t.${cot} DESC, t.id ASC LIMIT ${n}`, [v, ...ex.params])
      return { cot: { ten: COT_PKM, giaTri: ma === 'cong-trang' ? 'Công trạng' : 'Chuỗi thắng', phu: '' },
        dong: rows.map(r => dongNv(r, soNguyen(r.diem), `Cấp ${soNguyen(r.lv)}`, v)),
        ghiChu: `${b.ten} ${XH_VUNG[v]} — cùng dữ liệu với game (cập nhật khi Pokémon thoát game).` }
    }
    case 'doi': {
      // Database.getTopClan: tob_clan ORDER BY xp DESC LIMIT 100.
      const rows = await q(g, `SELECT id_clan, clanName, charMaster, lvClan, country FROM tob_clan ORDER BY xp DESC, id_clan ASC LIMIT ${n}`)
      const ids = rows.map(r => soNguyen(r.id_clan))
      const tv = new Map<number, number>()
      if (ids.length) {
        for (const r of await q(g, `SELECT idClan, COUNT(*) AS so FROM tob_char WHERE idClan IN (${ids.join(',')}) GROUP BY idClan`)) {
          tv.set(soNguyen(r.idClan), soNguyen(r.so))
        }
      }
      return { laDoi: true, cot: { ten: 'Đội', giaTri: 'Cấp đội', phu: 'Thành viên' },
        dong: rows.map(r => ({ ten: String(r.clanName ?? ''), loai: '', dex: 0, shiny: false, vung: [0, 1, 2].includes(soNguyen(r.country)) ? soNguyen(r.country) : -1,
          giaTri: soNguyen(r.lvClan), phu: `${tv.get(soNguyen(r.id_clan)) ?? 0} thành viên${r.charMaster ? ` · Đội trưởng ${String(r.charMaster)}` : ''}` })) }
    }
    case 'chiem-gym': {
      const tu = loc.ky === 'tat-ca' ? '2000-01-01 00:00:00' : loc.ky === 'tuan' ? xhGioChuoi(xhDauTuanGame(new Date())) : cd.mocChiemGym
      const ghiChu = loc.ky === 'tat-ca' ? 'Tính mọi thời điểm.' : `Tính từ ${tu.slice(11, 16)} ngày ${tu.slice(8, 10)}/${tu.slice(5, 7)}/${tu.slice(0, 4)}.`
      if (loc.kieu === 'dong-gop') {
        if (!(await coBang(g, 'tob_chiem_thanh_tuan'))) return { cot: { ten: COT_PKM, giaTri: 'Điểm', phu: '' }, dong: [], ghiChu }
        const rows = await q(g, `SELECT ${COT_NV}, d.diem, d.so FROM (SELECT id_char, SUM(diem) AS diem, COUNT(*) AS so FROM tob_chiem_thanh_tuan
          WHERE luc >= ? GROUP BY id_char) d JOIN tob_char a ON a.id = d.id_char WHERE d.diem > 0 AND ${ex.sql}
          ORDER BY d.diem DESC, d.so DESC, a.id ASC LIMIT ${n}`, [tu, ...ex.params])
        return { cot: { ten: COT_PKM, giaTri: 'Điểm đóng góp', phu: '' }, dong: rows.map(r => dongNv(r, soNguyen(r.diem), `${soNguyen(r.so)} trận chiếm Gym`)), ghiChu }
      }
      const hanhDong = loc.kieu === 'doi' ? 'cgt' : loc.kieu === 'pha-the' ? 'pcok' : 'gcok'
      const tho = await q(g, `SELECT charname AS ten, COUNT(*) AS so, MAX(timeLog) AS cuoi, MAX(id) AS idCuoi FROM tob_log_clan
        WHERE actioninfo = ? AND timeLog >= ? GROUP BY charname ORDER BY so DESC LIMIT 5000`, [hanhDong, tu])
      const gop = xhGopDem(tho.map(r => ({ ten: String(r.ten ?? ''), so: soNguyen(r.so), cuoi: chuoiGio(r.cuoi), idCuoi: soNguyen(r.idCuoi) }) as XhDongDem),
        hanhDong === 'gcok' ? xhTenGiaoThe : s => s)
      if (hanhDong === 'cgt') {
        const ten = gop.slice(0, n).map(x => x.ten)
        const doi = new Map<string, Record<string, any>>()
        if (ten.length) {
          for (const r of await q(g, `SELECT clanName, country FROM tob_clan WHERE clanName IN (${ten.map(() => '?').join(',')}) LIMIT ${ten.length * 2}`, ten)) {
            doi.set(String(r.clanName).toLowerCase(), r)
          }
        }
        return { laDoi: true, cot: { ten: 'Đội', giaTri: 'Lần chiếm Gym', phu: '' }, ghiChu,
          dong: gop.slice(0, n).map((x) => {
            const d = doi.get(x.ten.toLowerCase())
            const v = soNguyen(d?.country ?? -1)
            return { ten: x.ten, loai: '', dex: 0, shiny: false, vung: [0, 1, 2].includes(v) ? v : -1, giaTri: x.so, phu: '' }
          }) }
      }
      const nv = await nhanVatTheoTen(gop.slice(0, n * 3 + 50).map(x => x.ten), ex)
      const dong: Array<Omit<XhDong, 'hang'>> = []
      for (const x of gop) {
        const r = nv.get(x.ten.toLowerCase())
        if (!r) continue
        dong.push(dongNv(r, x.so))
        if (dong.length >= n) break
      }
      return { cot: { ten: COT_PKM, giaTri: 'Số lần', phu: '' }, dong, ghiChu }
    }
    case 'pokedex': {
      if (loc.kieu === 'shiny') {
        if (!(await coBang(g, 'tob_pokedex_shiny'))) return { cot: { ten: COT_PKM, giaTri: 'Loài shiny', phu: '' }, dong: [] }
        const rows = await q(g, `SELECT ${COT_NV}, d.so, d.tong FROM (SELECT id_char, COUNT(*) AS so, SUM(so_shiny) AS tong FROM tob_pokedex_shiny
          WHERE so_shiny > 0 GROUP BY id_char) d JOIN tob_char a ON a.id = d.id_char WHERE ${ex.sql}
          ORDER BY d.so DESC, d.tong DESC, a.id ASC LIMIT ${n}`, ex.params)
        return { cot: { ten: COT_PKM, giaTri: 'Loài shiny', phu: '' }, dong: rows.map(r => dongNv(r, soNguyen(r.so), `${dinhDang(soNguyen(r.tong))} lần gặp shiny`)) }
      }
      const daHa = loc.kieu !== 'da-gap'
      const rows = await q(g, `SELECT ${COT_NV}, d.so, d.ha FROM (SELECT id_char, COUNT(*) AS so, SUM(so_ha) AS ha, MAX(lan_dau) AS moc FROM tob_pokedex
        ${daHa ? 'WHERE so_ha > 0' : ''} GROUP BY id_char) d JOIN tob_char a ON a.id = d.id_char WHERE ${ex.sql}
        ORDER BY d.so DESC, d.moc ASC, a.id ASC LIMIT ${n}`, ex.params)
      return { cot: { ten: COT_PKM, giaTri: daHa ? 'Loài đã hạ' : 'Loài đã gặp', phu: '' },
        dong: rows.map(r => dongNv(r, soNguyen(r.so), `${dinhDang(soNguyen(r.ha))} lượt hạ`)) }
    }
    case 'pvp': {
      const mua = Math.max(0, soNguyen(loc.mua))
      const rows = await q(g, `SELECT ${COT_NV}, p.elo, p.thang, p.thua FROM tob_pvp_mua p JOIN tob_char a ON a.id = p.id_char
        WHERE p.mua = ? AND (p.thang + p.thua) > 0 AND ${ex.sql} ORDER BY p.elo DESC, p.thang DESC, p.thua ASC, a.id ASC LIMIT ${n}`, [mua, ...ex.params])
      return { cot: { ten: COT_PKM, giaTri: 'Elo', phu: 'Thắng – thua' }, ghiChu: `Mùa ${mua + 1}. Chỉ tính Pokémon đã đấu ít nhất 1 trận trong mùa.`,
        dong: rows.map((r) => {
          const elo = soNguyen(r.elo)
          return { ...dongNv(r, elo, `${soNguyen(r.thang)} thắng – ${soNguyen(r.thua)} thua`), bac: XH_TEN_BAC_PVP[xhBacPvp(elo)] }
        }) }
    }
    case 'gym': {
      const rows = await q(g, `SELECT ${COT_NV}, a.xp, t.du_lieu FROM tob_char_lau_dai t JOIN tob_char a ON a.id = t.id_char
        WHERE t.du_lieu LIKE '%gh=%' AND ${ex.sql} LIMIT 20000`, ex.params)
      const ds = rows.map(r => ({ r, so: xhSoHuyHieu(r.du_lieu), xp: soNguyen(r.xp), id: soNguyen(r.id) })).filter(x => x.so > 0)
      ds.sort((x, y) => y.so - x.so || y.xp - x.xp || x.id - y.id)
      return { cot: { ten: COT_PKM, giaTri: 'Huy hiệu', phu: '' }, dong: ds.slice(0, n).map(x => dongNv(x.r, x.so, `Cấp ${soNguyen(x.r.lastLv)}`)),
        ghiChu: 'Bằng số huy hiệu thì Pokémon nhiều kinh nghiệm hơn đứng trước.' }
    }
    case 'ham-nguc': {
      const hom = new Date()
      if (loc.ngay === 'hom-qua') hom.setDate(hom.getDate() - 1)
      const ngay = xhNgaySo(hom)
      const rows = await q(g, `SELECT ${COT_NV}, h.tang FROM tob_ham_nguc h JOIN tob_char a ON a.id = h.id_char
        WHERE h.ngay = ? AND h.tang > 0 AND ${ex.sql}
        ORDER BY h.tang DESC, CASE WHEN h.ghi_luc IS NULL THEN 1 ELSE 0 END, h.ghi_luc ASC, a.id ASC LIMIT ${n}`, [ngay, ...ex.params])
      return { cot: { ten: COT_PKM, giaTri: 'Tầng', phu: '' }, dong: rows.map(r => dongNv(r, soNguyen(r.tang), `Cấp ${soNguyen(r.lastLv)}`)),
        ghiChu: `Hầm ngày ${String(ngay % 100).padStart(2, '0')}/${String(Math.trunc(ngay / 100) % 100).padStart(2, '0')}/${Math.trunc(ngay / 10000)} — hầm mới mỗi 00:00.` }
    }
    case 'sinh-thai': {
      const rows = await q(g, `SELECT ${COT_NV}, t.du_lieu, t.cap_nhat FROM tob_hst_nhan_vat t JOIN tob_char a ON a.id = t.id_char
        WHERE ${ex.sql} LIMIT 20000`, ex.params)
      const iKn = XH_KY_NANG.indexOf((loc.kieu ?? 'cau-ca') as typeof XH_KY_NANG[number])
      const ds = rows.map((r) => {
        const st = xhDocSinhThai(r.du_lieu)
        const exp = iKn >= 0 ? st.exp[iKn]! : st.album
        return { r, exp, cap: iKn >= 0 ? xhCapKyNang(exp) : st.album, luc: soNguyen(r.cap_nhat), id: soNguyen(r.id) }
      }).filter(x => x.exp > 0)
      ds.sort((x, y) => y.exp - x.exp || x.luc - y.luc || x.id - y.id)
      const tenKn = xhBang('sinh-thai')!.boLoc[0]!.lua.find(l => l.gt === loc.kieu)?.nhan ?? ''
      return { cot: { ten: COT_PKM, giaTri: iKn >= 0 ? `Cấp ${tenKn.toLowerCase()}` : 'Ảnh album', phu: '' },
        dong: ds.slice(0, n).map(x => dongNv(x.r, x.cap, iKn >= 0 ? `${dinhDang(x.exp)} EXP` : '')) }
    }
    case 'thanh-tuu': {
      const tong = soNguyen(await qv(g, `SELECT COUNT(*) FROM data_quest_lau_dai WHERE kieu_muc_tieu LIKE 'thanh_tuu%'`))
      const rows = await q(g, `SELECT ${COT_NV}, d.so FROM (SELECT id_char, COUNT(*) AS so FROM tob_nhiem_vu_lau_dai
        WHERE trang_thai >= 1 AND ma IN (SELECT ma FROM data_quest_lau_dai WHERE kieu_muc_tieu LIKE 'thanh_tuu%') GROUP BY id_char) d
        JOIN tob_char a ON a.id = d.id_char WHERE ${ex.sql} ORDER BY d.so DESC, a.xp DESC, a.id ASC LIMIT ${n}`, ex.params)
      return { cot: { ten: COT_PKM, giaTri: 'Thành tựu', phu: '' }, dong: rows.map(r => dongNv(r, soNguyen(r.so), tong ? `trên ${tong} thành tựu` : '')) }
    }
    case 'su-kien': {
      if (loc.kieu === 'tha-den') {
        if (!(await coBang(g, 'tob_canmoc_thadeon_canhan'))) return { cot: { ten: COT_PKM, giaTri: 'Lần thả đèn', phu: '' }, dong: [] }
        const rows = await q(g, `SELECT ${COT_NV}, t.so_lan FROM tob_canmoc_thadeon_canhan t JOIN tob_char a ON a.id = t.idchar
          WHERE t.so_lan > 0 AND ${ex.sql} ORDER BY t.so_lan DESC, t.cap_nhat_luc ASC, a.id ASC LIMIT ${n}`, ex.params)
        return { cot: { ten: COT_PKM, giaTri: 'Lần thả đèn', phu: '' }, dong: rows.map(r => dongNv(r, soNguyen(r.so_lan), `Cấp ${soNguyen(r.lastLv)}`)),
          ghiChu: 'Top 10 thả đèn Trung Thu — như bảng xếp hạng ở NPC sự kiện.' }
      }
      const rows = await q(g, `SELECT ${COT_NV}, t.point1 FROM tob_top_event t JOIN tob_char a ON a.charname = t.charname
        WHERE t.point1 > 0 AND ${ex.sql} ORDER BY t.point1 DESC, t.id ASC LIMIT ${n}`, ex.params)
      return { cot: { ten: COT_PKM, giaTri: 'Điểm', phu: '' }, dong: rows.map(r => dongNv(r, soNguyen(r.point1), `Cấp ${soNguyen(r.lastLv)}`)),
        ghiChu: 'Top 10 điểm sự kiện — như NPC sự kiện trong game.' }
    }
  }
}

/**
 * Một bảng đã lọc, có đệm `lamMoiGiay` giây (mặc định 60 — bằng chu kỳ game làm mới top).
 * null = bảng không tồn tại / đang ẩn / thiếu bảng CSDL.
 */
/**
 * 0.9.7: gắn danh hiệu đang đeo (tên + độ hiếm từ data_danh_hieu) cho các dòng nhân vật. Không có bảng
 * data_danh_hieu (chưa chạy sql/them_danh_hieu.sql) hoặc lỗi -> bỏ qua, bảng vẫn hiện bình thường.
 */
async function xhGanDanhHieu(dong: Omit<XhDong, 'hang'>[]): Promise<void> {
  try {
    const g = dbGame()
    const ten = [...new Set(dong.map(d => d.ten).filter(Boolean))].slice(0, 200)
    if (!ten.length || !(await coBang(g, 'data_danh_hieu'))) return
    const nv = await q<{ charname: string, pInfo: string }>(g,
      `SELECT charname, pInfo FROM tob_char WHERE charname IN (${ten.map(() => '?').join(',')}) LIMIT ${ten.length * 2}`, ten)
    const idTheoTen = new Map<string, number>()
    for (const r of nv) {
      const id = xhIdDanhHieu(r.pInfo)
      if (id > 0) idTheoTen.set(String(r.charname).toLowerCase(), id)
    }
    const ids = [...new Set(idTheoTen.values())]
    if (!ids.length) return
    const dh = new Map<number, { ten: string, doHiem: number }>()
    for (const r of await q<{ id: number, ten: string, do_hiem: number }>(g,
      `SELECT id, ten, do_hiem FROM data_danh_hieu WHERE bat = 1 AND id IN (${ids.map(() => '?').join(',')}) LIMIT ${ids.length}`, ids)) {
      dh.set(soNguyen(r.id), { ten: String(r.ten ?? ''), doHiem: soNguyen(r.do_hiem) })
    }
    for (const d of dong) {
      const id = idTheoTen.get(d.ten.toLowerCase())
      const x = id ? dh.get(id) : undefined
      if (x?.ten) d.danhHieu = x
    }
  }
  catch (e: any) {
    console.error('[xep-hang] danh hieu:', e?.code ?? '', e?.message ?? e)
  }
}

export async function xhLayBang(ma: string, query: Record<string, unknown>): Promise<XhKetQua | null> {
  const b = xhBang(ma)
  if (!b) return null
  const cd = await xhDocCaiDat()
  if (!xhBangDuocHien(cd, b.ma, !!useHtbbConfig().nap.bat)) return null
  if (!(await xhBangKhaDung(b))) return null
  const loc = xhChonBoLoc(b, query)
  if ('mua' in loc && loc.mua === '') loc.mua = (await xhLuaMua()).macDinh
  const khoa = `${b.ma}|${JSON.stringify(loc)}`
  const c = demBang.get(khoa)
  if (c?.kq && Date.now() - c.luc < (c.kq.loi ? 10 : cd.lamMoiGiay) * 1000) return c.kq
  if (c?.dang) return c.dang
  if (demBang.size > 300) demBang.clear()
  const dang = (async (): Promise<XhKetQua> => {
    const capNhatLuc = new Date().toISOString()
    try {
      const t = await docBang(b.ma, loc, cd)
      if (!t.laDoi) await xhGanDanhHieu(t.dong)
      return { ma: b.ma, ten: b.ten, laDoi: !!t.laDoi, cot: t.cot, loc, ghiChu: t.ghiChu ?? '', capNhatLuc, loi: false,
        dong: t.dong.map((d, i) => ({ hang: i + 1, ...d })) }
    }
    catch (e: any) {
      console.error(`[xep-hang] ${b.ma}:`, e?.code ?? '', e?.message ?? e)
      return { ma: b.ma, ten: b.ten, laDoi: false, cot: { ten: COT_PKM, giaTri: '', phu: '' }, loc, ghiChu: '', capNhatLuc, loi: true, dong: [] }
    }
  })()
  demBang.set(khoa, { luc: c?.luc ?? 0, kq: c?.kq, dang })
  const kq = await dang
  demBang.set(khoa, { luc: Date.now(), kq })
  return kq
}
