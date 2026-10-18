/**
 * Cấm chat / bỏ cấm. Body { charId, soGio (0 = bỏ cấm, tối đa 1 năm), lyDo (bắt buộc 3–300 ký tự khi cấm) }.
 * Ghi pInfo[92] (trường lệnh GM "camchat" dùng) — CHỈ khi nhân vật chắc chắn offline: nhân vật online giữ pInfo
 * trong RAM và ghi đè khi lưu. UPDATE có điều kiện pInfo chưa đổi (nếu game vừa lưu lại giữa chừng -> báo thử lại).
 */
import { hsSoGio } from '../../../utils/hoSoNguoiChoi'

export default defineAdminHandler(async (event, admin) => {
  const b = await bodyObj(event)
  const id = ncIdNhanVat(b.charId)
  const soGio = hsSoGio(b.soGio, 24 * 365)
  if (soGio === null) throw apiLoi(400, 'Thời hạn không hợp lệ (số giờ nguyên, tối đa 1 năm).')
  const lyDo = chuoi(b.lyDo, 'Lý do', 300, soGio > 0 ? 3 : 0)
  const c = await q1<{ charname: string, pInfo: string, userId: number }>(dbGame(), 'SELECT charname, pInfo, userId FROM tob_char WHERE id = ? LIMIT 1', [id])
  if (!c) throw apiLoi(404, 'Không tìm thấy Pokémon.')
  const chu = String(await qv(dbAccount(), 'SELECT username FROM team_user WHERE id = ? LIMIT 1', [c.userId]) ?? '')
  if (soGio > 0 && chu && await ncLaQuanTri(chu)) throw apiLoi(403, 'Không cấm chat Pokémon của tài khoản quản trị.')
  await ncCanOffline(String(c.charname), soGio > 0 ? 'cấm chat' : 'bỏ cấm chat')
  const cu = docCamChat(c.pInfo)
  const giaTri = soGio > 0 ? gioSau(soGio) : ''
  const moi = datCamChat(String(c.pInfo ?? ''), giaTri)
  if (moi === null) throw apiLoi(422, 'Dữ liệu Pokémon (pInfo) không đúng định dạng — không sửa.')
  const r = await exec(dbGame(), 'UPDATE tob_char SET pInfo = ? WHERE id = ? AND pInfo = ?', [moi, id, c.pInfo])
  if (r.affectedRows !== 1) throw apiLoi(409, 'Dữ liệu Pokémon vừa thay đổi (có thể người chơi vừa vào game). Tải lại rồi thử lại.')
  if (await coBangCms('web_khoa_tai_khoan')) {
    await exec(dbGame(), "UPDATE web_khoa_tai_khoan SET mo_luc = NOW() WHERE username = ? AND loai = 'cam_chat' AND mo_luc IS NULL AND ly_do LIKE ? ESCAPE '!'",
      [chu, `${String(c.charname).replace(/[!%_]/g, x => `!${x}`)}:%`])
    if (soGio > 0) await exec(dbGame(), "INSERT INTO web_khoa_tai_khoan (username, loai, ly_do, het_han, admin_user, tao_luc) VALUES (?, 'cam_chat', ?, ?, ?, NOW())", [chu, `${c.charname}: ${lyDo}`.slice(0, 300), giaTri, admin.username])
  }
  await ghiThayDoi(event, soGio > 0 ? 'cam_chat' : 'bo_cam_chat', `nv:${c.charname}`, { camChatDen: cu }, { camChatDen: giaTri, lyDo })
  return { ok: true, message: soGio > 0 ? `Đã cấm chat ${c.charname} tới ${giaTri}.` : `Đã bỏ cấm chat ${c.charname}.` }
})
