/**
 * NẠP TIỀN — phần chạm database (bảng nap_* của HTBB/sql/pokemon_nap_tien.sql, nằm trong kpah_db).
 *
 * Web KHÔNG BAO GIỜ cộng lượng: chỉ đổi đơn sang `da_thanh_toan` qua MỘT hàm duy nhất
 * {@link napXacNhanThanhToan} (webhook / admin / mật khẩu chung / sandbox đều đi qua đây). Server
 * game (com.htbb.pokemon.NapTienGame.congDon) cộng lượng qua chốt da_thanh_toan -> da_cong.
 *
 * Idempotent ở ba lớp: UPDATE có điều kiện trên trạng thái cũ (chỉ 1 lần đổi), UNIQUE
 * (nha_cung_cap, ma_gd_ncc) (một giao dịch ngân hàng không xác nhận được 2 đơn / webhook gửi lặp),
 * UNIQUE nap_so_cai.ma_tham_chieu. Mốc thời gian do ứng dụng ghi (gioDb) — NOW() của NovaDB là UTC.
 */
import type { H3Event } from 'h3'
import type { PoolConnection } from 'mysql2/promise'
import type { GiaoDichVao } from './napTienThuan'

export interface GoiNap {
  ma: string
  ten: string
  loai: string
  gia_vnd: number
  luong: number
  mo_ta: string
  dang_ban: number
  thu_tu: number
}

export interface DonNap {
  id: number
  ma_don: string
  user_id: number
  username: string
  char_id: number
  charname: string
  ma_goi: string
  loai: string
  ten_goi: string
  so_tien: number
  luong: number
  trang_thai: string
  nguon: string
  nha_cung_cap: string | null
  ma_gd_ncc: string | null
  so_tien_nhan: number
  tao_luc: string
  thanh_toan_luc: string | null
  cong_luc: string | null
  char_cong: number
  da_luu_nv: number
  hoan_luc: string | null
  ly_do: string
  admin_user: string
}

export const NAP_DON_COT = 'id, ma_don, user_id, username, char_id, charname, ma_goi, loai, ten_goi, so_tien, luong, trang_thai, nguon,'
  + ' nha_cung_cap, ma_gd_ncc, so_tien_nhan, tao_luc, thanh_toan_luc, cong_luc, char_cong, da_luu_nv, hoan_luc, ly_do, admin_user'

/** Đơn giữ chỗ trong trần nạp: mọi trạng thái trừ thất bại / hoàn. */
const TT_TINH_TRAN = "('cho_thanh_toan','da_thanh_toan','da_cong','cho_thu_hoi')"

export async function napCoBang(): Promise<boolean> {
  return (await coBang(dbGame(), 'nap_don')) && (await coBang(dbGame(), 'nap_goi')) && (await coBang(dbGame(), 'nap_so_cai'))
}

export function napThieuBang(): never {
  throw apiLoi(409, 'Chưa có bảng nạp tiền (nap_*) — chạy HTBB/sql/pokemon_nap_tien.sql trước.', { code: 'thieu_bang' })
}

// ---------------------------------------------------------------- nhật ký

export async function napNhatKy(ex: PoolConnection | null, tacNhan: string, hanhDong: string, maDon: string, userId: number, chiTiet: string, ip = ''): Promise<void> {
  await exec(ex ?? dbGame(),
    'INSERT INTO nap_nhat_ky (tac_nhan, hanh_dong, ma_don, user_id, chi_tiet, ip, tao_luc) VALUES (?, ?, ?, ?, ?, ?, ?)',
    [tacNhan.slice(0, 60), hanhDong.slice(0, 40), maDon.slice(0, 16), Math.trunc(userId), chiTiet.slice(0, 500), ip.slice(0, 64), gioDb()])
}

// ---------------------------------------------------------------- cấu hình

export async function napCauHinh(): Promise<Record<string, string>> {
  const ra: Record<string, string> = {}
  for (const r of await q<{ khoa: string, gia_tri: string, cap_nhat_luc: string | null }>(dbGame(), 'SELECT khoa, gia_tri, cap_nhat_luc FROM nap_cau_hinh')) {
    ra[r.khoa] = String(r.gia_tri)
    if (r.cap_nhat_luc) ra[`${r.khoa}@luc`] = String(r.cap_nhat_luc)
  }
  return ra
}

