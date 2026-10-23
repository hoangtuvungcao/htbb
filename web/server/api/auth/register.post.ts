/**
 * Đăng ký tài khoản.
 * Body: { username, password, confirm, email } -> { username, autoActivate }
 * Email bắt buộc: là đường duy nhất để lấy lại mật khẩu (trang Quên mật khẩu).
 *
 * Không có token chống CSRF: đăng nhập bằng bearer token trong header nên trình
 * duyệt không tự gửi kèm phiên (xem server/utils/token.ts). Thứ chặn script tạo
 * tài khoản hàng loạt là hạn mức theo IP bên dưới — mỗi lần gọi thành công là một
 * dòng mới trong account.team_user. Hạn mức và quy tắc nhập liệu dùng CHUNG với đăng ký
 * trong game (lệnh 39, HTBB/src/real/cmd/DangKyTrongGame.java): shared/data/quy-tac-dang-ky.json.
 * Số tối đa / giờ / 24 giờ chỉnh được trên trang Cấu hình hệ thống (cau_hinh dang_ky.toi_da_gio / toi_da_ngay);
 * JSON chỉ là mặc định khi vắng dòng.
 *
 * Lỗi nhập liệu trả 400 với `data.errors` (danh sách) để trang hiện đủ từng dòng.
 */
export default defineEventHandler(async (event) => {
  // Công tắc dang_ky.web.bat (trang Cấu hình hệ thống, áp dụng ≤ 5 giây). Đọc lỗi = vẫn cho đăng ký (mặc định).
  if (!(await chCo('dang_ky.web.bat', true))) {
    const msg = 'Máy chủ đang tạm ngừng nhận đăng ký tài khoản mới. Bạn vui lòng quay lại sau.'
    throw apiLoi(403, msg, { errors: [msg] })
  }
  // Kiểm nhập liệu TRƯỚC hạn mức: form sai không chạm tới bộ đếm (kể cả chỗ giữ trong bộ nhớ).
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const d = dangKyChuanHoa(body)
  const errors = dangKyKiemTra(d)
  if (errors.length) {
    throw apiLoi(400, errors.join(' '), { errors })
  }

  // Hạn mức chung với đăng ký trong game, đếm gộp cả hai đường. Số tối đa: cau_hinh dang_ky.toi_da_gio /
  // dang_ky.toi_da_ngay (trang Cấu hình hệ thống), vắng = hanMuc của shared/data/quy-tac-dang-ky.json.
  // dangKyBiChan GIỮ CHỖ trong bộ nhớ: mọi đường không tạo được tài khoản phải trả (finally bên dưới) — không thì
  // bấm lại ngay sau một lần lỗi (trùng tên, lỗi CSDL) bị tính như đã tạo thêm một tài khoản.
  let daGhi = false
  let userId = 0
  try {
    const chan = await dangKyBiChan(event)
    if (chan) {
      const msg = dangKyLoiHanMuc(chan)
      throw apiLoi(429, msg, { errors: [msg] })
    }

    try {
      const acc = dbAccount()
      // Kiểm tra trùng tên
      if (await qv(acc, DANG_KY_TRUNG_TEN_SQL, [d.username]) !== null) {
        const msg = dangKyThongBao('trungTen')
        throw apiLoi(409, msg, { errors: [msg] })
      }
      // Lưu mật khẩu bằng PASSWORD() để KHỚP với login server của game. Email đã
      // qua dangKyKiemTra nên luôn có giá trị hợp lệ.
      const res = await exec(acc, DANG_KY_INSERT_SQL, [d.username, d.password, d.email])
      userId = Number(res.insertId)
    } catch (e: any) {
      if (e?.statusCode && e.statusCode < 500) throw e
      // Trùng khoá UNIQUE khi hai người gửi cùng lúc: báo như trùng tên.
      if (e?.code === 'ER_DUP_ENTRY') {
        const msg = dangKyThongBao('trungTen')
        throw apiLoi(409, msg, { errors: [msg] })
      }
      console.error('Register unavailable:', e?.code ?? e)
      // Người gọi chưa đăng nhập: không nói gì về cấu hình hay loại database.
      const msg = dangKyThongBao('loiHeThong')
      throw apiLoi(503, msg, { errors: [msg] })
    }

    // Chỉ đếm khi tạo THÀNH CÔNG: gõ trùng tên vài lần là chuyện bình thường của
    // người dùng thật, không nên tính vào hạn mức. ghiLanLam tự trả chỗ giữ.
    daGhi = true
    await ghiLanLam(event, 'dang_ky')
  } finally {
    if (!daGhi) dangKyTraCho(event)
  }
  // Tài khoản đã tạo xong thì không báo lỗi nữa dù kích hoạt hỏng; chỉ báo đúng
  // trạng thái để trang không nói "đã kích hoạt" khi thực ra chưa (người chơi sẽ
  // được nhắc liên hệ quản trị viên).
  const autoActivate = useHtbbConfig().site.autoActivate
    ? await activateAccount(userId, d.username)
    : false

  // Trang hiện kết quả ngay tại chỗ, không chuyển trang kèm thông báo.
  return { username: d.username, autoActivate }
})
