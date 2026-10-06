/**
 * Đăng ký tài khoản.
 * Body: { username, password, confirm, email? } -> { username, autoActivate }
 *
 * Không có token chống CSRF: đăng nhập bằng bearer token trong header nên trình
 * duyệt không tự gửi kèm phiên (xem server/utils/token.ts). Thứ chặn script tạo
 * tài khoản hàng loạt là hạn mức theo IP bên dưới — mỗi lần gọi thành công là một
 * dòng mới trong account.team_user.
 *
 * Lỗi nhập liệu trả 400 với `data.errors` (danh sách) để trang hiện đủ từng dòng.
 */
export default defineEventHandler(async (event) => {
  if (await biChanSpam(event, 'dang_ky', DANG_KY_TOI_DA, DANG_KY_TRONG_GIAY)) {
    throw apiLoi(429, 'Bạn đã tạo quá nhiều tài khoản từ địa chỉ này. Vui lòng thử lại sau một giờ.')
  }

  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const d = dangKyChuanHoa(body)
  const errors = dangKyKiemTra(d)
  if (errors.length) {
    throw apiLoi(400, errors.join(' '), { errors })
  }

  let userId: number
  try {
    const acc = dbAccount()
    // Kiểm tra trùng tên
    if (await qv(acc, DANG_KY_TRUNG_TEN_SQL, [d.username]) !== null) {
      const msg = 'Tên tài khoản đã tồn tại, vui lòng chọn tên khác.'
      throw apiLoi(409, msg, { errors: [msg] })
    }
    // Lưu mật khẩu bằng PASSWORD() để KHỚP với login server của game
    const res = await exec(acc, DANG_KY_INSERT_SQL, [d.username, d.password, d.email !== '' ? d.email : null])
    userId = Number(res.insertId)
  } catch (e: any) {
    if (e?.statusCode && e.statusCode < 500) throw e
    // Trùng khoá UNIQUE khi hai người gửi cùng lúc: báo như trùng tên.
    if (e?.code === 'ER_DUP_ENTRY') {
      const msg = 'Tên tài khoản đã tồn tại, vui lòng chọn tên khác.'
      throw apiLoi(409, msg, { errors: [msg] })
    }
    console.error('Register unavailable:', e?.code ?? e)
    // Người gọi chưa đăng nhập: không nói gì về cấu hình hay loại database.
    const msg = 'Hệ thống tạm thời chưa tạo được tài khoản. Vui lòng thử lại sau.'
    throw apiLoi(503, msg, { errors: [msg] })
  }

  // Chỉ đếm khi tạo THÀNH CÔNG: gõ trùng tên vài lần là chuyện bình thường của
  // người dùng thật, không nên tính vào hạn mức.
  await ghiLanLam(event, 'dang_ky')

  // Tài khoản đã tạo xong thì không báo lỗi nữa dù kích hoạt hỏng; chỉ báo đúng
  // trạng thái để trang không nói "đã kích hoạt" khi thực ra chưa (người chơi sẽ
  // được nhắc liên hệ quản trị viên).
  const autoActivate = useKpahConfig().site.autoActivate
    ? await activateAccount(userId, d.username)
    : false

  // Trang hiện kết quả ngay tại chỗ, không chuyển trang kèm thông báo.
  return { username: d.username, autoActivate }
})