export async function napTran(): Promise<{ tuan: number, thang: number }> {
  const c = await napCauHinh().catch(() => ({} as Record<string, string>))
  return {
    tuan: kepTran(Number(c.tran_tuan_vnd ?? 0), TRAN_TUAN_VND),
    thang: kepTran(Number(c.tran_thang_vnd ?? 0), TRAN_THANG_VND),
  }
}

// ---------------------------------------------------------------- gói

export async function napDanhSachGoi(chiDangBan = true): Promise<GoiNap[]> {
  const rows = await q<GoiNap>(dbGame(),
    `SELECT ma, ten, loai, gia_vnd, luong, mo_ta, dang_ban, thu_tu FROM nap_goi ${chiDangBan ? 'WHERE dang_ban = 1' : ''} ORDER BY thu_tu, ma`)
  // Dòng sai luật (admin sửa thẳng DB) không bao giờ được bán.
  return rows.filter(g => !chiDangBan || kiemGoi(g.loai, Number(g.gia_vnd), Number(g.luong)) === null)
}

// ---------------------------------------------------------------- ví / trần của người chơi

export interface ViNap {
  tong_nap_vnd: number
  no_luong: number
  khoa_nap: number
  tran_tu_dat: number
  tran_cho: number
  tran_cho_luc: string | null
}

async function damBaoVi(ex: PoolConnection | null, userId: number): Promise<void> {
  await exec(ex ?? dbGame(), 'INSERT IGNORE INTO nap_vi (user_id, cap_nhat_luc) VALUES (?, ?)', [Math.trunc(userId), gioDb()])
}

/** Đọc ví; mức trần đang chờ 72 giờ đủ hạn thì áp dụng luôn. */
export async function napVi(userId: number): Promise<ViNap> {
  await damBaoVi(null, userId)
  const v = await q1<ViNap>(dbGame(),
    'SELECT tong_nap_vnd, no_luong, khoa_nap, tran_tu_dat, tran_cho, tran_cho_luc FROM nap_vi WHERE user_id = ?', [Math.trunc(userId)])
  const vi = v ?? { tong_nap_vnd: 0, no_luong: 0, khoa_nap: 0, tran_tu_dat: 0, tran_cho: 0, tran_cho_luc: null }
  if (vi.tran_cho_luc && vi.tran_cho_luc <= gioDb()) {
    await exec(dbGame(), 'UPDATE nap_vi SET tran_tu_dat = ?, tran_cho = 0, tran_cho_luc = NULL WHERE user_id = ? AND tran_cho_luc = ?',
      [Number(vi.tran_cho), Math.trunc(userId), vi.tran_cho_luc])
    vi.tran_tu_dat = Number(vi.tran_cho)
    vi.tran_cho = 0
    vi.tran_cho_luc = null
  }
  return vi
}

/** Tổng đơn giữ chỗ trong tuần (từ thứ Hai) / tháng (từ ngày 1) hiện tại. */
export async function napDaDung(ex: PoolConnection | null, userId: number): Promise<{ tuan: number, thang: number }> {
  const { tuan, thang } = dauTuanThang()
  const tong = async (tu: string) => Number(await qv(ex ?? dbGame(),
    `SELECT COALESCE(SUM(so_tien), 0) FROM nap_don WHERE user_id = ? AND tao_luc >= ? AND trang_thai IN ${TT_TINH_TRAN}`,
    [Math.trunc(userId), tu]) ?? 0)
  return { tuan: await tong(tuan), thang: await tong(thang) }
}

