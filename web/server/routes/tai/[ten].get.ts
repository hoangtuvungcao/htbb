/** Phục vụ tệp game đã tải lên (kho server/utils/tepGame.ts). Tên phải đúng dạng an toàn — chống ../ và tệp ẩn. */
import { createReadStream } from 'node:fs'
import { stat } from 'node:fs/promises'
import { join } from 'node:path'

const LOAI: Record<string, string> = {
  apk: 'application/vnd.android.package-archive',
  jar: 'application/java-archive',
  jad: 'text/vnd.sun.j2me.app-descriptor; charset=utf-8',
  plist: 'application/xml',
  ipa: 'application/octet-stream',
}

export default defineEventHandler(async (event) => {
  const ten = tenTepDaLuu(String(getRouterParam(event, 'ten') ?? ''))
  if (!ten) throw createError({ statusCode: 404 })
  const duong = join(thuMucTepGame(), ten)
  const s = await stat(duong).catch(() => null)
  if (!s?.isFile()) throw createError({ statusCode: 404 })
  const duoi = ten.split('.').pop()!.toLowerCase()
  setHeader(event, 'Content-Type', LOAI[duoi] ?? 'application/octet-stream')
  setHeader(event, 'Content-Length', String(s.size))
  setHeader(event, 'X-Content-Type-Options', 'nosniff')
  setHeader(event, 'Content-Security-Policy', "default-src 'none'")
  setHeader(event, 'Cache-Control', 'public, max-age=300')
  // manifest .plist phải mở được trực tiếp cho itms-services; còn lại tải về
  if (duoi !== 'plist' && duoi !== 'jad') setHeader(event, 'Content-Disposition', `attachment; filename="${ten}"`)
  if (event.method === 'HEAD') return ''
  return sendStream(event, createReadStream(duong))
})
