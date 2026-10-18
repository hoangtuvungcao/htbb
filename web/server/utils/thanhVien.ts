/**
 * THÀNH VIÊN HTBB — phần chạm database (bảng tv_thanh_vien, tv_nhat_ky của HTBB/sql/pokemon_thanh_vien.sql
 * + cau_hinh thanh_vien.* + nap_goi THANH_VIEN). Thiết kế: docs/van-hanh.md.
 *
 * Thành viên theo TÀI KHOẢN. Cấp / gia hạn / thu hồi CHỈ qua tvCap / tvThuHoi (mua, hoàn tiền, admin,
 * hàng loạt) — mỗi lần một dòng tv_nhat_ky. Server game đọc tv_thanh_vien (nhớ 30 giây) và cau_hinh
 * (đọc lại 10 giây) — không cần khởi động lại.
 */
import type { PoolConnection } from 'mysql2/promise'
import {
  TV_CHUC_NANG, TV_K_BAN_BAT, TV_K_BAT, TV_K_CHUC_NANG, TV_K_GIA, TV_K_MIEN_CAP, TV_K_THOI_HAN, TV_K_THONG_BAO, TV_MA_GOI,
  conHanTv, docCauHinhTv, hanMoiTv, hanSauThuHoiTv, type CauHinhTv,
} from './thanhVienThuan'

export async function tvCoBang(): Promise<boolean> {
  return (await coBang(dbGame(), 'tv_thanh_vien')) && (await coBang(dbGame(), 'tv_nhat_ky'))
}

export function tvThieuBang(): never {
  throw apiLoi(409, 'Chưa có bảng thành viên — chạy HTBB/sql/pokemon_nap_tien.sql rồi HTBB/sql/pokemon_thanh_vien.sql.', { code: 'thieu_bang' })
}

// ---------------------------------------------------------------- cấu hình (cau_hinh, nhớ 5 giây)

let nho: { c: CauHinhTv, luc: number } | null = null

export async function tvCauHinh(moi = false): Promise<CauHinhTv> {
  if (!moi && nho && Date.now() - nho.luc < 5000) return nho.c
  const kv: Record<string, string> = {}
  try {
    for (const r of await q<{ khoa: string, gia_tri: string }>(dbGame(), "SELECT khoa, gia_tri FROM cau_hinh WHERE khoa LIKE 'thanh\\_vien.%' OR khoa LIKE 'kich\\_hoat.%'")) {
      kv[r.khoa] = String(r.gia_tri)
    }
  } catch { /* chưa có bảng: coi như tắt */ }
  const c = docCauHinhTv(kv)
  nho = { c, luc: Date.now() }
  return c
}

/** Bán thành viên được không: công tắc riêng + giấy phép + cổng thanh toán an toàn (fail closed). */
export async function tvBanDuoc(): Promise<{ duoc: boolean, lyDo: string[] }> {
  const n = useHtbbConfig().nap
  const c = await tvCauHinh()
  const lyDo: string[] = []
  if (!c.banBat) lyDo.push('thanh_vien.ban_bat = 0 (đang tắt bán)')
  if (!n.daCoGiayPhep) lyDo.push('chưa đặt NAP_DA_CO_GIAY_PHEP=1 (giấy phép hình ảnh/IP)')
  if (!n.congAnToan) lyDo.push(...n.loiCauHinh)
  if (!(await napCoBang()) || !(await tvCoBang())) lyDo.push('chưa có bảng nạp / thành viên')
  return { duoc: lyDo.length === 0, lyDo }
}