/** Người chơi tự đặt trần tháng: HẠ áp dụng ngay, NÂNG (hoặc bỏ) sau 72 giờ. 0 = bỏ trần tự đặt. */
export async function napDatTranTuDat(userId: number, tranMoi: number): Promise<{ ngay: boolean, hieuLucLuc: string | null }> {
  const t = Math.max(0, Math.min(TRAN_CUNG_THANG_VND, Math.trunc(tranMoi)))
  const vi = await napVi(userId)
  const cu = Number(vi.tran_tu_dat)
  const laHa = t > 0 && (cu === 0 || t <= cu)
  if (laHa) {
    await exec(dbGame(), 'UPDATE nap_vi SET tran_tu_dat = ?, tran_cho = 0, tran_cho_luc = NULL, cap_nhat_luc = ? WHERE user_id = ?', [t, gioDb(), Math.trunc(userId)])
    await napNhatKy(null, 'web', 'tu_dat_tran', '', userId, `hạ trần tháng xuống ${t}`)
    return { ngay: true, hieuLucLuc: null }
  }
  const luc = gioDb(new Date(Date.now() + GIO_CHO_NANG_TRAN * 3600_000))
  await exec(dbGame(), 'UPDATE nap_vi SET tran_cho = ?, tran_cho_luc = ?, cap_nhat_luc = ? WHERE user_id = ?', [t, luc, gioDb(), Math.trunc(userId)])
  await napNhatKy(null, 'web', 'tu_dat_tran', '', userId, `nâng trần tháng lên ${t || 'mặc định'} — hiệu lực ${luc}`)
  return { ngay: false, hieuLucLuc: luc }
}

// ---------------------------------------------------------------- tạo đơn

export interface NhanVatNap {
  id: number
  charname: string
  lastLv: number
}

export async function napNhanVat(userId: number): Promise<NhanVatNap[]> {
  return q<NhanVatNap>(dbGame(), 'SELECT id, charname, lastLv FROM tob_char WHERE userId = ? ORDER BY xp DESC LIMIT 20', [Math.trunc(userId)])
}

/** Đơn chờ quá 24 giờ -> that_bai (tiền đến muộn vẫn xác nhận được: that_bai -> da_thanh_toan). */
export async function napHetHanDonCu(): Promise<void> {
  await exec(dbGame(), "UPDATE nap_don SET trang_thai = 'that_bai', ly_do = 'het han 24 gio' WHERE trang_thai = 'cho_thanh_toan' AND tao_luc < ?",
    [gioDb(new Date(Date.now() - GIO_HET_HAN_DON * 3600_000))])
}

export async function napTaoDon(u: { id: number, username: string }, maGoi: string, charId: number, khoaIdem: string | null, nguon: 'web' | 'admin' = 'web', adminUser = ''): Promise<DonNap> {
  return transaction(dbGame(), async (conn) => {
    if (khoaIdem) {
      // Bấm hai lần / mạng chập chờn gửi lại: trả lại đơn đã tạo, không tạo đơn mới.
      const cu = await q1<DonNap>(conn, `SELECT ${NAP_DON_COT} FROM nap_don WHERE user_id = ? AND khoa_idem = ? LIMIT 1`, [u.id, khoaIdem])
      if (cu) return cu
    }
    await damBaoVi(conn, u.id)
    const vi = await q1<{ khoa_nap: number, tran_tu_dat: number }>(conn, 'SELECT khoa_nap, tran_tu_dat FROM nap_vi WHERE user_id = ? FOR UPDATE', [u.id]) // khoá theo tài khoản: hai đơn song song không lách được trần
    if (Number(vi?.khoa_nap) === 1 && nguon === 'web') {
      throw apiLoi(403, 'Tài khoản tạm khoá nạp do có giao dịch bị hoàn/khiếu nại. Liên hệ ban quản trị.')
    }
    const goi = await q1<GoiNap>(conn, 'SELECT ma, ten, loai, gia_vnd, luong, mo_ta, dang_ban, thu_tu FROM nap_goi WHERE ma = ? LIMIT 1', [maGoi])
    if (!goi || (Number(goi.dang_ban) !== 1 && nguon === 'web')) throw apiLoi(404, 'Gói không tồn tại hoặc đã ngừng bán.')
    const loiGoi = kiemGoi(goi.loai, Number(goi.gia_vnd), Number(goi.luong))
    if (loiGoi) throw apiLoi(409, `Gói đang cấu hình sai: ${loiGoi}`)
    const nv = await q1<{ id: number, charname: string }>(conn, 'SELECT id, charname FROM tob_char WHERE id = ? AND userId = ? LIMIT 1', [Math.trunc(charId), u.id])
    if (!nv) throw apiLoi(400, 'Hãy chọn nhân vật nhận (nhân vật của chính tài khoản này).')
    if (nguon === 'web') {
      const tran = await napTran()
      const da = await napDaDung(conn, u.id)
      const loi = kiemTran(da.tuan, da.thang, Number(goi.gia_vnd), tran.tuan, tran.thang, Number(vi?.tran_tu_dat ?? 0))
      if (loi) throw apiLoi(409, loi, { code: 'vuot_tran' })
    }
    let maDon = taoMaDon()
    for (let i = 0; i < 5 && await qv(conn, 'SELECT 1 FROM nap_don WHERE ma_don = ?', [maDon]); i++) maDon = taoMaDon()
    await exec(conn,
      `INSERT INTO nap_don (ma_don, khoa_idem, user_id, username, char_id, charname, ma_goi, loai, ten_goi, so_tien, luong, trang_thai, nguon, tao_luc, admin_user)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'cho_thanh_toan', ?, ?, ?)`,
      [maDon, khoaIdem, u.id, u.username.slice(0, 50), nv.id, String(nv.charname).slice(0, 50), goi.ma, goi.loai, goi.ten.slice(0, 100),
        Number(goi.gia_vnd), Number(goi.luong), nguon, gioDb(), adminUser.slice(0, 50)])
    await napNhatKy(conn, nguon === 'admin' ? `admin:${adminUser}` : 'web', 'tao_don', maDon, u.id,
      `${goi.ma} ${goi.gia_vnd} đ -> ${nv.charname} (id ${nv.id})`)
    return (await q1<DonNap>(conn, `SELECT ${NAP_DON_COT} FROM nap_don WHERE ma_don = ? LIMIT 1`, [maDon]))!
  })
}

