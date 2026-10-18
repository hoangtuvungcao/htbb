/**
 * Đọc số liệu cho trang Kinh tế (/admin/kinh-te). Chỉ đọc, mọi truy vấn có LIMIT + cửa sổ thời gian
 * trên cột có chỉ mục (tob_log_all_item: aclog, timelog; tob_log_buy_shop: timeBuy).
 * Luật cảnh báo + đọc log xu: shared/utils/kinhTeCanhBao.ts (thuần, có test).
 */
import type { NgayKinhTe, NhanXuNhanVat } from '../../shared/utils/kinhTeCanhBao'

/** Số dòng log tối đa đọc cho một truy vấn (log 'truxu' có thể hàng trăm nghìn dòng/ngày). */
export const KINH_TE_LOG_TOI_DA = 100_000

export async function docKinhTeNgay(soNgay: number): Promise<NgayKinhTe[] | null> {
  if (!(await coBang(dbGame(), 'tob_kinh_te_ngay'))) return null
  const n = Math.max(7, Math.min(400, Math.trunc(soNgay) || 90))
  const ds = await q(dbGame(), `SELECT * FROM tob_kinh_te_ngay ORDER BY ngay DESC LIMIT ${n}`)
  return ds.reverse().map(r => ({
    ngay: String(r.ngay instanceof Date ? r.ngay.toISOString() : r.ngay).slice(0, 10),
    soNguoi: Number(r.so_nguoi), capTrungVi: Number(r.cap_trung_vi), chiSoGia: Number(r.chi_so_gia),
    tongXu: Number(r.tong_xu), tongLuong: Number(r.tong_luong), tongLuongKhoa: Number(r.tong_luong_khoa),
    cungTienThuc: Number(r.cung_tien_thuc_x100) / 100, top1: Number(r.top1_phan_tram_x100) / 100,
    xuVao: Number(r.xu_vao), xuRa: Number(r.xu_ra), muc: Number(r.muc),
  }))
}

/** Log nhanxu 48 giờ gần nhất (giới hạn dòng) -> tổng nhận mỗi nhân vật 24h / 24h trước. */
export async function docNhanXu48h(): Promise<{ co: boolean, ds: NhanXuNhanVat[], soDong: number, catBot: boolean }> {
  if (!(await coBang(dbGame(), 'tob_log_all_item'))) return { co: false, ds: [], soDong: 0, catBot: false }
  const rows = await q(
    dbGame(),
    // Tuổi tính ngay trong CSDL (tránh lệch múi giờ giữa driver và máy chủ CSDL).
    `SELECT charname, info, UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(timelog) AS tuoi FROM tob_log_all_item
      WHERE aclog = 'nhanxu' AND timelog >= DATE_SUB(NOW(), INTERVAL 2 DAY)
      ORDER BY id DESC LIMIT ${KINH_TE_LOG_TOI_DA}`,
  )
  const ds = nhanXuTheoNhanVat(rows.map(r => ({ ten: String(r.charname ?? ''), info: String(r.info ?? ''), t: -Number(r.tuoi ?? 0) })), 0)
  return { co: true, ds, soDong: rows.length, catBot: rows.length >= KINH_TE_LOG_TOI_DA }
}

/** Nguồn vào / ra của xu theo log nhanxu / truxu trong `soNgay` ngày. */
export async function docNguonXu(soNgay: number) {
  const ngay = Math.max(1, Math.min(7, Math.trunc(soNgay) || 1))
  if (!(await coBang(dbGame(), 'tob_log_all_item'))) return { co: false, ngay, vao: [], ra: [], soDongVao: 0, soDongRa: 0, catBot: false }
  const doc = (aclog: 'nhanxu' | 'truxu') => q(
    dbGame(),
    `SELECT info FROM tob_log_all_item WHERE aclog = ? AND timelog >= DATE_SUB(NOW(), INTERVAL ${ngay} DAY)
      ORDER BY id DESC LIMIT ${KINH_TE_LOG_TOI_DA}`,
    [aclog],
  )
  const [vao, ra] = await Promise.all([doc('nhanxu'), doc('truxu')])
  return {
    co: true,
    ngay,
    vao: tongHopNguon(vao.map(r => String(r.info ?? ''))),
    ra: tongHopNguon(ra.map(r => String(r.info ?? ''))),
    soDongVao: vao.length,
    soDongRa: ra.length,
    catBot: vao.length >= KINH_TE_LOG_TOI_DA || ra.length >= KINH_TE_LOG_TOI_DA,
  }
}

/** Mua ở shop NPC theo loại tiền (tob_log_buy_shop, gộp theo cửa sổ thời gian bên Java). */
export async function docMuaShop(soNgay: number) {
  const ngay = Math.max(1, Math.min(30, Math.trunc(soNgay) || 1))
  if (!(await coBang(dbGame(), 'tob_log_buy_shop'))) return { co: false, ngay, theoTien: [], top: [] }
  const theoTien = await q(
    dbGame(),
    `SELECT money AS tien, COUNT(*) AS so_lan, SUM(price * soluong) AS tong FROM tob_log_buy_shop
      WHERE timeBuy >= DATE_SUB(NOW(), INTERVAL ${ngay} DAY) GROUP BY money ORDER BY tong DESC LIMIT 20`,
  )
  const top = await q(
    dbGame(),
    `SELECT info, money AS tien, SUM(soluong) AS so_luong, SUM(price * soluong) AS tong FROM tob_log_buy_shop
      WHERE timeBuy >= DATE_SUB(NOW(), INTERVAL ${ngay} DAY) GROUP BY info, money ORDER BY tong DESC LIMIT 20`,
  )
  return {
    co: true,
    ngay,
    theoTien: theoTien.map(r => ({ tien: String(r.tien ?? ''), soLan: Number(r.so_lan), tong: Number(r.tong ?? 0) })),
    top: top.map(r => ({ info: String(r.info ?? '').slice(0, 120), tien: String(r.tien ?? ''), soLuong: Number(r.so_luong ?? 0), tong: Number(r.tong ?? 0) })),
  }
}

