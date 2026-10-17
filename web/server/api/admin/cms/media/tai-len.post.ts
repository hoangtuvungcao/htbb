/** Tải ảnh lên (multipart, trường "file", nhiều file được). Chỉ PNG/JPEG/GIF/WebP ≤ 5 MB. */
import { randomBytes } from 'node:crypto'
import { mkdir, writeFile } from 'node:fs/promises'
import { join } from 'node:path'

export default defineAdminHandler(async (event, admin) => {
  await canBangCms('web_media')
  const ds = await readMultipartFormData(event)
  const tep = (ds ?? []).filter(p => p.name === 'file' && p.data?.length)
  if (!tep.length) throw apiLoi(400, 'Chưa chọn file ảnh.')
  if (tep.length > 20) throw apiLoi(400, 'Tối đa 20 ảnh mỗi lần.')
  const dir = thuMucTaiLen()
  await mkdir(dir, { recursive: true })
  const ra: { url: string, ten: string }[] = []
  for (const p of tep) {
    if (p.data.length > MEDIA_TOI_DA) throw apiLoi(400, `"${p.filename}" lớn hơn 5 MB.`)
    const a = nhanDienAnh(p.data)
    if (!a) throw apiLoi(400, `"${p.filename}" không phải ảnh PNG/JPEG/GIF/WebP.`)
    const ten = `${randomBytes(12).toString('hex')}.${a.duoi}`
    await writeFile(join(dir, ten), p.data, { flag: 'wx' })
    await exec(dbGame(), 'INSERT INTO web_media (ten_file, ten_goc, mime, kich_thuoc, rong, cao, tai_boi, tai_luc) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())',
      [ten, String(p.filename ?? '').slice(0, 200), a.mime, p.data.length, a.rong, a.cao, admin.username])
    ra.push({ url: `/uploads/${ten}`, ten: String(p.filename ?? ten) })
  }
  await ghiNhatKy(event, 'cms_tai_anh', ra.map(r => r.url).join(' ').slice(0, 100), `${ra.length} ảnh`)
  return { ok: true, ds: ra }
})
