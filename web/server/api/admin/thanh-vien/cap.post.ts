/**
 * Cấp / gia hạn Thành viên bằng tay (Siêu quản trị) — một hoặc NHIỀU tài khoản (tester, đền bù).
 * Body: { tai_khoan: "a, b\nc", ngay (0 = vĩnh viễn), ly_do }. Mỗi tài khoản một dòng tv_nhat_ky + nhật ký admin.
 */
import { tachDanhSachTk } from '../../../utils/thanhVienThuan'

export default defineAdminHandler(async (event, admin) => {
  if (!(await tvCoBang())) tvThieuBang()
  const b = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const ds = tachDanhSachTk(str(b?.tai_khoan))
  const ngay = int(b?.ngay, -1)
  const lyDo = str(b?.ly_do).trim().slice(0, 200)
  if (!ds.length) throw apiLoi(400, 'Nhập ít nhất một tên tài khoản.')
  if (ngay < 0 || ngay > 36500) throw apiLoi(400, 'Số ngày: 0 (vĩnh viễn) đến 36.500.')
  if (lyDo.length < 3) throw apiLoi(400, 'Ghi lý do (ít nhất 3 ký tự).')
  const tk = await q<{ id: number, username: string }>(dbAccount(),
    `SELECT id, username FROM team_user WHERE username IN (${ds.map(() => '?').join(',')})`, ds)
  const thay = new Map(tk.map(t => [String(t.username).toLowerCase(), t]))
  const khongCo = ds.filter(x => !thay.has(x))
  let xong = 0
  for (const t of tk) {
    await tvCap(null, { userId: Number(t.id), username: String(t.username).toLowerCase(), ngay, nguon: ds.length > 1 ? 'hang_loat' : 'admin', tacNhan: `admin:${admin.username}`, lyDo, ip: ipKhach(event) })
    xong++
  }
  await ghiNhatKy(event, 'cap_thanh_vien', ds.slice(0, 5).join(',') + (ds.length > 5 ? '…' : ''), `${xong} tài khoản, ${ngay ? `${ngay} ngày` : 'vĩnh viễn'}: ${lyDo}`)
  return { ok: true, xong, khongCo, message: `Đã cấp ${xong} tài khoản${khongCo.length ? `; không có: ${khongCo.join(', ')}` : ''}.` }
})
