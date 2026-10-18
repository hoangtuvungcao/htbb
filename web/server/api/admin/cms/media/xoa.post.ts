import { unlink } from 'node:fs/promises'
import { join } from 'node:path'

export default defineAdminHandler(async (event) => {
  await canBangCms('web_media')
  const id = int((await bodyObj(event)).id)
  const r = await q1<{ ten_file: string }>(dbGame(), 'SELECT * FROM web_media WHERE id = ? LIMIT 1', [id])
  if (!r) throw apiLoi(404, 'Không tìm thấy ảnh.')
  if (/^[a-f0-9]{24}\.(png|jpg|gif|webp)$/.test(r.ten_file)) await unlink(join(thuMucTaiLen(), r.ten_file)).catch(() => {})
  await exec(dbGame(), 'DELETE FROM web_media WHERE id = ?', [id])
  await ghiThayDoi(event, 'cms_xoa_anh', `anh:${r.ten_file}`, r, null)
  return { ok: true }
})