export async function napLayDon(maDon: string): Promise<DonNap | null> {
  return q1<DonNap>(dbGame(), `SELECT ${NAP_DON_COT} FROM nap_don WHERE ma_don = ? LIMIT 1`, [maDon])
}

export async function napLichSu(userId: number): Promise<DonNap[]> {
  return q<DonNap>(dbGame(), `SELECT ${NAP_DON_COT} FROM nap_don WHERE user_id = ? ORDER BY id DESC LIMIT 50`, [Math.trunc(userId)])
}

/** QR VietQR cho 1 đơn (ảnh do img.vietqr.io dựng — không lộ bí mật nào). */
export function napQr(soTien: number, maDon: string): string {
  const n = useHtbbConfig().nap
  const path = `${n.bankBin}-${encodeURIComponent(n.accountNo)}-compact2.png`
  return `https://img.vietqr.io/image/${path}?${new URLSearchParams({ amount: String(Math.trunc(soTien)), addInfo: maDon, accountName: n.accountName })}`
}

/** Dữ liệu đơn được phép trả cho CHỦ đơn. */
export function napDonCongKhai(d: DonNap) {
  return {
    ma_don: d.ma_don, ma_goi: d.ma_goi, loai: d.loai, ten_goi: d.ten_goi, so_tien: Number(d.so_tien), luong: Number(d.luong),
    charname: d.charname, trang_thai: d.trang_thai, tao_luc: d.tao_luc, thanh_toan_luc: d.thanh_toan_luc, cong_luc: d.cong_luc,
  }
}

// ---------------------------------------------------------------- XÁC NHẬN THANH TOÁN (hàm duy nhất)

export type KetQuaXacNhan = 'ok' | 'da_xu_ly' | 'khong_thay' | 'thieu_tien' | 'gd_da_dung' | 'sai_trang_thai'

/**
 * HÀM DUY NHẤT đổi đơn sang da_thanh_toan. Gọi lặp / song song an toàn:
 *   - SELECT .. FOR UPDATE trong transaction + UPDATE .. WHERE trang_thai IN ('cho_thanh_toan','that_bai');
 *   - UNIQUE (nha_cung_cap, ma_gd_ncc): một giao dịch ngân hàng chỉ xác nhận được một đơn;
 *   - sổ cái UNIQUE 'tt:<mã đơn>'.
 * Thiếu tiền: KHÔNG xác nhận (ghi nhật ký, admin xử lý tay). Không bao giờ cộng lượng ở đây.
 */