export async function tvLuuCauHinh(admin: string, c: CauHinhTv): Promise<void> {
  const ghi: [string, string][] = [
    [TV_K_BAT, c.bat ? '1' : '0'], [TV_K_BAN_BAT, c.banBat ? '1' : '0'], [TV_K_GIA, String(c.gia)],
    [TV_K_THOI_HAN, String(c.thoiHanNgay)], [TV_K_CHUC_NANG, c.chucNang.join(',')], [TV_K_MIEN_CAP, String(c.mienCap)],
    [TV_K_THONG_BAO, c.thongBao],
    ['kich_hoat.chan_dang_nhap', c.khChanDangNhap ? '1' : '0'], ['kich_hoat.chuc_nang', c.khChucNang.join(',')],
    ['kich_hoat.thong_bao', c.khThongBao], ['kich_hoat.thanh_vien_la_kich_hoat', c.khTvLaKh ? '1' : '0'],
  ]
  const luc = gioDb()
  for (const [k, v] of ghi) {
    await exec(dbGame(), 'INSERT IGNORE INTO cau_hinh (khoa, gia_tri, sua_boi, sua_luc) VALUES (?, ?, ?, ?)', [k, v, admin.slice(0, 50), luc])
    await exec(dbGame(), 'UPDATE cau_hinh SET gia_tri = ?, sua_boi = ?, sua_luc = ? WHERE khoa = ?', [v, admin.slice(0, 50), luc, k])
  }
  // Sản phẩm trong danh mục nạp đi theo cấu hình (một nguồn: cau_hinh).
  if (await napCoBang()) {
    await exec(dbGame(), "INSERT IGNORE INTO nap_goi (ma, ten, loai, gia_vnd, luong, dang_ban) VALUES (?, 'Thành viên Huyền Thoại Bảo Bối', 'thanh_vien', ?, 0, 0)", [TV_MA_GOI, c.gia])
    await exec(dbGame(), 'UPDATE nap_goi SET gia_vnd = ?, dang_ban = ?, loai = ?, luong = 0, cap_nhat_luc = ?, admin_user = ? WHERE ma = ?',
      [c.gia, c.banBat ? 1 : 0, 'thanh_vien', luc, admin.slice(0, 50), TV_MA_GOI])
  }
  nho = null
}

// ---------------------------------------------------------------- trạng thái

export interface TrangThaiTv {
  laThanhVien: boolean
  hetHanMs: number | null
  nguon: string
  capLuc: string | null
  soLan: number
}

export async function tvTrangThai(userId: number): Promise<TrangThaiTv> {
  const r = await q1<{ het_han_ms: number, nguon: string, cap_luc: string | null, so_lan: number }>(dbGame(),
    'SELECT het_han_ms, nguon, cap_luc, so_lan FROM tv_thanh_vien WHERE user_id = ? LIMIT 1', [Math.trunc(userId)])
  if (!r) return { laThanhVien: false, hetHanMs: null, nguon: '', capLuc: null, soLan: 0 }
  const h = Number(r.het_han_ms)
  return { laThanhVien: conHanTv(h), hetHanMs: h, nguon: r.nguon, capLuc: r.cap_luc, soLan: Number(r.so_lan) }
}

// ---------------------------------------------------------------- cấp / thu hồi (HÀM DUY NHẤT)

export interface ThaoTacTv {
  userId: number
  username: string
  ngay: number
  nguon: 'mua' | 'admin' | 'hang_loat'
  tacNhan: string
  lyDo: string
  maDon?: string
  ip?: string
}

async function ghiNhatKyTv(ex: PoolConnection | null, a: ThaoTacTv, hanhDong: string, cu: number, moi: number): Promise<void> {
  await exec(ex ?? dbGame(),
    'INSERT INTO tv_nhat_ky (user_id, username, hanh_dong, ngay, het_han_cu, het_han_moi, ma_don, tac_nhan, ly_do, ip, tao_luc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
    [a.userId, a.username.slice(0, 50), hanhDong, Math.trunc(a.ngay), cu, moi, (a.maDon ?? '').slice(0, 16), a.tacNhan.slice(0, 60), a.lyDo.slice(0, 255),
      (a.ip ?? '').slice(0, 64), gioDb()])
}

