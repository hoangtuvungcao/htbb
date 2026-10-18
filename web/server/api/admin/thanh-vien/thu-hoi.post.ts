/** Thu hồi Thành viên (Siêu quản trị). Body: { tai_khoan, ly_do }. Không hoàn tiền — hoàn tiền làm ở /admin/naptien. */
export default defineAdminHandler(async (event, admin) => {
  if (!(await tvCoBang())) tvThieuBang()
  const b = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const ten = str(b?.tai_khoan).trim().toLowerCase()
  const lyDo = str(b?.ly_do).trim().slice(0, 200)
  if (lyDo.length < 3) throw apiLoi(400, 'Ghi lý do (ít nhất 3 ký tự).')
  const r = await q1<{ user_id: number, username: string }>(dbGame(), 'SELECT user_id, username FROM tv_thanh_vien WHERE username = ? LIMIT 1', [ten])
  if (!r) throw apiLoi(404, `"${ten}" không có trong danh sách thành viên.`)
  await tvThuHoi(null, { userId: Number(r.user_id), username: r.username, ngay: 0, nguon: 'admin', tacNhan: `admin:${admin.username}`, lyDo, ip: ipKhach(event), hoanTien: false })
  await ghiNhatKy(event, 'thu_hoi_thanh_vien', ten, lyDo)
  return { ok: true, message: `Đã thu hồi Thành viên của ${ten}.` }
})