export async function napXacNhanThanhToan(a: {
  maDon: string
  soTienNhan: number
  nhaCungCap: 'sepay' | 'casso' | 'mock' | 'admin' | 'mat_khau_chung'
  maGd: string
  tacNhan: string
  ip?: string
  ghiChu?: string
}): Promise<{ ketQua: KetQuaXacNhan, don: DonNap | null }> {
  return transaction(dbGame(), async (conn) => {
    const don = await q1<DonNap>(conn, `SELECT ${NAP_DON_COT} FROM nap_don WHERE ma_don = ? LIMIT 1 FOR UPDATE`, [a.maDon])
    if (!don) return { ketQua: 'khong_thay' as const, don: null }
    if (don.nha_cung_cap === a.nhaCungCap && don.ma_gd_ncc === a.maGd) return { ketQua: 'da_xu_ly' as const, don }
    if (!['cho_thanh_toan', 'that_bai'].includes(don.trang_thai)) {
      return { ketQua: (['da_thanh_toan', 'da_cong'].includes(don.trang_thai) ? 'da_xu_ly' : 'sai_trang_thai') as KetQuaXacNhan, don }
    }
    const daDung = await qv(conn, 'SELECT ma_don FROM nap_don WHERE nha_cung_cap = ? AND ma_gd_ncc = ? LIMIT 1', [a.nhaCungCap, a.maGd])
    if (daDung) return { ketQua: 'gd_da_dung' as const, don }
    const khop = khopSoTien(Number(don.so_tien), Math.trunc(a.soTienNhan))
    if (khop === 'thieu') {
      await napNhatKy(conn, a.tacNhan, 'thieu_tien', don.ma_don, don.user_id,
        `nhận ${a.soTienNhan} < ${don.so_tien} (${a.nhaCungCap} ${a.maGd}) — KHÔNG xác nhận, xử lý tay`, a.ip ?? '')
      return { ketQua: 'thieu_tien' as const, don }
    }
    const res = await exec(conn,
      `UPDATE nap_don SET trang_thai = 'da_thanh_toan', thanh_toan_luc = ?, nha_cung_cap = ?, ma_gd_ncc = ?, so_tien_nhan = ?, ly_do = ?
        WHERE id = ? AND trang_thai IN ('cho_thanh_toan','that_bai')`,
      [gioDb(), a.nhaCungCap, a.maGd.slice(0, 64), Math.trunc(a.soTienNhan), (a.ghiChu ?? (khop === 'du_thua' ? 'chuyen du' : '')).slice(0, 255), don.id])
    if (res.affectedRows !== 1) return { ketQua: 'da_xu_ly' as const, don }
    try {
      await exec(conn,
        'INSERT INTO nap_so_cai (user_id, char_id, loai, so_luong, so_tien, ma_don, ma_tham_chieu, ghi_chu, tao_luc) VALUES (?, ?, ?, 0, ?, ?, ?, ?, ?)',
        [don.user_id, don.char_id, 'thanh_toan', Math.trunc(a.soTienNhan), don.ma_don, `tt:${don.ma_don}`, `${a.nhaCungCap} ${a.maGd}`.slice(0, 255), gioDb()])
    } catch (e: any) {
      if (!/duplicate|unique/i.test(String(e?.message ?? e))) throw e
    }
    await damBaoVi(conn, don.user_id)
    await exec(conn, 'UPDATE nap_vi SET tong_nap_vnd = tong_nap_vnd + ?, so_don = so_don + 1, cap_nhat_luc = ? WHERE user_id = ?',
      [Math.trunc(a.soTienNhan), gioDb(), don.user_id])
    await napNhatKy(conn, a.tacNhan, 'xac_nhan_thanh_toan', don.ma_don, don.user_id,
      `${a.nhaCungCap} ${a.maGd} ${a.soTienNhan} đ${khop === 'du_thua' ? ' (chuyển dư)' : ''}${a.ghiChu ? ` — ${a.ghiChu}` : ''}`, a.ip ?? '')
    return { ketQua: 'ok' as const, don: { ...don, trang_thai: 'da_thanh_toan' } }
  })
}

