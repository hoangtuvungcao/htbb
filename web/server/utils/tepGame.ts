/**
 * Kho tệp game (APK / zip PC / jar / AppImage / ipa + manifest) phục vụ ở /tai/<tên> — tách khỏi thư viện
 * ảnh vì tệp lớn (tới HTBB_TAI_GAME_TOI_DA_MB, mặc định 1024 MB) và không phải ảnh.
 *   - Thư mục: HTBB_TAI_GAME_DIR (mặc định <HTBB_UPLOAD_DIR>/tai-game).
 *   - Tải lên: POST /api/admin/cms/tai-game/tai-len?ten=<tên tệp>, thân = nội dung tệp
 *     (application/octet-stream), ghi THẲNG xuống đĩa theo luồng (không giữ cả tệp trong RAM),
 *     tính SHA-256 trong lúc ghi, kiểm chữ ký byte đầu theo đuôi (shared/utils/taiGame.ts chuKyTepHopLe).
 *   - Có proxy phía trước (Apache/Nginx) thì nhớ nới giới hạn thân request cho đường dẫn này.
 */
import { createHash, randomBytes } from 'node:crypto'
import { createWriteStream } from 'node:fs'
import { mkdir, readdir, readFile, rename, stat, unlink, writeFile } from 'node:fs/promises'
import { join, resolve } from 'node:path'
import type { H3Event } from 'h3'
import { chuKyTepHopLe, tenTepGameAnToan } from '../../shared/utils/taiGame'

export function thuMucTepGame(): string {
  return resolve((process.env.HTBB_TAI_GAME_DIR ?? process.env.KPAH_TAI_GAME_DIR) || join(thuMucTaiLen(), 'tai-game')) // TUONG-THICH-TEN-CU
}

export function toiDaTepGame(): number {
  const mb = Math.trunc(Number((process.env.HTBB_TAI_GAME_TOI_DA_MB ?? process.env.KPAH_TAI_GAME_TOI_DA_MB) ?? 1024)) // TUONG-THICH-TEN-CU
  return (Number.isFinite(mb) && mb > 0 ? Math.min(mb, 4096) : 1024) * 1024 * 1024
}

export interface TepGame { ten: string, url: string, byte: number, sha256: string, luc: string }

/** Tên đã lưu hợp lệ (chống ../, tệp ẩn .tmp, .sha256). */
export function tenTepDaLuu(ten: string): string | null {
  const t = tenTepGameAnToan(ten)
  return t && t === ten ? t : null
}

export async function dsTepGame(): Promise<TepGame[]> {
  const thu = thuMucTepGame()
  let ds: string[] = []
  try { ds = await readdir(thu) }
  catch { return [] }
  const ra: TepGame[] = []
  for (const ten of ds) {
    if (!tenTepDaLuu(ten)) continue
    try {
      const s = await stat(join(thu, ten))
      if (!s.isFile()) continue
      const sha = (await readFile(join(thu, `.${ten}.sha256`), 'utf8').catch(() => '')).trim()
      ra.push({ ten, url: `/tai/${ten}`, byte: s.size, sha256: /^[0-9a-f]{64}$/.test(sha) ? sha : '', luc: s.mtime.toISOString() })
    }
    catch { /* bỏ qua */ }
  }
  return ra.sort((a, b) => b.luc.localeCompare(a.luc))
}

/** Ghi luồng thân request thành tệp. Lỗi -> apiLoi 4xx, tệp tạm bị xoá. */
export async function luuTepGameTuLuong(event: H3Event, tenGoc: string, ghiDe: boolean): Promise<TepGame> {
  const ten = tenTepGameAnToan(tenGoc)
  if (!ten) throw apiLoi(400, 'Tên tệp không hợp lệ. Đuôi được nhận: .apk .zip .jar .jad .exe .gz .tgz .AppImage .ipa .plist')
  const duoi = ten.split('.').pop()!.toLowerCase()
  const thu = thuMucTepGame()
  await mkdir(thu, { recursive: true })
  const dich = join(thu, ten)
  if (!ghiDe && await stat(dich).then(() => true, () => false)) {
    throw apiLoi(409, `Đã có tệp ${ten}. Đặt tên khác (nên kèm số phiên bản) hoặc chọn ghi đè.`)
  }
  const max = toiDaTepGame()
  const khai = Number(getHeader(event, 'content-length') ?? 0)
  if (khai > max) throw apiLoi(413, `Tệp quá lớn (tối đa ${Math.round(max / 1048576)} MB).`)
  const tam = join(thu, `.tmp-${randomBytes(8).toString('hex')}`)
  const hash = createHash('sha256')
  const out = createWriteStream(tam, { flags: 'wx' })
  let byte = 0
  let dau = Buffer.alloc(0)
  try {
    await new Promise<void>((ok, loi) => {
      const req = event.node.req
      req.on('data', (c: Buffer) => {
        byte += c.length
        if (byte > max) { req.destroy(); loi(apiLoi(413, `Tệp quá lớn (tối đa ${Math.round(max / 1048576)} MB).`)); return }
        if (dau.length < 4096) dau = Buffer.concat([dau, c.subarray(0, 4096 - dau.length)])
        hash.update(c)
        if (!out.write(c)) { req.pause(); out.once('drain', () => req.resume()) }
      })
      req.on('end', () => out.end(() => ok()))
      req.on('error', loi)
      out.on('error', loi)
    })
    if (byte === 0) throw apiLoi(400, 'Tệp rỗng.')
    if (!chuKyTepHopLe(duoi, dau)) throw apiLoi(400, `Nội dung không đúng định dạng .${duoi} (kiểm theo byte đầu tệp).`)
    const sha256 = hash.digest('hex')
    await rename(tam, dich)
    await writeFile(join(thu, `.${ten}.sha256`), `${sha256}\n`)
    return { ten, url: `/tai/${ten}`, byte, sha256, luc: new Date().toISOString() }
  }
  catch (e) {
    out.destroy()
    await unlink(tam).catch(() => {})
    throw e
  }
}

export async function xoaTepGame(ten: string): Promise<boolean> {
  const t = tenTepDaLuu(ten)
  if (!t) return false
  const thu = thuMucTepGame()
  try {
    await unlink(join(thu, t))
    await unlink(join(thu, `.${t}.sha256`)).catch(() => {})
    return true
  }
  catch { return false }
}