/** Cấp / gia hạn (ngay = 0: vĩnh viễn). Chạy trong transaction của người gọi nếu có. */
export async function tvCap(ex: PoolConnection | null, a: ThaoTacTv): Promise<{ cu: number | null, moi: number }> {
  const db = ex ?? dbGame()
  const r = await q1<{ het_han_ms: number }>(db, 'SELECT het_han_ms FROM tv_thanh_vien WHERE user_id = ? LIMIT 1 FOR UPDATE', [a.userId])
  const bayGio = Date.now()
  const cu = r ? Number(r.het_han_ms) : null
  const moi = hanMoiTv(!!r && cu! >= 0, cu ?? -1, bayGio, a.ngay)
  if (r) {
    await exec(db, 'UPDATE tv_thanh_vien SET het_han_ms = ?, username = ?, nguon = ?, so_lan = so_lan + 1, cap_nhat_luc = ?, ghi_chu = ? WHERE user_id = ?',
      [moi, a.username.slice(0, 50), a.nguon, gioDb(), a.lyDo.slice(0, 255), a.userId])
  } else {
    await exec(db, 'INSERT INTO tv_thanh_vien (user_id, username, het_han_ms, nguon, so_lan, cap_luc, cap_nhat_luc, ghi_chu) VALUES (?, ?, ?, ?, 1, ?, ?, ?)',
      [a.userId, a.username.slice(0, 50), moi, a.nguon, gioDb(), gioDb(), a.lyDo.slice(0, 255)])
  }
  await ghiNhatKyTv(ex, a, cu !== null && conHanTv(cu, bayGio) ? 'gia_han' : 'cap', cu ?? -1, moi)
  return { cu, moi }
}

/** Thu hồi: hoanNgay = số ngày của lần cấp bị hoàn (-1 = thu hồi hẳn, admin). */
export async function tvThuHoi(ex: PoolConnection | null, a: ThaoTacTv & { hoanTien: boolean }): Promise<{ cu: number | null, moi: number }> {
  const db = ex ?? dbGame()
  const r = await q1<{ het_han_ms: number }>(db, 'SELECT het_han_ms FROM tv_thanh_vien WHERE user_id = ? LIMIT 1 FOR UPDATE', [a.userId])
  if (!r) return { cu: null, moi: -1 }
  const cu = Number(r.het_han_ms)
  const moi = a.hoanTien ? hanSauThuHoiTv(cu, Date.now(), a.ngay) : -1
  await exec(db, 'UPDATE tv_thanh_vien SET het_han_ms = ?, cap_nhat_luc = ?, ghi_chu = ? WHERE user_id = ?', [moi, gioDb(), a.lyDo.slice(0, 255), a.userId])
  await ghiNhatKyTv(ex, a, a.hoanTien ? 'hoan_tien' : 'thu_hoi', cu, moi)
  return { cu, moi }
}

// ---------------------------------------------------------------- admin: danh sách, thống kê, tín hiệu

export async function tvThongKe() {
  const bayGio = Date.now()
  const conHan = Number(await qv(dbGame(), 'SELECT COUNT(*) FROM tv_thanh_vien WHERE het_han_ms = 0 OR het_han_ms > ?', [bayGio]) ?? 0)
  const vinhVien = Number(await qv(dbGame(), 'SELECT COUNT(*) FROM tv_thanh_vien WHERE het_han_ms = 0') ?? 0)
  const sapHet = Number(await qv(dbGame(), 'SELECT COUNT(*) FROM tv_thanh_vien WHERE het_han_ms > ? AND het_han_ms <= ?', [bayGio, bayGio + 7 * 86400000]) ?? 0)
  const daThuHoi = Number(await qv(dbGame(), 'SELECT COUNT(*) FROM tv_thanh_vien WHERE het_han_ms < 0') ?? 0)
  let doanhThu = 0
  let soDon = 0
  if (await napCoBang()) {
    const r = await q1<{ so: number, tien: number }>(dbGame(),
      "SELECT COUNT(*) AS so, COALESCE(SUM(so_tien_nhan), 0) AS tien FROM nap_don WHERE loai = 'thanh_vien' AND trang_thai = 'da_cong'")
    soDon = Number(r?.so ?? 0)
    doanhThu = Number(r?.tien ?? 0)
  }
  return { conHan, vinhVien, sapHet, daThuHoi, soDon, doanhThu }
}

