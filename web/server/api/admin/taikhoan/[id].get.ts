import { hsAnEmail } from '../../../utils/hoSoNguoiChoi'
/** Chi tiết một tài khoản + nhân vật + trạng thái kích hoạt (trang /admin/taikhoan?id=...). */
export default defineAdminHandler(async (event, admin) => {
  const id = int(getRouterParam(event, 'id'))
  if (!Number.isSafeInteger(id)) throw apiLoi(404, 'Không tìm thấy tài khoản.')
  // Liệt kê cột thay vì SELECT *: team_user có cột password (bản băm) — tuyệt đối không
  // được gửi xuống trình duyệt, kể cả cho admin.
  const tk = id > 0
    ? await q1(dbAccount(),
        'SELECT id, username, email, phone, regdate, ban, provider, fromgame FROM team_user WHERE id = ? LIMIT 1', [id])
    : null
  if (!tk) throw apiLoi(404, `Không tìm thấy tài khoản #${id}.`)

  const nhanVat = await q(dbGame(),
    `SELECT id, charname, lastLv, \`class\`, dex, xp, gold, luong, luonglock, ban, del, lastLog, idserver
       FROM tob_char WHERE userId = ? ORDER BY lastLv DESC, id ASC`, [id])

  const daKichHoat = (await qv(dbGame(),
    `SELECT 1 FROM \`5h_active\`
      WHERE (username = ? OR userID = ?)
        AND time_end <> '-1'
        AND CAST(time_end AS UNSIGNED) > UNIX_TIMESTAMP()
      LIMIT 1`, [tk.username, id])) !== null

  const ten = String(tk.username)
  return {
    taiKhoan: {
      id: Number(tk.id),
      username: ten,
      email: admin.vaiTro === 'xem' ? hsAnEmail(tk.email) : (tk.email ?? ''),
      phone: admin.vaiTro === 'xem' && tk.phone ? `${String(tk.phone).slice(0, 3)}•••••` : (tk.phone ?? ''),
      regdate: tk.regdate ?? '',
      ban: Number(tk.ban),
    },
    laAdmin: laTaiKhoanAdmin(ten),
    laChinhMinh: ten.toLowerCase() === admin.username.toLowerCase(),
    daKichHoat,
    nhanVat: nhanVat.map(nv => ({
      id: Number(nv.id),
      charname: String(nv.charname),
      lastLv: Number(nv.lastLv),
      class: Number(nv.class),
      dex: dexHienTai(nv.class, nv.lastLv, nv.dex),
      gold: nv.gold,
      luong: nv.luong,
      luonglock: nv.luonglock,
      ban: Number(nv.ban),
      del: Number(nv.del),
      lastLog: nv.lastLog === null ? '' : String(nv.lastLog),
      idserver: Number(nv.idserver),
    })),
  }
})