/** Ghi webhook thô (đối soát). false = mã giao dịch này đã nhận rồi (gọi lặp). */
export async function napGhiWebhook(ncc: string, maGd: string, chuKyHopLe: boolean, soTien: number, noiDung: string, maDon: string, ketQua: string, ip: string): Promise<boolean> {
  try {
    await exec(dbGame(),
      'INSERT INTO nap_webhook (nha_cung_cap, ma_gd, chu_ky_hop_le, so_tien, noi_dung, ma_don, ket_qua, ip, tao_luc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
      [ncc, maGd.slice(0, 64), chuKyHopLe ? 1 : 0, Math.trunc(soTien), noiDung.slice(0, 500), maDon.slice(0, 16), ketQua.slice(0, 120), ip.slice(0, 64), gioDb()])
    return true
  } catch (e: any) {
    if (/duplicate|unique/i.test(String(e?.message ?? e))) return false
    throw e
  }
}

export async function napCapNhatWebhook(ncc: string, maGd: string, maDon: string, ketQua: string): Promise<void> {
  await exec(dbGame(), 'UPDATE nap_webhook SET ma_don = ?, ket_qua = ? WHERE nha_cung_cap = ? AND ma_gd = ?', [maDon.slice(0, 16), ketQua.slice(0, 120), ncc, maGd.slice(0, 64)])
}

/**
 * Xử lý MỘT giao dịch tiền vào (chung cho SePay / Casso / mock): ghi webhook (UNIQUE chặn gọi
 * lặp) -> tìm mã đơn trong nội dung -> napXacNhanThanhToan.
 */
export async function napXuLyGiaoDich(event: H3Event, ncc: 'sepay' | 'casso' | 'mock', gd: GiaoDichVao): Promise<{ success: boolean, message: string }> {
  const ip = ipKhach(event)
  const maDon = timMaDon(gd.noiDung)
  const moi = await napGhiWebhook(ncc, gd.maGd, true, gd.soTien, gd.noiDung, maDon ?? '', 'dang_xu_ly', ip)
  if (!moi) return { success: true, message: 'duplicate (already received)' }
  if (!maDon) {
    await napCapNhatWebhook(ncc, gd.maGd, '', /KP[A-Z0-9]{6,10}/i.test(gd.noiDung) ? 'ma_don_cu_moc_nap_xu_ly_tay' : 'khong_co_ma_don')
    return { success: true, message: 'no order code in content' }
  }
  const kq = await napXacNhanThanhToan({ maDon, soTienNhan: gd.soTien, nhaCungCap: ncc, maGd: gd.maGd, tacNhan: `webhook:${ncc}`, ip })
  await napCapNhatWebhook(ncc, gd.maGd, maDon, kq.ketQua)
  return { success: kq.ketQua === 'ok' || kq.ketQua === 'da_xu_ly', message: `${kq.ketQua} ${maDon}` }
}

// ---------------------------------------------------------------- hoàn tiền / cộng tay

/**
 * Hoàn tiền / chargeback. da_thanh_toan -> hoan_tien (chưa vào game: xong). da_cong -> cho_thu_hoi
 * (server game chuyển thành nợ, trừ dần lượng; thẻ tháng -30 ngày; vé mùa mất cao cấp).
 * chargeback = khoá tạo đơn mới của tài khoản.
 */
