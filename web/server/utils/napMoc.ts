/**
 * NẠP TIỀN THEO MỐC (bảng tob_nap_moc trong htbb_db, xem vps/db_new_server.sql PHẦN 2I).
 * Tạo / đọc đơn, và phần "xác nhận đã nhận tiền" mà webhook SePay, trang
 * /xac-nhan-nap (mật khẩu chung) và trang /admin/naptien (khu quản trị) cùng dùng.
 *
 * Luồng trạng thái: cho_thanh_toan -> da_thanh_toan (webhook / admin xác nhận)
 *                   -> da_nhan (server game ghi khi người chơi nhận quà ở NPC Tổng Quản).
 * Web KHÔNG BAO GIỜ phát quà trực tiếp — chỉ đổi trạng thái; quà do server game cấp.
 */
import { createHmac } from 'node:crypto'
import type { H3Event } from 'h3'
import type { PoolConnection } from 'mysql2/promise'

export interface DonNapMoc {
  id: number
  userID: number
  moc: number
  amount: number
  ma_don: string
  phuong_hoang: number
  trang_thai: string
  created_at: string
  paid_at: string | null
  claimed_at: string | null
}

/** Các cột của tob_nap_moc được đọc ra (liệt kê rõ thay vì SELECT *). */
export const NAP_MOC_COT = 'id, userID, moc, amount, ma_don, phuong_hoang, trang_thai, created_at, paid_at, claimed_at'

/** URL QR VietQR theo tài khoản TPBank đã cấu hình. */
export function napMocQr(amount: number, maDon: string): string {
  const n = useHtbbConfig().nap
  return napMocVietQrUrl(n.bankBin, n.accountNo, n.accountName, amount, maDon)
}

/** Tạo 1 đơn nạp mốc mới (trạng thái chờ thanh toán). */
export async function napMocTaoDon(userID: number, moc: number): Promise<{ ma_don: string, amount: number, moc: number }> {
  const info = MOC_NAP[moc as 1 | 2 | 3]
  if (!info) throw new Error('Mốc không hợp lệ')
  const amount = Math.trunc(info.gia)

  let maDon: string
  do {
    maDon = napMocTaoMaDon()
  } while (await qv(dbGame(), 'SELECT 1 FROM tob_nap_moc WHERE ma_don = ?', [maDon]))

  await exec(dbGame(), 'INSERT INTO tob_nap_moc (userID, moc, amount, ma_don) VALUES (?, ?, ?, ?)', [userID, moc, amount, maDon])
  return { ma_don: maDon, amount, moc }
}

/** Lấy 1 đơn nạp mốc theo mã đơn (để hiển thị QR / kiểm tra trạng thái). */
export async function napMocLayDon(maDon: string): Promise<DonNapMoc | null> {
  return q1<DonNapMoc>(dbGame(), `SELECT ${NAP_MOC_COT} FROM tob_nap_moc WHERE ma_don = ? LIMIT 1`, [maDon])
}

/**
 * Chuyển đơn sang da_thanh_toan. Idempotent: WHERE trang_thai='cho_thanh_toan'
 * nên webhook gửi lặp hay hai admin bấm cùng lúc cũng chỉ một lần có tác dụng.
 * Trả về true khi đúng lần này đã đổi trạng thái.
 */
export async function napMocDanhDauDaThanhToan(id: number, ex: PoolConnection | null = null): Promise<boolean> {
  const res = await exec(
    ex ?? dbGame(),
    "UPDATE tob_nap_moc SET trang_thai='da_thanh_toan', paid_at=NOW() WHERE id = ? AND trang_thai='cho_thanh_toan'",
    [Math.trunc(id)],
  )
  return res.affectedRows > 0
}

/** Xác nhận thủ công một đơn theo id (dùng chung cho hai trang admin). */
export async function napMocXacNhanThuCong(id: number): Promise<{ ok: boolean, don: DonNapMoc | null }> {
  const don = await q1<DonNapMoc>(dbGame(), `SELECT ${NAP_MOC_COT} FROM tob_nap_moc WHERE id = ? LIMIT 1`, [Math.trunc(id)])
  if (!don) return { ok: false, don: null }
  const ok = await napMocDanhDauDaThanhToan(don.id)
  return { ok, don }
}

