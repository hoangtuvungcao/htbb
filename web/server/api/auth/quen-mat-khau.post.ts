/**
 * Quên mật khẩu: cấp mật khẩu mới ngẫu nhiên và gửi tới email của tài khoản.
 * Body: { username, email } -> { message }
 *
 * Không để lộ tài khoản / email nào có thật: sai tên, sai email, tài khoản bị khoá,
 * quá hạn mức theo tài khoản... đều trả ĐÚNG câu trả lời như khi cấp lại thành công.
 *
 * Thứ tự khi khớp: sinh mật khẩu mới -> đọc bản băm cũ -> ghi bản băm mới -> gửi mail.
 * Gửi mail hỏng thì GHI LẠI bản băm cũ, người chơi vẫn đăng nhập bằng mật khẩu cũ
 * được (không bị khoá ngoài vì một lá thư không tới). Bản băm cũ chỉ nằm trong biến
 * của request này, không bao giờ trả ra ngoài hay ghi log.
 *
 * Hạn mức: theo IP (bảng web_chan_spam, đếm MỌI lần gửi form) và theo tài khoản
 * (tối đa QUEN_MK_MOI_TK_TOI_DA lần cấp lại / 24 giờ, đếm trong bộ nhớ — xem
 * server/utils/matKhau.ts).
 */
const TRA_LOI_CHUNG = 'Nếu tên tài khoản và email khớp, mật khẩu mới đã được gửi tới email của bạn. Hãy kiểm tra cả thư mục Spam.'

export default defineEventHandler(async (event) => {
  if (await biChanSpam(event, 'quen_mk', QUEN_MK_TOI_DA, QUEN_MK_TRONG_GIAY)) {
    throw apiLoi(429, 'Bạn đã yêu cầu quá nhiều lần. Vui lòng thử lại sau một giờ.')
  }
  // Đếm MỌI lần gửi (kể cả sai tên/email): đây là cửa dò tài khoản + email.
  await ghiLanLam(event, 'quen_mk')

  if (!mailDaCauHinh()) {
    throw apiLoi(503, 'Chức năng quên mật khẩu chưa được bật. Vui lòng liên hệ quản trị viên.')
  }

  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const username = str(body?.username).toLowerCase()
  const email = str(body?.email)
  if (username === '' || email === '') {
    throw apiLoi(400, 'Vui lòng nhập tên tài khoản và email.')
  }
  if (!dangKyEmailHopLe(email)) {
    throw apiLoi(400, 'Email không hợp lệ.')
  }

  const acc = dbAccount()
  let row: { id: number, username: string, email: string | null, ban: number, hash: string | null } | null
  try {
    row = await q1(
      acc,
      'SELECT id, username, email, ban, `password` AS hash FROM team_user WHERE username = ? LIMIT 1',
      [username],
    )
  } catch (e: any) {
    console.error('Quen mat khau unavailable:', e?.code ?? e)
    throw apiLoi(503, 'Hệ thống đang bận. Vui lòng thử lại sau.')
  }

  // NovaDB so '=' phân biệt hoa thường, nên so email ở đây (không phân biệt hoa thường).
  const emailTk = typeof row?.email === 'string' ? row.email.trim().toLowerCase() : ''
  const khop = row !== null && emailTk !== '' && emailTk === email.toLowerCase()
    && Number(row.ban) !== 1 && typeof row.hash === 'string'
  if (!khop || !row) {
    return { message: TRA_LOI_CHUNG }
  }
  if (quenMkTaiKhoanBiChan(username)) {
    // Không báo khác đi: báo "quá hạn mức" là xác nhận tên + email khớp.
    console.warn('[quen-mk] Tài khoản đã được cấp lại quá số lần trong 24 giờ:', username)
    return { message: TRA_LOI_CHUNG }
  }

  const userId = Math.trunc(Number(row.id))
  const hashCu = row.hash as string
  const matKhauMoi = taoMatKhauNgauNhien()

  try {
    await exec(acc, 'UPDATE team_user SET `password` = PASSWORD(?) WHERE id = ?', [matKhauMoi, userId])
  } catch (e: any) {
    console.error('Quen mat khau update failed:', e?.code ?? e)
    throw apiLoi(503, 'Hệ thống đang bận. Vui lòng thử lại sau.')
  }

  const ten = String(row.username)
  // Không chèn đường link dựng từ header Host (người gửi request tự đặt được) vào thư.
  try {
    await guiMail({
      to: String(row.email).trim(),
      subject: 'Mật khẩu mới - Khí Phách Anh Hùng',
      text: [
        'Xin chào,',
        '',
        'Bạn (hoặc ai đó) vừa yêu cầu cấp lại mật khẩu cho tài khoản Khí Phách Anh Hùng.',
        '',
        `Tên tài khoản: ${ten}`,
        `Mật khẩu mới: ${matKhauMoi}`,
        '',
        'Hãy đăng nhập website rồi đổi sang mật khẩu của riêng bạn ở trang Tài khoản (mục Bảo mật).',
        '',
        'Nếu không phải bạn yêu cầu, bạn có thể bỏ qua thư này — nhưng mật khẩu cũ không còn dùng được nữa, hãy đăng nhập bằng mật khẩu mới ở trên rồi đổi mật khẩu.',
      ].join('\n'),
      html: `<div style="font-family:Arial,Helvetica,sans-serif;font-size:15px;line-height:1.6;color:#222">
<p>Xin chào,</p>
<p>Bạn (hoặc ai đó) vừa yêu cầu cấp lại mật khẩu cho tài khoản <b>Khí Phách Anh Hùng</b>.</p>
<table style="border-collapse:collapse;margin:12px 0">
<tr><td style="padding:4px 12px 4px 0;color:#666">Tên tài khoản</td><td style="padding:4px 0"><b>${maHoaHtml(ten)}</b></td></tr>
<tr><td style="padding:4px 12px 4px 0;color:#666">Mật khẩu mới</td><td style="padding:4px 0"><b style="font-family:Consolas,monospace;font-size:17px;letter-spacing:1px">${maHoaHtml(matKhauMoi)}</b></td></tr>
</table>
<p>Hãy đăng nhập website rồi đổi sang mật khẩu của riêng bạn ở trang <b>Tài khoản</b> (mục Bảo mật).</p>
<p style="color:#666">Nếu không phải bạn yêu cầu, bạn có thể bỏ qua thư này — nhưng mật khẩu cũ không còn dùng được nữa, hãy đăng nhập bằng mật khẩu mới ở trên rồi đổi mật khẩu.</p>
</div>`,
    })
  } catch {
    // guiMail đã ghi log mã lỗi của Google. Trả lại mật khẩu cũ.
    try {
      await exec(acc, 'UPDATE team_user SET `password` = ? WHERE id = ?', [hashCu, userId])
    } catch (e: any) {
      // Hiếm: mật khẩu đã đổi mà thư không tới và không trả lại được -> ghi log để admin đặt lại.
      console.error('[quen-mk] KHÔNG trả lại được mật khẩu cũ cho tài khoản', ten, e?.code ?? e)
    }
    throw apiLoi(503, 'Chưa gửi được email lúc này, mật khẩu của bạn chưa bị đổi. Vui lòng thử lại sau.')
  }

  quenMkGhiTaiKhoan(username)
  return { message: TRA_LOI_CHUNG }
})