export async function napHoanTien(id: number, admin: string, lyDo: string, chargeback: boolean, ip: string): Promise<DonNap> {
  return transaction(dbGame(), async (conn) => {
    const don = await q1<DonNap>(conn, `SELECT ${NAP_DON_COT} FROM nap_don WHERE id = ? LIMIT 1 FOR UPDATE`, [Math.trunc(id)])
    if (!don) throw apiLoi(404, `Không có đơn #${id}.`)
    const den = don.trang_thai === 'da_thanh_toan' ? 'hoan_tien' : (don.trang_thai === 'da_cong' ? 'cho_thu_hoi' : null)
    if (!den || !chuyenHopLe(don.trang_thai, den)) throw apiLoi(409, `Đơn đang "${don.trang_thai}" — chỉ hoàn được đơn đã thanh toán hoặc đã cộng.`)
    const res = await exec(conn, 'UPDATE nap_don SET trang_thai = ?, hoan_luc = ?, ly_do = ?, admin_user = ? WHERE id = ? AND trang_thai = ?',
      [den, gioDb(), `${chargeback ? '[chargeback] ' : ''}${lyDo}`.slice(0, 255), admin.slice(0, 50), don.id, don.trang_thai])
    if (res.affectedRows !== 1) throw apiLoi(409, 'Đơn vừa đổi trạng thái, tải lại rồi thử lại.')
    try {
      await exec(conn,
        'INSERT INTO nap_so_cai (user_id, char_id, loai, so_luong, so_tien, ma_don, ma_tham_chieu, ghi_chu, tao_luc) VALUES (?, ?, ?, 0, ?, ?, ?, ?, ?)',
        [don.user_id, don.char_id, chargeback ? 'chargeback' : 'hoan_tien', -Number(don.so_tien_nhan || don.so_tien), don.ma_don, `hoan:${don.ma_don}`, lyDo.slice(0, 255), gioDb()])
    } catch (e: any) {
      if (!/duplicate|unique/i.test(String(e?.message ?? e))) throw e
    }
    await damBaoVi(conn, don.user_id)
    await exec(conn, `UPDATE nap_vi SET tong_nap_vnd = tong_nap_vnd - ?, cap_nhat_luc = ?${chargeback ? ', khoa_nap = 1' : ''} WHERE user_id = ?`,
      [Number(don.so_tien_nhan || don.so_tien), gioDb(), don.user_id])
    await napNhatKy(conn, `admin:${admin}`, chargeback ? 'chargeback' : 'hoan_tien', don.ma_don, don.user_id,
      `${don.trang_thai} -> ${den}: ${lyDo}`, ip)
    return { ...don, trang_thai: den }
  })
}

// ---------------------------------------------------------------- đối soát

export async function napDoiSoat() {
  const thongKe = await q<{ trang_thai: string, so: number, tien: number }>(dbGame(),
    'SELECT trang_thai, COUNT(*) AS so, COALESCE(SUM(so_tien), 0) AS tien FROM nap_don GROUP BY trang_thai')
  const muoiPhut = gioDb(new Date(Date.now() - 10 * 60_000))
  const choCongLau = await q<DonNap>(dbGame(),
    `SELECT ${NAP_DON_COT} FROM nap_don WHERE trang_thai = 'da_thanh_toan' AND thanh_toan_luc < ? ORDER BY id LIMIT 50`, [muoiPhut])
  const congChuaLuu = await q<DonNap>(dbGame(),
    `SELECT ${NAP_DON_COT} FROM nap_don WHERE trang_thai = 'da_cong' AND da_luu_nv = 0 AND cong_luc < ? ORDER BY id LIMIT 50`, [muoiPhut])
  const webhookLoi = await q(dbGame(),
    `SELECT id, nha_cung_cap, ma_gd, so_tien, noi_dung, ma_don, ket_qua, tao_luc FROM nap_webhook
      WHERE ket_qua NOT IN ('ok', 'da_xu_ly') AND tao_luc >= ? ORDER BY id DESC LIMIT 50`, [gioDb(new Date(Date.now() - 7 * 86400_000))])
  const theoNgay = await q<{ ngay: string, so: number, tien: number }>(dbGame(),
    `SELECT SUBSTR(thanh_toan_luc, 1, 10) AS ngay, COUNT(*) AS so, COALESCE(SUM(so_tien_nhan), 0) AS tien FROM nap_don
      WHERE thanh_toan_luc IS NOT NULL AND thanh_toan_luc >= ? GROUP BY SUBSTR(thanh_toan_luc, 1, 10) ORDER BY ngay DESC`,
    [gioDb(new Date(Date.now() - 30 * 86400_000))])
  const soCai = await q<{ loai: string, so: number, luong: number, tien: number }>(dbGame(),
    'SELECT loai, COUNT(*) AS so, COALESCE(SUM(so_luong), 0) AS luong, COALESCE(SUM(so_tien), 0) AS tien FROM nap_so_cai GROUP BY loai')
  const cauHinh = await napCauHinh()
  return { thongKe, choCongLau, congChuaLuu, webhookLoi, theoNgay, soCai, cauHinh }
}

export async function napTenTaiKhoan(ten: string): Promise<{ id: number, username: string } | null> {
  return q1<{ id: number, username: string }>(dbAccount(), 'SELECT id, username FROM team_user WHERE username = ? LIMIT 1', [ten.trim().toLowerCase()])
}