/** Tên tài khoản (account.team_user) cho danh sách userID — hai database khác nhau nên tra riêng. */
export async function napMocTenTaiKhoan(ids: number[]): Promise<Record<number, string>> {
  const ds = [...new Set(ids.map(i => Math.trunc(Number(i))).filter(i => Number.isFinite(i)))]
  const ra: Record<number, string> = {}
  if (!ds.length) return ra
  const ph = ds.map(() => '?').join(',')
  const rows = await q<{ id: number, username: string }>(dbAccount(), `SELECT id, username FROM team_user WHERE id IN (${ph})`, ds)
  for (const r of rows) ra[Number(r.id)] = String(r.username)
  return ra
}

/** Gắn tên tài khoản vào từng đơn. */
export async function napMocGanTen<T extends { userID: number }>(dons: T[]): Promise<(T & { username: string | null })[]> {
  const ten = await napMocTenTaiKhoan(dons.map(d => d.userID))
  return dons.map(d => ({ ...d, username: ten[Number(d.userID)] ?? null }))
}

/**
 * Danh sách đơn đang chờ thanh toán cho trang /xac-nhan-nap, mới nhất trước.
 * Trang này chỉ cần mật khẩu chung nên chỉ trả đúng các cột cần để đối chiếu.
 */
export async function napMocDonDangCho() {
  const dons = await q<Pick<DonNapMoc, 'id' | 'userID' | 'moc' | 'amount' | 'ma_don' | 'created_at'>>(
    dbGame(),
    "SELECT id, userID, moc, amount, ma_don, created_at FROM tob_nap_moc WHERE trang_thai='cho_thanh_toan' ORDER BY created_at DESC",
  )
  return napMocGanTen(dons)
}

/** Dòng nhật ký khi xác nhận tay (hanh_dong = 'xac_nhan_nap_moc'). */
export function napMocMoTaDon(don: DonNapMoc, username: string | null): string {
  return `đơn #${don.id}, mốc ${don.moc}, ${vnd(don.amount)}, tài khoản ${username ?? `userID ${don.userID}`}`
}

// ---------------------------------------------------------------------------
//  Trang /xac-nhan-nap (mật khẩu chung HTBB_ADMIN_NAPTIEN_PASSWORD)
// ---------------------------------------------------------------------------

/**
 * FAIL CLOSED: 'doi-mat-khau-nay' là giá trị MẶC ĐỊNH công khai (có trong mã nguồn và
 * file cấu hình mẫu) — không phải mật khẩu thật. Nếu so sánh thẳng với nó khi quên đặt
 * mật khẩu riêng thì BẤT KỲ AI cũng đăng nhập được bằng chuỗi này và tự xác nhận đơn
 * nạp của chính mình mà không trả đồng nào (server game cấp thẳng lượng + pet). Cùng
 * triết lý với ADMIN_ACCOUNTS để trống: quên cấu hình phải TỰ ĐỘNG tự khoá (trả 404
 * như chưa từng có trang này).
 */
export const NAP_MOC_MK_MAC_DINH = 'doi-mat-khau-nay'

export function napMocTrangAdminMo(): boolean {
  const mk = useHtbbConfig().nap.adminNaptienPassword
  return mk !== '' && mk !== NAP_MOC_MK_MAC_DINH
}

export function napMocDongTrangAdmin(): never {
  throw createError({ statusCode: 404, statusMessage: 'Not Found', data: { code: 'not_found' } })
}

/** Token 'naptien' sống 1 giờ rồi phải nhập lại mật khẩu. */
export const NAP_MOC_TOKEN_HAN = 3600

/**
 * Dấu vân tay của mật khẩu hiện tại, ghi vào token 'naptien'. Đổi mật khẩu thì mọi
 * token đã phát trước đó mất hiệu lực ngay, không phải chờ hết 1 giờ. Là HMAC theo
 * khoá bí mật của site nên đọc token cũng không suy ra được mật khẩu.
 */
export function napMocVanTayMatKhau(): string {
  return createHmac('sha256', useHtbbConfig().secret)
    .update(`naptien|${useHtbbConfig().nap.adminNaptienPassword}`)
    .digest('base64url')
    .slice(0, 22)
}

export function napMocTaoTokenTrangAdmin(): string {
  return taoToken('naptien', 0, napMocVanTayMatKhau(), NAP_MOC_TOKEN_HAN)
}

export function napMocRequireTrangAdmin(event: H3Event): void {
  if (!napMocTrangAdminMo()) napMocDongTrangAdmin()
  const p = docToken(bearer(event), 'naptien')
  if (!p || p.u !== napMocVanTayMatKhau()) {
    throw apiLoi(401, 'Phiên xác nhận nạp tiền đã hết hạn. Hãy nhập lại mật khẩu.', { code: 'naptien_required' })
  }
}
