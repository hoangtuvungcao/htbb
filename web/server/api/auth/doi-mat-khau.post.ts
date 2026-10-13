/**
 * Đổi mật khẩu của người chơi đang đăng nhập.
 * Body: { current, password, confirm } -> { ok: true }
 *
 * Tài khoản lấy từ token (id), không bao giờ từ tham số gửi lên. Phải nhập đúng
 * mật khẩu hiện tại (so bằng PASSWORD() như login server). Mật khẩu mới phải theo
 * quy tắc của login server: chỉ chữ cái không dấu + số (matKhauGameHopLe) — mật
 * khẩu có ký tự khác lưu được vào DB nhưng KHÔNG vào game được.
 *
 * Đổi xong, nếu tài khoản có email và đã cấu hình gửi mail thì gửi thư báo (không
 * chờ, gửi hỏng cũng không báo lỗi). Các phiên web đang đăng nhập vẫn giữ nguyên
 * (token không gắn với mật khẩu, tự hết hạn sau 7 ngày).
 */
export default defineEventHandler(async (event) => {
  const u = requireUser(event)

  // Chỉ đếm lần gõ SAI mật khẩu hiện tại (giống đăng nhập).
  if (await biChanSpam(event, 'doi_mk_sai', DOI_MK_SAI_TOI_DA, DOI_MK_SAI_TRONG_GIAY)) {
    throw apiLoi(429, 'Bạn đã nhập sai mật khẩu quá nhiều lần. Vui lòng thử lại sau 15 phút.')
  }

  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const s = (v: unknown) => (typeof v === 'string' ? v : '')
  const current = s(body?.current)
  const password = s(body?.password)
  const confirm = s(body?.confirm)

  if (current === '') throw apiLoi(400, 'Vui lòng nhập mật khẩu hiện tại.')
  if (!matKhauGameHopLe(password)) throw apiLoi(400, LOI_MAT_KHAU_GAME)
  if (password !== confirm) throw apiLoi(400, 'Mật khẩu nhập lại không khớp.')
  if (password === current) throw apiLoi(400, 'Mật khẩu mới phải khác mật khẩu hiện tại.')

  let tk: { email: string | null } | null
  try {
    tk = await kiemTraMatKhauHienTai(u.id, current)
    if (tk) {
      await exec(dbAccount(), 'UPDATE team_user SET `password` = PASSWORD(?) WHERE id = ?', [password, Math.trunc(u.id)])
    }
  } catch (e: any) {
    console.error('Doi mat khau unavailable:', e?.code ?? e)
    throw apiLoi(503, 'Chưa đổi được mật khẩu lúc này. Vui lòng thử lại sau.')
  }
  if (!tk) {
    await ghiLanLam(event, 'doi_mk_sai')
    throw apiLoi(400, 'Mật khẩu hiện tại không đúng.')
  }

  // Thư báo: không chờ, không làm hỏng request.
  if (tk.email && mailDaCauHinh()) {
    const ten = u.username
    guiMail({
      to: tk.email,
      subject: 'Mật khẩu đã được đổi - Khí Phách Anh Hùng',
      text: [
        'Xin chào,',
        '',
        `Mật khẩu của tài khoản ${ten} vừa được đổi trên website Khí Phách Anh Hùng.`,
        '',
        'Nếu chính bạn đổi thì không cần làm gì thêm.',
        'Nếu không phải bạn, hãy dùng chức năng Quên mật khẩu trên website để lấy lại tài khoản và liên hệ quản trị viên ngay.',
      ].join('\n'),
      html: `<div style="font-family:Arial,Helvetica,sans-serif;font-size:15px;line-height:1.6;color:#222">
<p>Xin chào,</p>
<p>Mật khẩu của tài khoản <b>${maHoaHtml(ten)}</b> vừa được đổi trên website Khí Phách Anh Hùng.</p>
<p>Nếu chính bạn đổi thì không cần làm gì thêm.</p>
<p style="color:#666">Nếu không phải bạn, hãy dùng chức năng <b>Quên mật khẩu</b> trên website để lấy lại tài khoản và liên hệ quản trị viên ngay.</p>
</div>`,
    }).catch(() => { /* guiMail đã ghi log */ })
  }

  return { ok: true }
})
