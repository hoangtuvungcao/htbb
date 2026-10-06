/**
 * Đăng nhập trang xác nhận nạp tiền thủ công /xac-nhan-nap bằng mật khẩu chung.
 * Body: { password } -> { token }
 */

/** Gõ sai quá 10 lần / 15 phút từ một IP thì chặn, để không dò được mật khẩu chung. */
const SAI_TOI_DA = 10
const SAI_TRONG_GIAY = 900

export default defineEventHandler(async (event) => {
  if (!napMocTrangAdminMo()) napMocDongTrangAdmin()

  if (await biChanSpam(event, 'naptien_sai_mk', SAI_TOI_DA, SAI_TRONG_GIAY)) {
    throw apiLoi(429, 'Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau 15 phút.')
  }
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const password = typeof body?.password === 'string' ? body.password : ''

  if (!napMocSoKhoa(useKpahConfig().nap.adminNaptienPassword, password)) {
    await ghiLanLam(event, 'naptien_sai_mk')
    throw apiLoi(401, 'Sai mật khẩu.')
  }
  return { token: napMocTaoTokenTrangAdmin() }
})
