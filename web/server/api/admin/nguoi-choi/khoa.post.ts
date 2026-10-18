/**
 * Khoá tài khoản có lý do + thời hạn. Body { username, lyDo (3–300 ký tự), soGio (0 = vĩnh viễn, tối đa 10 năm) }.
 * team_user.ban = 1 — login server đọc cột này ở LẦN ĐĂNG NHẬP SAU; người đang online chơi tiếp tới khi thoát
 * (game không có lệnh đá người chơi để web gọi). Hết hạn: server/plugins/mo-khoa-het-han.ts tự mở.
 * Khoá mới THAY THẾ lần khoá đang hiệu lực (đóng dòng cũ) để lịch sử không có hai hạn chồng nhau.
 */
import { hsSoGio } from '../../../utils/hoSoNguoiChoi'

export default defineAdminHandler(async (event, admin) => {
  const b = await bodyObj(event)
  const u = ncTenTaiKhoan(b.username)
  const lyDo = chuoi(b.lyDo, 'Lý do', 300, 3)
  const soGio = hsSoGio(b.soGio, 24 * 3650)
  if (soGio === null) throw apiLoi(400, 'Thời hạn không hợp lệ (số giờ nguyên, 0 = vĩnh viễn, tối đa 10 năm).')
  const tk = await q1<{ id: number, username: string, ban: number }>(dbAccount(), 'SELECT id, username, ban FROM team_user WHERE username = ? LIMIT 1', [u])
  if (!tk) throw apiLoi(404, `Không có tài khoản "${u}".`)
  const ten = String(tk.username)
  if (ten.toLowerCase() === admin.username.toLowerCase() || await ncLaQuanTri(ten)) throw apiLoi(403, 'Không khoá được tài khoản quản trị.')
  const hetHan = soGio > 0 ? gioSau(soGio) : null
  const coBangKhoa = await coBangCms('web_khoa_tai_khoan')
  const cu = coBangKhoa
    ? await q1<{ id: number, het_han: string | null, ly_do: string }>(dbGame(), "SELECT id, het_han, ly_do FROM web_khoa_tai_khoan WHERE username = ? AND loai = 'khoa' AND mo_luc IS NULL ORDER BY id DESC LIMIT 1", [ten])
    : null
  await exec(dbAccount(), 'UPDATE team_user SET ban = 1 WHERE id = ?', [tk.id])
  if (coBangKhoa) {
    await exec(dbGame(), "UPDATE web_khoa_tai_khoan SET mo_luc = NOW() WHERE username = ? AND loai = 'khoa' AND mo_luc IS NULL", [ten])
    await exec(dbGame(), "INSERT INTO web_khoa_tai_khoan (username, loai, ly_do, het_han, admin_user, tao_luc) VALUES (?, 'khoa', ?, ?, ?, NOW())", [ten, lyDo, hetHan, admin.username])
  }
  await ghiThayDoi(event, 'khoa_tai_khoan', `tk:${ten}`,
    { ban: Number(tk.ban), khoaCu: cu ? { hetHan: cu.het_han ?? 'vĩnh viễn', lyDo: cu.ly_do } : null },
    { ban: 1, lyDo, hetHan: hetHan ?? 'vĩnh viễn' })
  return { ok: true, message: `Đã khoá ${ten}${hetHan ? ` tới ${hetHan}` : ' vĩnh viễn'}. Người đang online chơi tiếp tới khi thoát.` }
})
