/** Cấm chat / bỏ cấm. Body { charId, soGio (0 = bỏ cấm), lyDo }. Chỉ khi nhân vật offline (xem nguoiChoi.ts). */
export default defineAdminHandler(async (event, admin) => {
  const b = await bodyObj(event)
  const id = int(b.charId)
  const soGio = Math.max(0, Math.min(24 * 365, int(b.soGio)))
  const lyDo = soGio > 0 ? chuoi(b.lyDo, 'Lý do', 300, 3) : chuoi(b.lyDo, 'Lý do', 300)
  const c = await q1<{ charname: string, pInfo: string, userId: number }>(dbGame(), 'SELECT charname, pInfo, userId FROM tob_char WHERE id = ? LIMIT 1', [id])
  if (!c) throw apiLoi(404, 'Không tìm thấy nhân vật.')
  const on = await dangOnline(c.charname)
  if (on === true) throw apiLoi(409, `${c.charname} đang online — game sẽ ghi đè thay đổi. Dùng lệnh GM "camchat" trong game, hoặc thử lại khi người chơi thoát.`)
  if (on === null) throw apiLoi(409, 'Không xác định được trạng thái online (máy chủ game chưa ghi tob_vi_tri_online gần đây). Để an toàn, chỉ cấm chat khi máy chủ đang chạy và người chơi offline — hoặc dùng lệnh GM "camchat".')
  const cu = docCamChat(c.pInfo)
  const giaTri = soGio > 0 ? gioSau(soGio) : ''
  const moi = datCamChat(String(c.pInfo ?? ''), giaTri)
  if (moi === null) throw apiLoi(422, 'Dữ liệu nhân vật (pInfo) không đúng định dạng — không sửa.')
  await exec(dbGame(), 'UPDATE tob_char SET pInfo = ? WHERE id = ?', [moi, id])
  const chu = String(await qv(dbAccount(), 'SELECT username FROM team_user WHERE id = ? LIMIT 1', [c.userId]) ?? '')
  if (await coBangCms('web_khoa_tai_khoan')) {
    if (soGio > 0) await exec(dbGame(), "INSERT INTO web_khoa_tai_khoan (username, loai, ly_do, het_han, admin_user, tao_luc) VALUES (?, 'cam_chat', ?, ?, ?, NOW())", [chu, `${c.charname}: ${lyDo}`, giaTri, admin.username])
    else await exec(dbGame(), "UPDATE web_khoa_tai_khoan SET mo_luc = NOW() WHERE username = ? AND loai = 'cam_chat' AND mo_luc IS NULL", [chu])
  }
  await ghiThayDoi(event, soGio > 0 ? 'cam_chat' : 'bo_cam_chat', `nv:${c.charname}`, { camChatDen: cu }, { camChatDen: giaTri, lyDo })
  return { ok: true, message: soGio > 0 ? `Đã cấm chat ${c.charname} tới ${giaTri}.` : `Đã bỏ cấm chat ${c.charname}.` }
})
