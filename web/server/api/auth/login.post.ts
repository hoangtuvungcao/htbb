/**
 * Đăng nhập website bằng tài khoản game.
 * Body: { username, password } -> { token, user }
 */

/** Số lần gõ SAI mật khẩu tối đa từ một IP, trong 15 phút. */
const DANG_NHAP_SAI_TOI_DA = 15
const DANG_NHAP_TRONG_GIAY = 900

export default defineEventHandler(async (event) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))

  // Chỉ đếm lần SAI, nên người đăng nhập đúng không bao giờ chạm hạn mức.
  if (await biChanSpam(event, 'dang_nhap_sai', DANG_NHAP_SAI_TOI_DA, DANG_NHAP_TRONG_GIAY)) {
    throw apiLoi(429, 'Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau 15 phút.')
  }

  const username = str(body?.username).toLowerCase()
  const password = typeof body?.password === 'string' ? body.password : ''
  if (username === '' || password === '') {
    throw apiLoi(400, 'Vui lòng nhập tên tài khoản và mật khẩu.')
  }

  let row: { id: number, username: string, ban: number, password: string } | null
  try {
    // So khớp mật khẩu bằng PASSWORD() giống login server của game.
    row = await q1(
      dbAccount(),
      'SELECT id, username, ban, `password` FROM team_user WHERE username = ? AND `password` = PASSWORD(?) LIMIT 1',
      [username, password],
    )
  } catch (e: any) {
    console.error('Website login unavailable:', e?.code ?? e)
    throw apiLoi(503, 'Chưa thể đăng nhập lúc này. Bạn vui lòng thử lại sau ít phút.')
  }

  if (!row) {
    await ghiLanLam(event, 'dang_nhap_sai')
    throw apiLoi(401, 'Sai tên tài khoản hoặc mật khẩu.')
  }
  if (Number(row.ban) === 1) {
    // Trang đăng nhập thấy code này thì hiện nút Zalo hỗ trợ (kênh khiếu nại khoá tài khoản).
    throw apiLoi(403, 'Tài khoản của bạn đã bị khoá. Nếu cho rằng có nhầm lẫn, hãy nhắn ban quản trị qua Zalo hỗ trợ (ghi tên tài khoản, không gửi mật khẩu).', { code: 'tai_khoan_bi_khoa' })
  }

  const user = { id: Number(row.id), username: String(row.username) }
  return { token: taoTokenNguoiChoi(user.id, user.username, row.password), user }
})
