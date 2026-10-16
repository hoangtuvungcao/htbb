/**
 * Thêm / đổi email của người chơi đang đăng nhập (email dùng cho Quên mật khẩu).
 * Body: { current, email } -> { email }
 *
 * Phải nhập đúng mật khẩu hiện tại: ai cầm được phiên đăng nhập (máy quán net quên
 * đăng xuất...) mà đổi được email là chiếm luôn được tài khoản qua Quên mật khẩu.
 * Gõ sai mật khẩu đếm chung hạn mức với Đổi mật khẩu ('doi_mk_sai').
 *
 * Đổi xong thì báo về email CŨ (nếu có, nếu đã cấu hình gửi mail) — không chờ, gửi
 * hỏng cũng không báo lỗi.
 */
export default defineEventHandler(async (event) => {
  const u = await requireUser(event)

  if (await biChanSpam(event, 'doi_mk_sai', DOI_MK_SAI_TOI_DA, DOI_MK_SAI_TRONG_GIAY)) {
    throw apiLoi(429, 'Bạn đã nhập sai mật khẩu quá nhiều lần. Vui lòng thử lại sau 15 phút.')
  }

  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const current = typeof body?.current === 'string' ? body.current : ''
  const email = str(body?.email)

  if (current === '') throw apiLoi(400, 'Vui lòng nhập mật khẩu hiện tại.')
  if (email === '') throw apiLoi(400, 'Vui lòng nhập email.')
  if (!dangKyEmailHopLe(email)) throw apiLoi(400, 'Email không hợp lệ.')

  let tk: { email: string | null } | null
  try {
    tk = await kiemTraMatKhauHienTai(u.id, current)
    if (tk) {
      await exec(dbAccount(), 'UPDATE team_user SET email = ? WHERE id = ?', [email, Math.trunc(u.id)])
    }
  } catch (e: any) {
    console.error('Doi email unavailable:', e?.code ?? e)
    throw apiLoi(503, 'Chưa lưu được email lúc này. Vui lòng thử lại sau.')
  }
  if (!tk) {
    await ghiLanLam(event, 'doi_mk_sai')
    throw apiLoi(400, 'Mật khẩu hiện tại không đúng.')
  }

  const emailCu = tk.email
  if (emailCu && emailCu.toLowerCase() !== email.toLowerCase() && mailDaCauHinh()) {
    const ten = u.username
    const tenSite = useKpahConfig().site.name
    guiMail({
      to: emailCu,
      subject: `Email tài khoản đã được đổi - ${tenSite}`,
      text: [
        'Xin chào,',
        '',
        `Email của tài khoản ${ten} vừa được đổi sang địa chỉ khác trên website ${tenSite}.`,
        'Từ nay thư lấy lại mật khẩu sẽ gửi tới địa chỉ mới, không gửi về địa chỉ này nữa.',
        '',
        'Nếu không phải bạn đổi, hãy liên hệ quản trị viên ngay.',
      ].join('\n'),
      html: `<div style="font-family:Arial,Helvetica,sans-serif;font-size:15px;line-height:1.6;color:#222">
<p>Xin chào,</p>
<p>Email của tài khoản <b>${maHoaHtml(ten)}</b> vừa được đổi sang địa chỉ khác trên website ${tenSite}.</p>
<p>Từ nay thư lấy lại mật khẩu sẽ gửi tới địa chỉ mới, không gửi về địa chỉ này nữa.</p>
<p style="color:#666">Nếu không phải bạn đổi, hãy liên hệ quản trị viên ngay.</p>
</div>`,
    }).catch(() => { /* guiMail đã ghi log */ })
  }

  return { email }
})
