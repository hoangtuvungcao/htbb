/**
 * Thư viện ảnh: lưu file ở HTBB_UPLOAD_DIR (mặc định <thư mục chạy web>/uploads), phục vụ qua
 * /uploads/<tên> (server/routes/uploads/[ten].get.ts). CHỈ nhận ảnh PNG / JPEG / GIF / WebP —
 * kiểm bằng chữ ký byte đầu file, KHÔNG tin đuôi tên hay Content-Type trình duyệt gửi.
 * Không nhận SVG (có thể chứa script). Tên file lưu là chuỗi ngẫu nhiên.
 */
import { join, resolve } from 'node:path'

export const MEDIA_TOI_DA = 5 * 1024 * 1024

export function thuMucTaiLen(): string {
  return resolve((process.env.HTBB_UPLOAD_DIR ?? process.env.KPAH_UPLOAD_DIR) || join(process.cwd(), 'uploads')) // TUONG-THICH-TEN-CU
}

export interface ThongTinAnh { mime: string, duoi: string, rong: number, cao: number }

/** Nhận diện ảnh + kích thước từ nội dung. null = không phải ảnh được phép. */
export function nhanDienAnh(b: Buffer): ThongTinAnh | null {
  if (b.length < 16) return null
  // PNG
  if (b.readUInt32BE(0) === 0x89504E47 && b.readUInt32BE(4) === 0x0D0A1A0A) {
    return { mime: 'image/png', duoi: 'png', rong: b.readUInt32BE(16), cao: b.readUInt32BE(20) }
  }
  // GIF
  if (b.toString('ascii', 0, 6) === 'GIF87a' || b.toString('ascii', 0, 6) === 'GIF89a') {
    return { mime: 'image/gif', duoi: 'gif', rong: b.readUInt16LE(6), cao: b.readUInt16LE(8) }
  }
  // WebP
  if (b.toString('ascii', 0, 4) === 'RIFF' && b.toString('ascii', 8, 12) === 'WEBP') {
    const loai = b.toString('ascii', 12, 16)
    let rong = 0, cao = 0
    if (loai === 'VP8X' && b.length >= 30) { rong = 1 + b.readUIntLE(24, 3); cao = 1 + b.readUIntLE(27, 3) }
    else if (loai === 'VP8 ' && b.length >= 30) { rong = b.readUInt16LE(26) & 0x3FFF; cao = b.readUInt16LE(28) & 0x3FFF }
    else if (loai === 'VP8L' && b.length >= 25) { const v = b.readUInt32LE(21); rong = (v & 0x3FFF) + 1; cao = ((v >> 14) & 0x3FFF) + 1 }
    return { mime: 'image/webp', duoi: 'webp', rong, cao }
  }
  // JPEG: tìm khung SOFn
  if (b[0] === 0xFF && b[1] === 0xD8 && b[2] === 0xFF) {
    let i = 2
    while (i + 9 < b.length) {
      if (b[i] !== 0xFF) { i++; continue }
      const m = b[i + 1]!
      if (m >= 0xC0 && m <= 0xCF && m !== 0xC4 && m !== 0xC8 && m !== 0xCC) {
        return { mime: 'image/jpeg', duoi: 'jpg', cao: b.readUInt16BE(i + 5), rong: b.readUInt16BE(i + 7) }
      }
      i += 2 + b.readUInt16BE(i + 2)
    }
    return { mime: 'image/jpeg', duoi: 'jpg', rong: 0, cao: 0 }
  }
  return null
}

export const MIME_THEO_DUOI: Record<string, string> = { png: 'image/png', jpg: 'image/jpeg', gif: 'image/gif', webp: 'image/webp' }
