/**
 * Danh sách / tìm tài khoản cho trang Tài khoản.
 *   ?q=   tìm theo tên, email, số điện thoại hoặc ID (tối đa 50 dòng)
 *   ?trang=  phân trang danh sách đầy đủ, 30 dòng/trang
 */
export default defineAdminHandler(async (event, admin) => {
  const query = getQuery(event)
  const qs = str(query.q)
  // Kẹp trên để OFFSET luôn là số nguyên bình thường (số quá lớn sẽ thành dạng 1e+21,
  // ghép vào SQL là lỗi cú pháp).
  const trang = Math.min(1_000_000, Math.max(1, int(query.trang, 1)))
  let ketQua: { id: number, username: string, email: string | null, phone: string, regdate: string | null, ban: number }[]
  let conTrangSau = false

  if (qs !== '') {
    if (/^\d+$/.test(qs)) {
      ketQua = await q(dbAccount(),
        `SELECT id, username, email, phone, regdate, ban FROM team_user
          WHERE id = ? OR username = ? LIMIT ${TK_TIM_TOI_DA}`, [Number(qs), qs])
    } else {
      const nhu = `%${qs}%`
      ketQua = await q(dbAccount(),
        `SELECT id, username, email, phone, regdate, ban FROM team_user
          WHERE username LIKE ? OR email LIKE ? OR phone LIKE ?
          ORDER BY id DESC LIMIT ${TK_TIM_TOI_DA}`, [nhu, nhu, nhu])
    }
  } else {
    // Lấy dư 1 dòng để biết còn trang sau hay không, không COUNT(*) riêng.
    const lay = Math.trunc(TK_MOI_TRANG + 1)
    const bo = Math.trunc((trang - 1) * TK_MOI_TRANG)
    ketQua = await q(dbAccount(),
      `SELECT id, username, email, phone, regdate, ban FROM team_user
        ORDER BY id DESC LIMIT ${lay} OFFSET ${bo}`)
    if (ketQua.length > TK_MOI_TRANG) {
      conTrangSau = true
      ketQua.pop()
    }
  }

  const minh = admin.username.toLowerCase()
  return {
    q: qs,
    trang,
    conTrangSau,
    gioiHan: qs !== '' && ketQua.length >= TK_TIM_TOI_DA,
    ketQua: ketQua.map(r => ({
      id: Number(r.id),
      username: String(r.username),
      email: r.email ?? '',
      phone: r.phone ?? '',
      regdate: r.regdate ?? '',
      ban: Number(r.ban),
      laAdmin: laTaiKhoanAdmin(String(r.username)),
      laChinhMinh: String(r.username).toLowerCase() === minh,
    })),
  }
})
