/**
 * Quên mật khẩu — BƯỚC 2: nhập mã xác nhận (gửi qua email ở bước 1, /api/auth/quen-mat-khau) + mật khẩu mới.
 * Body: { username, ma, password, confirm } -> { ok: true }
 *
 * Mã gắn với hash mật khẩu hiện tại (matKhau.ts taoMaQuenMk): đổi xong là mã hết hiệu lực, mọi phiên web cũ
 * cũng hết hiệu lực (token gắn dấu mật khẩu - auth.ts). Sai mã: hạn mức theo IP + theo tài khoản.
 * Sai tên tài khoản và sai mã trả CÙNG một lỗi (không lộ tài khoản nào có thật).
 */
const DAT_LAI_SAI_TOI_DA = 10
const DAT_LAI_SAI_TRONG_GIAY = 900
const LOI_MA = 'Mã xác nhận không đúng hoặc đã hết hạn. Hãy yêu cầu mã mới.'

export default defineEventHandler(async (event) => {
  if (await biChanSpam(event, 'dat_lai_mk_sai', DAT_LAI_SAI_TOI_DA, DAT_LAI_SAI_TRONG_GIAY)) {
    throw apiLoi(429, 'Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau 15 phút.')
  }

  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const s = (v: unknown) => (typeof v === 'string' ? v : '')
  const username = str(body?.username).toLowerCase()
  const ma = s(body?.ma).trim()
  const password = s(body?.password)
  const confirm = s(body?.confirm)

  if (username === '' || ma === '') throw apiLoi(400, 'Vui lòng nhập tên tài khoản và mã xác nhận.')
  if (!matKhauGameHopLe(password)) throw apiLoi(400, LOI_MAT_KHAU_GAME)
  if (password !== confirm) throw apiLoi(400, 'Mật khẩu nhập lại không khớp.')
  if (saiMaBiChan(username)) throw apiLoi(429, 'Tài khoản này đã nhập sai mã quá nhiều lần. Vui lòng thử lại sau một giờ.')

  const acc = dbAccount()
  let row: { id: number, ban: number, hash: string | null } | null
  try {
    row = await q1(acc, 'SELECT id, ban, `password` AS hash FROM team_user WHERE username = ? LIMIT 1', [username])
  } catch (e: any) {
    console.error('Dat lai mat khau unavailable:', e?.code ?? e)
    throw apiLoi(503, 'Hệ thống đang bận. Vui lòng thử lại sau.')
  }

  const userId = Math.trunc(Number(row?.id ?? 0))
  const dung = row !== null && Number(row.ban) !== 1 && typeof row.hash === 'string'
    && maQuenMkDung(userId, row.hash, ma)
  if (!dung || !row) {
    await ghiLanLam(event, 'dat_lai_mk_sai')
    ghiSaiMa(username)
    throw apiLoi(400, LOI_MA)
  }

  try {
    // Chỉ đổi nếu hash vẫn là hash đã sinh ra mã (hai request cùng mã chạy song song: chỉ một cái đổi được)
    const kq = await exec(acc, 'UPDATE team_user SET `password` = PASSWORD(?) WHERE id = ? AND `password` = ?',
      [password, userId, row.hash])
    if (kq.affectedRows !== 1) throw apiLoi(400, LOI_MA)
  } catch (e: any) {
    if (e?.statusCode) throw e
    console.error('Dat lai mat khau update failed:', e?.code ?? e)
    throw apiLoi(503, 'Hệ thống đang bận. Vui lòng thử lại sau.')
  }
  return { ok: true }
})
