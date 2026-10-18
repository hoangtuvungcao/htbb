/**
 * Đăng nhập quản trị — Bước MỘT: tên tài khoản + mật khẩu.
 *
 * Body: { username, password }
 *  -> { token, admin }                              vào luôn (chưa ghi danh 2FA, không bắt buộc)
 *  -> { need2fa: true, pending, username, conLai }  đúng mật khẩu, còn phải gõ mã hai bước
 *
 * Không có CSRF: phiên là token trong header Authorization, không phải cookie.
 * Mọi lần sai đều ghi vào web_admin_log (dang_nhap_that_bai) và bị đếm bởi
 * dangBiChanDoSai() — cùng bộ đếm với bước hai.
 */
import { join } from 'node:path'
import { accessSync, constants, statSync } from 'node:fs'

/**
 * Đoán vì sao kết nối database hỏng, để log của server nói được điều gì đó hữu
 * ích thay vì chỉ một mã 1045. Hay gặp nhất: server.ini CÓ ở đó nhưng web không
 * có quyền đọc, nên lặng lẽ rơi xuống mặc định root / mật khẩu rỗng — và MySQL
 * báo "Access denied for user 'root'".
 *
 * CHỈ ghi ra log, không trả cho trình duyệt: người gọi API này chưa đăng nhập,
 * và đường dẫn trên máy chủ hay chuyện "DB đang dùng root, mật khẩu rỗng" là
 * thông tin quý cho kẻ tấn công.
 */
function goiYLoiKetNoi(): string {
  const c = useHtbbConfig()
  const f = join(c.serverDir, 'server.ini')
  let laFile = false
  try {
    laFile = statSync(f).isFile()
  } catch { /* không có */ }
  if (laFile) {
    try {
      accessSync(f, constants.R_OK)
    } catch {
      return `Có file ${f} nhưng web không có quyền đọc, nên đang dùng thông tin mặc định. `
        + 'Cho người chạy web đọc được file đó (usermod -a -G kpah <người chạy web> rồi khởi động lại), '
        + 'hoặc khai KPAH_DB_USER/KPAH_DB_PASS trong biến môi trường.'
    }
  }
  if (!laFile && c.db.user === 'root' && c.db.pass === '') {
    return `Không thấy ${f} và biến môi trường cũng không khai KPAH_DB_USER/KPAH_DB_PASS, `
      + 'nên đang dùng mặc định root với mật khẩu rỗng. Khai thông tin thật trong biến môi trường.'
  }
  return ''
}

/** Lỗi cơ sở dữ liệu lúc đăng nhập: chi tiết vào log, trình duyệt chỉ nhận câu chung. */
function loiCsdl(buoc: string, e: any): never {
  // Khối gọi hàm này bắt CẢ hai loại lỗi, và chúng cần cách sửa khác hẳn nhau:
  //   - không kết nối được  -> MySQL chưa chạy, hoặc sai KPAH_DB_USER/KPAH_DB_PASS
  //   - kết nối được nhưng câu lệnh hỏng -> hay gặp nhất là máy chủ dùng MySQL 8,
  //     nơi hàm PASSWORD() đã bị gỡ (login server cũng sẽ hỏng theo)
  // Ghi nguyên văn + gợi ý ra log của server để người sửa không đi nhầm hướng.
  const goiY = goiYLoiKetNoi()
  console.error(`[htbb-admin] dang nhap that bai (${buoc}):`, e?.code ?? '', e?.message ?? e, goiY ? `\n  goi y: ${goiY}` : '')
  throw apiLoi(503, 'Cơ sở dữ liệu đang lỗi nên chưa đăng nhập được. Chi tiết và gợi ý cách sửa nằm trong log của web server.')
}

export default defineEventHandler(async (event) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))

  if (await dangBiChanDoSai(event)) {
    throw apiLoi(429, 'Đã gõ sai quá nhiều lần. Chờ 15 phút rồi thử lại.')
  }

  const tenDaGo = str(body?.username).toLowerCase()
  const matKhau = typeof body?.password === 'string' ? body.password : ''

  if (tenDaGo === '' || matKhau === '') {
    throw apiLoi(400, 'Nhập đủ tên tài khoản và mật khẩu.')
  }
  if (!(await coAiLaAdmin())) {
    throw apiLoi(403, 'Chưa cấu hình KPAH_ADMIN_ACCOUNTS nên trang quản trị đang đóng với tất cả mọi người.')
  }

  let row: { id: number, username: string, ban: number } | null
  try {
    // So khớp mật khẩu bằng PASSWORD() y hệt login server của game
    // (CheckLoginSocket: select * from team_user where username=... and password=password(...)).
    // Chỉ lấy id, username, ban — cột mật khẩu không bao giờ rời khỏi MySQL.
    row = await q1(
      dbAccount(),
      'SELECT id, username, ban FROM team_user WHERE username = ? AND `password` = PASSWORD(?) LIMIT 1',
      [tenDaGo, matKhau],
    )
  } catch (e: any) {
    loiCsdl('doc team_user', e)
  }

  if (!row) {
    await ghiNhatKyDangNhapThatBai(event, tenDaGo, 'sai tên hoặc mật khẩu')
    throw apiLoi(401, 'Sai tên tài khoản hoặc mật khẩu.')
  }
  if (!(await vaiTroCua(String(row.username)))) {
    // Mật khẩu ĐÚNG nhưng không nằm trong danh sách admin — đáng ghi lại.
    await ghiNhatKyDangNhapThatBai(event, tenDaGo, 'đúng mật khẩu nhưng không có quyền admin')
    throw apiLoi(403, 'Tài khoản này không có quyền vào trang quản trị.')
  }
  if (Number(row.ban) === 1) {
    await ghiNhatKyDangNhapThatBai(event, tenDaGo, 'tài khoản admin đang bị khoá')
    throw apiLoi(403, 'Tài khoản này đang bị khoá.')
  }

  const ten = String(row.username)
  // Không đọc được bảng web_admin_2fa thì TỪ CHỐI, không coi là "chưa bật hai
  // bước": nếu không, MySQL trục trặc một lúc là đủ để vào chỉ bằng mật khẩu.
  let daGhiDanh: boolean
  let conLai = 0
  try {
    daGhiDanh = await hbDaGhiDanh(ten)
    if (daGhiDanh) conLai = await hbSoMaDuPhongConLai(ten)
  } catch (e: any) {
    loiCsdl('doc web_admin_2fa', e)
  }

  if (daGhiDanh) {
    // Mật khẩu đúng, nhưng CHƯA vào được. Token chờ này không phải token admin:
    // requireAdmin() chỉ nhận loại 'admin', nên mọi API khác vẫn trả 401.
    return {
      need2fa: true as const,
      pending: taoToken('cho2fa', Number(row.id), ten, HB_CHO_2FA_HAN),
      username: ten,
      conLai,
    }
  }
  if (hbBatBuocTatCa()) {
    // KPAH_ADMIN_2FA_BAT_BUOC = 1: đúng mật khẩu vẫn không đủ.
    await ghiNhatKyDangNhapThatBai(event, tenDaGo, 'chưa ghi danh hai bước mà đang bắt buộc')
    throw apiLoi(403, 'Tài khoản này chưa bật xác thực hai bước, và máy chủ đang bắt buộc. '
      + `Ghi danh trên máy chủ trước: node scripts/tao-ma-2buoc.mjs ${tenDaGo}`)
  }
  return await hbVaoDuoc(event, { id: Number(row.id), username: ten }, 'chỉ mật khẩu — CHƯA bật hai bước')
})