export async function tvDanhSach(tim: string, page: number, moiTrang = 50) {
  const w: string[] = []
  const p: unknown[] = []
  if (tim) {
    w.push('username LIKE ?')
    p.push(`${tim.toLowerCase().replace(/[\\%_]/g, c => `\\${c}`)}%`)
  }
  const ws = w.length ? `WHERE ${w.join(' AND ')}` : ''
  const tong = Number(await qv(dbGame(), `SELECT COUNT(*) FROM tv_thanh_vien ${ws}`, p) ?? 0)
  const ds = await q<{ user_id: number, username: string, het_han_ms: number, nguon: string, so_lan: number, cap_luc: string, cap_nhat_luc: string, ghi_chu: string }>(dbGame(),
    `SELECT user_id, username, het_han_ms, nguon, so_lan, cap_luc, cap_nhat_luc, ghi_chu FROM tv_thanh_vien ${ws} ORDER BY cap_nhat_luc DESC, user_id DESC
      LIMIT ${Math.trunc(moiTrang)} OFFSET ${Math.trunc((Math.max(1, page) - 1) * moiTrang)}`, p)
  return { tong, ds: ds.map(d => ({ ...d, het_han_ms: Number(d.het_han_ms), con_han: conHanTv(Number(d.het_han_ms)) })) }
}

/** Mã máy dùng chung bởi nhiều tài khoản (tín hiệu tài khoản phụ / clone). */
export async function tvTinHieuNhieuTaiKhoan(nguong = 3) {
  if (!(await coBang(dbGame(), 'tob_may_dang_nhap'))) return { coBang: false, may: [] as unknown[] }
  const may = await q<{ ma_may: string, so_tk: number, lan_cuoi: string }>(dbGame(),
    `SELECT ma_may, COUNT(DISTINCT username) AS so_tk, MAX(lan_cuoi) AS lan_cuoi FROM tob_may_dang_nhap
      GROUP BY ma_may HAVING COUNT(DISTINCT username) >= ? ORDER BY so_tk DESC, lan_cuoi DESC LIMIT 50`, [Math.max(2, Math.trunc(nguong))])
  const ra = []
  for (const m of may) {
    const tk = await q<{ username: string, ip: string, lan_cuoi: string }>(dbGame(),
      'SELECT username, ip, lan_cuoi FROM tob_may_dang_nhap WHERE ma_may = ? ORDER BY lan_cuoi DESC LIMIT 30', [m.ma_may])
    const tenDs = tk.map(t => t.username.toLowerCase())
    const tv = tenDs.length
      ? await q<{ username: string, het_han_ms: number }>(dbGame(),
        `SELECT username, het_han_ms FROM tv_thanh_vien WHERE username IN (${tenDs.map(() => '?').join(',')})`, tenDs)
      : []
    const laTv = new Set(tv.filter(x => conHanTv(Number(x.het_han_ms))).map(x => x.username.toLowerCase()))
    ra.push({ ...m, so_tk: Number(m.so_tk), soThanhVien: tenDs.filter(t => laTv.has(t)).length, taiKhoan: tk.map(t => ({ ...t, thanhVien: laTv.has(t.username.toLowerCase()) })) })
  }
  return { coBang: true, may: ra }
}

/** Liên kết thiết bị / IP của MỘT tài khoản + tài khoản khác dùng chung máy. */
export async function tvLienKet(username: string) {
  if (!(await coBang(dbGame(), 'tob_may_dang_nhap'))) return { coBang: false, may: [] as unknown[], ip: [] as unknown[] }
  const u = username.trim().toLowerCase()
  const may = await q<{ ma_may: string, ip: string, so_lan: number, lan_cuoi: string }>(dbGame(),
    'SELECT ma_may, ip, so_lan, lan_cuoi FROM tob_may_dang_nhap WHERE username = ? ORDER BY lan_cuoi DESC LIMIT 30', [u])
  const ra = []
  for (const m of may) {
    const khac = await q<{ username: string }>(dbGame(), 'SELECT DISTINCT username FROM tob_may_dang_nhap WHERE ma_may = ? AND username <> ? LIMIT 30', [m.ma_may, u])
    ra.push({ ...m, taiKhoanKhac: khac.map(k => k.username) })
  }
  const ips = [...new Set(may.map(m => m.ip).filter(Boolean))]
  const ip = []
  for (const x of ips.slice(0, 10)) {
    const khac = await q<{ username: string }>(dbGame(), 'SELECT DISTINCT username FROM tob_may_dang_nhap WHERE ip = ? AND username <> ? LIMIT 30', [x, u])
    ip.push({ ip: x, taiKhoanKhac: khac.map(k => k.username) })
  }
  return { coBang: true, may: ra, ip }
}

export function tvTenChucNang(ma: string): string {
  return TV_CHUC_NANG.find(c => c.ma === ma)?.ten ?? ma
}