/** Thống kê chi tiêu theo tuần (tob_log_use_luong_<tuần>_<tháng>_<năm>): bảng tuần mới nhất. */
export async function docChiTieuTuan() {
  const bang = await q(dbGame(), `SELECT table_name AS t FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name LIKE 'tob_log_use_luong%'`)
    .catch(() => [] as Record<string, any>[])
  const ten = bang.map(r => String(r.t ?? r.TABLE_NAME ?? '')).filter(t => /^tob_log_use_luong(_\d{1,2}_\d{1,2}_\d{4})?$/.test(t))
  const khoa = (t: string) => {
    const m = t.match(/_(\d{1,2})_(\d{1,2})_(\d{4})$/)
    return m ? Number(m[3]) * 10000 + Number(m[2]) * 100 + Number(m[1]) : 0
  }
  ten.sort((a, b) => khoa(b) - khoa(a))
  for (const t of ten) {
    const dong = await q(dbGame(), `SELECT name, moneytype, SUM(soluong) AS so_luong, SUM(solanmua) AS so_lan, SUM(totalmoney) AS tong FROM \`${t}\` GROUP BY name, moneytype ORDER BY tong DESC LIMIT 30`)
    if (dong.length || t === ten.at(-1)) {
      return {
        bang: t,
        tuan: t.replace('tob_log_use_luong', '').replace(/^_/, '').replace(/_/g, '/') || 'gốc',
        dong: dong.map(r => ({ ten: String(r.name ?? ''), tien: String(r.moneytype ?? ''), soLuong: Number(r.so_luong ?? 0), soLan: Number(r.so_lan ?? 0), tong: Number(r.tong ?? 0) })),
      }
    }
  }
  return { bang: null, tuan: '', dong: [] }
}

export type TienKinhTe = 'xu' | 'luong' | 'luong_khoa'

const BIEU_THUC: Record<TienKinhTe, string> = {
  xu: '(COALESCE(gold, 0) + COALESCE(kho_xu, 0))',
  luong: '(COALESCE(luong, 0) + COALESCE(kho_luong, 0))',
  luong_khoa: '(COALESCE(luonglock, 0) + COALESCE(kho_luong_khoa, 0))',
}

/** Top người giữ nhiều nhất cho mỗi loại tiền (túi + kho). */
export async function docTopNamGiu(n: number) {
  const lim = Math.max(5, Math.min(50, Math.trunc(n) || 20))
  const ra: Record<TienKinhTe, { id: number, ten: string, cap: number, tui: number, kho: number, tong: number, lanCuoi: string | null, khoa: boolean, daXoa: boolean }[]> = { xu: [], luong: [], luong_khoa: [] }
  if (!(await coBang(dbGame(), 'tob_char'))) return { co: false, ...ra }
  const cot: Record<TienKinhTe, [string, string]> = { xu: ['gold', 'kho_xu'], luong: ['luong', 'kho_luong'], luong_khoa: ['luonglock', 'kho_luong_khoa'] }
  for (const t of Object.keys(BIEU_THUC) as TienKinhTe[]) {
    const [tui, kho] = cot[t]
    const dong = await q(
      dbGame(),
      `SELECT id, charname, lastLv, \`${tui}\` AS tui, \`${kho}\` AS kho, ${BIEU_THUC[t]} AS tong, lastLog, ban, del
         FROM tob_char ORDER BY tong DESC LIMIT ${lim}`,
    )
    ra[t] = dong.filter(r => Number(r.tong) > 0).map(r => ({
      id: Number(r.id), ten: String(r.charname ?? ''), cap: Number(r.lastLv ?? 0),
      tui: Number(r.tui ?? 0), kho: Number(r.kho ?? 0), tong: Number(r.tong ?? 0),
      lanCuoi: r.lastLog ? String(r.lastLog instanceof Date ? r.lastLog.toISOString().replace('T', ' ').slice(0, 19) : r.lastLog).slice(0, 19) : null,
      khoa: Number(r.ban ?? 0) !== 0, daXoa: Number(r.del ?? 1) === 0,
    }))
  }
  const tongCong = await q1(dbGame(), `SELECT SUM(${BIEU_THUC.xu}) AS xu, SUM(${BIEU_THUC.luong}) AS luong, SUM(${BIEU_THUC.luong_khoa}) AS luong_khoa, COUNT(*) AS n FROM tob_char`)
  return {
    co: true,
    ...ra,
    tongCong: { xu: Number(tongCong?.xu ?? 0), luong: Number(tongCong?.luong ?? 0), luongKhoa: Number(tongCong?.luong_khoa ?? 0), soNhanVat: Number(tongCong?.n ?? 0) },
  }
}
