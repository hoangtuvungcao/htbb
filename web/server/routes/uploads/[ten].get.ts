/** Phục vụ ảnh đã tải lên. Tên phải đúng dạng file do server tạo (chống ../ và file lạ). */
import { readFile } from 'node:fs/promises'
import { join } from 'node:path'

export default defineEventHandler(async (event) => {
  const ten = String(getRouterParam(event, 'ten') ?? '')
  const m = ten.match(/^[a-f0-9]{24}\.(png|jpg|gif|webp)$/)
  if (!m) throw createError({ statusCode: 404 })
  try {
    const buf = await readFile(join(thuMucTaiLen(), ten))
    setHeader(event, 'Content-Type', MIME_THEO_DUOI[m[1]!]!)
    setHeader(event, 'Cache-Control', 'public, max-age=31536000, immutable')
    setHeader(event, 'X-Content-Type-Options', 'nosniff')
    setHeader(event, 'Content-Security-Policy', "default-src 'none'")
    return buf
  }
  catch {
    throw createError({ statusCode: 404 })
  }
})
