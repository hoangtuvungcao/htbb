/**
 * Sửa MỘT tên: { loai: 'npc' | 'map', id, ten, chucVu? }. Ghi `sua_boi`, `sua_luc = NOW()` (game server thấy chữ ký
 * bảng đổi và tự nạp lại trong ≤ 30 giây), ghi nhật ký quản trị (tên cũ → tên mới). Bản đồ chưa có dòng (ví dụ một
 * bản sao theo khu như 2424) thì THÊM dòng riêng.
 */
export default defineAdminHandler(async (event, admin) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const loai = str(body?.loai)
  const id = int(body?.id, -1)
  const ten = str(body?.ten).trim()
  if (loai !== 'npc' && loai !== 'map') throw apiLoi(400, 'Không hiểu loại (npc / map).')
  if (id < 0 || id > 65535) throw apiLoi(400, 'Id không hợp lệ.')
  const loi = kiemTenHien(ten)
  if (loi) throw apiLoi(400, loi)
  const pool = dbGame()
  if (!(await coBang(pool, BANG_TEN_NPC)) || !(await coBang(pool, BANG_TEN_MAP))) throw apiLoi(409, 'CSDL game chưa có bảng data_npc / data_map_ten — chạy vps/nang_cap_csdl.sh (sql/nang_cap.sql) trước.')
  if (loai === 'npc') {
    const chucVu = str(body?.chucVu).trim().slice(0, 64)
    const cu = await q1<{ ten: string, chuc_vu: string }>(pool, `SELECT ten, chuc_vu FROM ${BANG_TEN_NPC} WHERE type = ?`, [id])
    if (!cu) throw apiLoi(404, `Không có NPC type ${id}.`)
    await exec(pool, `UPDATE ${BANG_TEN_NPC} SET ten = ?, chuc_vu = ?, sua_boi = ?, sua_luc = NOW() WHERE type = ?`, [ten, chucVu || cu.chuc_vu, admin.username, id])
    await ghiNhatKy(event, 'ten_npc_sua', `npc ${id}`, `${cu.ten} → ${ten}`)
    return { ok: true, message: `Đã đổi NPC ${id}: ${cu.ten} → ${ten}. Máy chủ tự nạp trong 30 giây; tên NPC tĩnh hiện trong game sau khi phát hành lại client.` }
  }
  const cu = await q1<{ ten: string }>(pool, `SELECT ten FROM ${BANG_TEN_MAP} WHERE map_id = ?`, [id])
  if (cu) await exec(pool, `UPDATE ${BANG_TEN_MAP} SET ten = ?, sua_boi = ?, sua_luc = NOW() WHERE map_id = ?`, [ten, admin.username, id])
  else await exec(pool, `INSERT INTO ${BANG_TEN_MAP} (map_id, ten, sua_boi, sua_luc) VALUES (?, ?, ?, NOW())`, [id, ten, admin.username])
  xoaBoNhoTenHien()
  await ghiNhatKy(event, 'ten_map_sua', `map ${id}`, `${cu?.ten ?? '(chưa có dòng)'} → ${ten}`)
  return { ok: true, message: `Đã đổi bản đồ ${id}: ${cu?.ten ?? '(mới)'} → ${ten}. Máy chủ tự nạp trong 30 giây; người chơi thấy ở lần vào bản đồ kế tiếp.` }
})
