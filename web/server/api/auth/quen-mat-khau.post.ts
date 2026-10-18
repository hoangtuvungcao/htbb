/**
 * Quên mật khẩu — BƯỚC 1: gửi MÃ XÁC NHẬN tới email của tài khoản (KHÔNG đổi mật khẩu).
 * Body: { username, email } -> { message }
 * Bước 2: /api/auth/dat-lai-mat-khau { username, ma, password, confirm } mới đổi.
 *
 * Trước đây khớp tên + email là ĐỔI NGAY sang mật khẩu ngẫu nhiên -> ai biết tên + email của người khác là
 * khoá được họ ra ngoài (game + web) nhiều lần mỗi ngày. Giờ không có mã trong email thì không đổi được gì.
 *
 * Không để lộ tài khoản / email nào có thật: sai tên, sai email, tài khoản bị khoá, quá hạn mức theo tài
 * khoản... đều trả ĐÚNG câu trả lời như khi gửi thành công.
 *
 * Hạn mức: theo IP (bảng web_chan_spam, đếm MỌI lần gửi form) và theo tài khoản (QUEN_MK_MOI_TK_TOI_DA lần
 * gửi mã / 24 giờ, đếm trong bộ nhớ — xem server/utils/matKhau.ts).
 */
const TRA_LOI_CHUNG = 'Nếu tên tài khoản và email khớp, mã xác nhận đã được gửi tới email của bạn (hiệu lực ít nhất 30 phút). Hãy kiểm tra cả thư mục Spam.'

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
  const ma = taoMaQuenMk(userId, row.hash as string)
  const ten = String(row.username)
  // Không chèn đường link dựng từ header Host (người gửi request tự đặt được) vào thư: gửi MÃ để nhập trên web.
  // Gửi NGẦM, không chờ: chờ SMTP (vài giây) chỉ xảy ra khi tên + email khớp -> đo thời gian trả lời là biết
  // cặp nào có thật. Gửi hỏng chỉ ghi log (guiMail đã ghi), người chơi bấm gửi lại.
  const tenSite = useHtbbConfig().site.name
  guiMail({
      to: String(row.email).trim(),
      subject: `Mã xác nhận đặt lại mật khẩu - ${tenSite}`,
      text: [
        'Xin chào,',
        '',
        `Bạn (hoặc ai đó) vừa yêu cầu đặt lại mật khẩu cho tài khoản ${tenSite}.`,
        '',
        `Tên tài khoản: ${ten}`,
        `Mã xác nhận: ${ma}`,
        '',
        'Nhập mã này cùng mật khẩu mới ở trang Quên mật khẩu trên website. Mã có hiệu lực ít nhất 30 phút và chỉ dùng được một lần.',
        '',
        'Nếu không phải bạn yêu cầu, hãy bỏ qua thư này — mật khẩu của bạn KHÔNG bị đổi.',
      ].join('\n'),
      html: `<div style="font-family:Arial,Helvetica,sans-serif;font-size:15px;line-height:1.6;color:#222">
<p>Xin chào,</p>
<p>Bạn (hoặc ai đó) vừa yêu cầu đặt lại mật khẩu cho tài khoản <b>${tenSite}</b>.</p>
<table style="border-collapse:collapse;margin:12px 0">
<tr><td style="padding:4px 12px 4px 0;color:#666">Tên tài khoản</td><td style="padding:4px 0"><b>${maHoaHtml(ten)}</b></td></tr>
<tr><td style="padding:4px 12px 4px 0;color:#666">Mã xác nhận</td><td style="padding:4px 0"><b style="font-family:Consolas,monospace;font-size:20px;letter-spacing:3px">${maHoaHtml(ma)}</b></td></tr>
</table>
<p>Nhập mã này cùng mật khẩu mới ở trang <b>Quên mật khẩu</b> trên website. Mã có hiệu lực ít nhất 30 phút và chỉ dùng được một lần.</p>
<p style="color:#666">Nếu không phải bạn yêu cầu, hãy bỏ qua thư này — mật khẩu của bạn <b>không</b> bị đổi.</p>
</div>`,
  }).catch(() => { /* guiMail đã ghi log */ })

  quenMkGhiTaiKhoan(username)
  return { message: TRA_LOI_CHUNG }
})
