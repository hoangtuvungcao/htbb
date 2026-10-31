/**
 * Huỷ một yêu cầu đổi lượng còn chờ của chính tài khoản: { id }. Chỉ huỷ được dòng CHỜ / CHỜ VÀO GAME (huyLenh dùng
 * UPDATE có điều kiện); dòng game đã chốt (đang xử lý / xong) thì không — game và web không thể cùng thắng.
 */
import { DOI_LOAI_LENH } from '../../utils/doiLuongDb'
import { huyLenh } from '../../utils/lenhNhanVat'

export default defineEventHandler(async (event) => {
  const u = await requireUser(event)
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const id = int(body?.id, 0)
  if (id <= 0) throw apiLoi(400, 'Yêu cầu không hợp lệ.')
  const db = dbGame()
  const r = await q1<{ char_id: number, loai: string }>(db,
    `SELECT l.char_id, l.loai FROM web_lenh_nhan_vat l JOIN tob_char c ON c.id = l.char_id
      WHERE l.id = ? AND c.userId = ? AND l.loai IN (?, ?) LIMIT 1`, [id, Math.trunc(u.id), ...DOI_LOAI_LENH])
  if (!r) throw apiLoi(404, 'Không tìm thấy yêu cầu này.')
  const ok = await huyLenh(db, id, `nguoi_choi:${u.username}`, { charId: Math.trunc(Number(r.char_id)), loai: String(r.loai) })
  if (!ok) throw apiLoi(409, 'Yêu cầu đã được máy chủ xử lý, không huỷ được nữa.')
  return { ok: true }
})
