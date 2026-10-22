/**
 * Chân dung 40×40 của mọi loài có ảnh (sinh bởi scripts/sinh-chan-dung-loai.mjs từ gói sprite máy chủ
 * server/map/loai và chân dung đã có ở public/pokemon). '' = chưa có ảnh (trang vẽ bóng Poké Ball).
 */
import raw from '../data/chan-dung-loai.json'

const BANG = raw as Record<string, string>

export function chanDungLoai(dex: number): string {
  return BANG[String(dex)] ?? ''
}
